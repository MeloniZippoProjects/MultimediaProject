package melonizippo.org.facerecognition;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import melonizippo.org.facerecognition.deep.DNNExtractor;
import melonizippo.org.facerecognition.deep.Parameters;
import melonizippo.org.facerecognition.facerecognition.FaceDetector;
import melonizippo.org.facerecognition.facerecognition.KNNClassifier;

public class FaceRecognitionActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final int PERMISSION_CAMERA = 1;
    private static final int CAMERA_FRONT = 1;
    private static final int CAMERA_BACK = 0;
    JavaCameraView javaCameraView;

    public FaceDetector faceDetector;
    public DNNExtractor extractor;
    public KNNClassifier knnClassifier;

    private int currentCamera = CAMERA_FRONT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);

        FaceRecognitionApp app = (FaceRecognitionApp) getApplication();
        faceDetector = app.faceDetector;
        extractor = app.extractor;
        knnClassifier = app.knnClassifier;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.goToIdentitiesEditor);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FaceRecognitionActivity.this, IdentitiesEditorActivity.class);
                startActivity(intent);
            }
        });

        javaCameraView = findViewById(R.id.HelloOpenCvView);
        javaCameraView.setCvCameraViewListener(this);

        javaCameraView.setCameraIndex(currentCamera);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CAMERA);
        }
        else {
            enableJavaCameraView();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults)
    {
        switch (requestCode) {
            case PERMISSION_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableJavaCameraView();
                }
                return;
            }
        }
    }

    protected void enableJavaCameraView()
    {
        try {
            javaCameraView.enableView();
            Log.d("test", "enable java camera view");
        }
        catch(Exception e) {
            Log.e("prova", "cannot open camera");
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(javaCameraView != null)
            javaCameraView.enableView();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height)
    {
    }

    @Override
    public void onCameraViewStopped()
    {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        Mat frameMat = adjustMatOrientation(inputFrame.rgba());
        frameMat = adjustMirroring(frameMat);
        //todo: process with face recognition
        //todo: draw stuff on it

        MatOfRect faces = faceDetector.detect(frameMat);

        classifyFaces(frameMat, faces);

        //todo: if intruder, send alarm and store it

        Mat outputMat = printFaceBoxesOnMat(frameMat, faces);

        return outputMat;
    }

    private Mat adjustMirroring(Mat frameMat) {
        Mat mirroredMap = new Mat();
        if(currentCamera == CAMERA_FRONT)
            Core.flip(frameMat, mirroredMap, 1);
        return mirroredMap;
    }

    //todo: define proper return type
    private void classifyFaces(Mat frameMat, MatOfRect faces)
    {
        for(Rect face : faces.toArray())
        {
            Mat faceMat = frameMat.submat(face);
            float[] faceFeatures = extractor.extract(faceMat);

            //todo: classify with knn
        }
    }

    private Mat printFaceBoxesOnMat(Mat frameMat, MatOfRect faces)
    {
        Mat outputMat = frameMat.clone();
        for(Rect rect : faces.toArray())
        {
            Point p1 = rect.tl();
            Point p2 = rect.br();
            Imgproc.rectangle(outputMat, p1, p2, new Scalar(0, 0, 255), 5);
        }

        return outputMat;
    }

    private Mat adjustMatOrientation(Mat frameMat)
    {
        int degrees = -1 * getOrientationDegrees();
        if(degrees != 0) //check for potrait mode
        {
            Mat rotationMat = Imgproc.getRotationMatrix2D(new Point(frameMat.width() / 2, frameMat.height() / 2), degrees, 1);
            //Mat rotatedMat = new Mat(new Size(frameMat.height(), frameMat.width()), CvType.CV_8UC4);
            Mat rotatedMat = new Mat();
            Imgproc.warpAffine(frameMat, rotatedMat, rotationMat, frameMat.size());
            return rotatedMat;
        }
        else
            return frameMat;
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

        if(currentCamera == CAMERA_FRONT)
            degrees *= -1;

        return degrees;
    }
}
