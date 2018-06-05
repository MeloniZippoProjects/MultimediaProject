package melonizippo.org.facerecognition;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import org.bytedeco.javacv.FrameFilter;

import java.io.File;

import melonizippo.org.facerecognition.algorithms.VideoFaceRecognizer;
import melonizippo.org.facerecognition.algorithms.opencv.facerecognition.Parameters;

public class VideoFaceRecognizerActivity extends AppCompatActivity {

    private VideoFaceRecognizer videoFaceRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_face_recognizer);
    }

    public void buttonStart(View view) {
        ImageView frameView = findViewById(R.id.frameView);

        try {
            videoFaceRecognizer = new VideoFaceRecognizer(
                    Parameters.HAAR_CASCADE_FRONTALFACE,
                    Parameters.STORAGE_FILE,
                    frameView );

            dispatchTakeVideoIntent();
        }
        catch (Exception e) {}
    }

    static final int REQUEST_VIDEO_CAPTURE = 1;

    private void dispatchTakeVideoIntent()
    {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK)
        {
            Uri videoUri = intent.getData();
            File videoFile = new File(videoUri.getPath());

            try{
                videoFaceRecognizer.initVideo(
                        videoFile,
                        null);
                videoFaceRecognizer.analyzeVideo();
                videoFaceRecognizer.closeRecorder();
            }
            catch (Exception e) {}
        }
    }
}
