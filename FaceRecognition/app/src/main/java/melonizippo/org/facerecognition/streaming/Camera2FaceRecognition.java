package melonizippo.org.facerecognition.streaming;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import melonizippo.org.facerecognition.FaceRecognitionApp;
import melonizippo.org.facerecognition.IdentitiesViewActivity;
import melonizippo.org.facerecognition.R;
import melonizippo.org.facerecognition.facerecognition.FaceDetectionExecutor;
import melonizippo.org.facerecognition.facerecognition.FaceDetector;

public class Camera2FaceRecognition extends AppCompatActivity {

    public static final String TAG = "Camera2 Face Recognition";

    //we are interested only in the two main cameras
    public static final String BACK_CAMERA = "0";
    public static final String FRONT_CAMERA = "1";
    private static final int PERMISSION_CAMERA = 1;

    /**
     * Used to get CameraDevice instances
     */
    private CameraManager cameraManager;


    private String currentCameraId;
    private CameraDevice currentCameraDevice;

    private AutoFitSurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private ImageReader imageReader;
    private CameraCaptureSession cameraCaptureSession;

    private TextView textView;

    private FaceDetectionExecutor executorService;

    //face recognition objects
    public FaceDetector faceDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2_face_recognition);

        FaceRecognitionApp app = (FaceRecognitionApp) getApplication();
        faceDetector = app.faceDetector;

        //todo: get it from savedInstanceState
        if(savedInstanceState != null)
            currentCameraId = savedInstanceState.getString("currentCamera");
        else
            currentCameraId = FRONT_CAMERA;

        //obtain cameraManager instance
        cameraManager = getSystemService(CameraManager.class);

        //obtain surface holder
        surfaceView = findViewById(R.id.camera_preview);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceHolderCallback);

        textView = findViewById(R.id.faceLabel);

        executorService = new FaceDetectionExecutor(app, textView);

        //graphic settings
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton identitiesEditor = findViewById(R.id.goToIdentitiesEditor);
        identitiesEditor.setOnClickListener(view -> {
            Intent intent = new Intent(Camera2FaceRecognition.this, IdentitiesViewActivity.class);
            startActivity(intent);
        });

        FloatingActionButton switchCamera = findViewById(R.id.switchCamera);
        switchCamera.setOnClickListener(view -> {
            toggleJavaCameraView();
        });
    }

    private void toggleJavaCameraView() {
        if(currentCameraId.equals(FRONT_CAMERA))
            currentCameraId = BACK_CAMERA;
        else
            currentCameraId = FRONT_CAMERA;

        //close previous captureSession
        cameraCaptureSession.close();
        imageReader.close();
        currentCameraDevice.close();

        openCamera(currentCameraId);

    }

    @Override
    protected void onSaveInstanceState(Bundle outstate) {
        super.onSaveInstanceState(outstate);
        outstate.putString("currentCamera", currentCameraId);
        Log.d(TAG, "save instance state");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "on pause");
        if(canvas != null)
            surfaceHolder.unlockCanvasAndPost(canvas);
        imageReader.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "on resume");
    }

    private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            setLayout();
            //open front camera at startup
            openCamera(currentCameraId);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private void setLayout() {
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int width = 0;
        int height = 0;
        switch (rotation)
        {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                    width = 3;
                    height = 4;
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                    width = 4;
                    height = 3;
                break;
        }
        surfaceView.setAspectRatio(width,height);
    }

    private CameraDevice.StateCallback cameraCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            currentCameraDevice = camera;
            try {
                CameraCharacteristics characteristics
                        = cameraManager.getCameraCharacteristics(currentCameraId);
                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if(map == null)
                {
                    Log.e(TAG, "Configuration map is null");
                    return;
                }

                Size[] sizes = map.getOutputSizes(ImageReader.class);

                imageReader = ImageReader.newInstance(surfaceView.getHeight()/2, surfaceView.getWidth()/2, ImageFormat.JPEG, 5);
                imageReader.setOnImageAvailableListener(imageAvailableListener, null);
                createCaptureSession();

                return;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "camera " + camera.getId() + " disconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG, "camera " + camera.getId() + " error");
        }
    };

    private void createCaptureSession() throws CameraAccessException {
        List<Surface> targets = new ArrayList<>();
        targets.add(imageReader.getSurface());
        currentCameraDevice.createCaptureSession(targets, captureSessionStateCallback, null);
    }

    private CameraCaptureSession.StateCallback captureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "configured capture session");

            cameraCaptureSession = session;

            //create capture request
            try {
                CaptureRequest.Builder captureRequest = currentCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequest.addTarget(imageReader.getSurface());
                session.setRepeatingRequest(captureRequest.build(), null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "cannot configure capture session");
        }
    };

    private ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            Bitmap bitmap = processImage(image);
            image.close();

            canvas = surfaceHolder.lockCanvas();
            canvas.drawBitmap(bitmap, new Matrix(), null);
            surfaceHolder.unlockCanvasAndPost(canvas);
            canvas = null;
        }
    };

    private Mat mImage = new Mat();
    private Bitmap processImage(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);

        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        Matrix rotateMatrix = new Matrix();
        rotateMatrix.setRotate(getOrientationDegrees(), bitmapImage.getWidth()/2, bitmapImage.getHeight());
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapImage, 0, 0,
                bitmapImage.getWidth(), bitmapImage.getHeight(),
                rotateMatrix, false);

        Utils.bitmapToMat(rotatedBitmap, mImage);

        //do face detection stuff
        MatOfRect faces = faceDetector.detect(mImage);

         mImage = printFaceBoxesOnMat(mImage, faces.toList());

        if(executorService.classifyFaces(mImage, faces))
            Log.d(TAG, "Queued an image for labeling");

        //todo: if intruder, send alarm and store it

        Bitmap processedBitmap = Bitmap.createBitmap(mImage.cols(), mImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mImage, processedBitmap);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(processedBitmap, surfaceView.getWidth(), surfaceView.getHeight(), false);
        return scaledBitmap;
    }

    public void openCamera(String cameraId) {
        currentCameraId = cameraId;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CAMERA);
            return;
        }
        try {
            cameraManager.openCamera(currentCameraId, cameraCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Cannot open camera");
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case PERMISSION_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera(currentCameraId);
                }
            }
        }
    }

    private static Mat outputMat = new Mat();
    private static Scalar rectColor = new Scalar(255, 255, 255, 255);
    private Mat printFaceBoxesOnMat(Mat frameMat, List<Rect> faces)
    {
        if(faces.size() == 0)
            return frameMat;

        frameMat.copyTo(outputMat);
        for(Rect rect : faces)
        {
            Point p1 = rect.tl();
            Point p2 = rect.br();
            Imgproc.rectangle(outputMat, p1, p2, rectColor, 5);
        }
        return outputMat;
    }

    private int getOrientationDegrees()
    {
        int rotation = this.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;

        switch (rotation)
        {
            case Surface.ROTATION_0: degrees = 90; break;
            case Surface.ROTATION_90: degrees = 0; break;
            case Surface.ROTATION_180: degrees = 270; break;
            case Surface.ROTATION_270: degrees = 180; break;
        }

        if(currentCameraId.equals(FRONT_CAMERA))
            degrees *= -1;

        return degrees;
    }

}
