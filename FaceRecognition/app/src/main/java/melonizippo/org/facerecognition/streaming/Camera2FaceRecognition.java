package melonizippo.org.facerecognition.streaming;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import melonizippo.org.facerecognition.R;

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

    private SurfaceHolder surfaceHolder;
    private ImageReader imageReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2_face_recognition);

        //todo: get it from savedInstanceState
        currentCameraId = FRONT_CAMERA;

        //obtain cameraManager instance
        cameraManager = getSystemService(CameraManager.class);

        //obtain surface holder
        SurfaceView surfaceView = findViewById(R.id.camera_preview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceHolderCallback);


        //graphic settings
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
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
                //todo: consider using yuv
                //Size[] sizes = map.getOutputSizes(ImageFormat.JPEG);
                //Size[] sizes1 = map.getOutputSizes(SurfaceHolder.class);
                //Size[] sizes = map.getOutputSizes(ImageReader.class);

                imageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 1);
                imageReader.setOnImageAvailableListener(imageAvailableListener, null);
                List<Surface> targets = new ArrayList<>();
                targets.add(imageReader.getSurface());
                currentCameraDevice.createCaptureSession(targets, captureSessionStateCallback, null);

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

    private CameraCaptureSession.StateCallback captureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "configured capture session");

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

            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawBitmap(bitmap, new Matrix(), null);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    };

    private Bitmap processImage(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        image.close();
        return bitmapImage;
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

}
