package melonizippo.org.facerecognition;

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

            videoFaceRecognizer.initVideo(
                    new File(Parameters.VIDEO_PATH),
                    Parameters.DEST_VIDEO );
            videoFaceRecognizer.analyzeVideo();
            videoFaceRecognizer.closeRecorder();
        }
        catch (Exception e) {}
    }
}
