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

import org.opencv.android.JavaCamera2View;
import org.opencv.android.JavaCameraView;

public class CameraTestActivity extends AppCompatActivity {

    private static final int PERMISSION_CAMERA = 1;
    JavaCameraView javaCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);
        javaCameraView = findViewById(R.id.HelloOpenCvView);

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
}
