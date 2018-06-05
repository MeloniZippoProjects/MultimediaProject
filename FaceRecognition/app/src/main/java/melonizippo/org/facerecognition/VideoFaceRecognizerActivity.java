package melonizippo.org.facerecognition;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import org.bytedeco.javacv.FrameFilter;

import melonizippo.org.facerecognition.algorithms.VideoFaceRecognizer;

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
                    null,
                    null,
                    frameView
            );
        } catch (Exception e) {}
    }
}
