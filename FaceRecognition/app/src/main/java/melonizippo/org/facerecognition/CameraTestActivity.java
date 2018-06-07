package melonizippo.org.facerecognition;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.JavaCameraView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;

import java.io.File;

import melonizippo.org.facerecognition.deep.DNNExtractor;
import melonizippo.org.facerecognition.facerecognition.FaceDetector;
import melonizippo.org.facerecognition.facerecognition.KNNClassifier;

public class CameraTestActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final int PERMISSION_CAMERA = 1;
    JavaCameraView javaCameraView;
    private DNNExtractor extractor;
    private FaceDetector faceDetector;
    private KNNClassifier knnClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);

        javaCameraView = findViewById(R.id.HelloOpenCvView);
        javaCameraView.setCvCameraViewListener(this);

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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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
    public void onDestroy()
    {
        super.onDestroy();
        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height)
    {
        extractor = new DNNExtractor();
        faceDetector = new FaceDetector(
                InternalStorageFiles.getFile(
                        InternalStorageFiles.HAARCASCADE_FRONTALFACE)
                        .getPath()); //todo: add haarcascades files

//        try {
//            knnClassifier = new KNNClassifier(File.createTempFile("place", "holder")); //todo: implement internal storage
//        } catch(Exception e) {}
    }

    @Override
    public void onCameraViewStopped()
    {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        Mat frameMat = adjustMatOrientation(inputFrame.rgba());
        //todo: process with face recognition
        //todo: draw stuff on it
        return frameMat;
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

        return degrees;
    }
}
