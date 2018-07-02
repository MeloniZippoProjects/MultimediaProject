package melonizippo.org.facerecognition.facerecognition;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import melonizippo.org.facerecognition.FaceRecognitionApp;
import melonizippo.org.facerecognition.database.FaceData;
import melonizippo.org.facerecognition.deep.DNNExtractor;

public class FaceDetectionExecutor {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private DNNExtractor extractor;
    private KNNClassifier knnClassifier;

    private TextView textView;

    private Semaphore classifying;

    public FaceDetectionExecutor(FaceRecognitionApp app, TextView textView)
    {
        extractor = app.extractor;
        knnClassifier = app.knnClassifier;
        this.textView = textView;
        classifying = new Semaphore(1);
    }

    private static Mat faceMat = new Mat();
    public boolean classifyFaces(Mat frameMat, MatOfRect faces)
    {
        if(!classifying.tryAcquire())
            return false;

        Runnable task = () -> {
            try {
                if(faces.toList().size() == 0)
                {
                    updateLabel("No face detected");
                    return;
                }

                //todo: we do not need LabeledRects
                List<LabeledRect> labeledRects = new LinkedList<>();

                for (Rect face : faces.toArray()) {
                    faceMat = frameMat.submat(face);
                    float[] faceFeatures = extractor.extract(faceMat);
                    FaceData query = new FaceData(faceMat, faceFeatures);
                    PredictedClass predict = knnClassifier.predict(query);
                    //todo: if intruder, send alarm and store it

                    LabeledRect labeledRect = new LabeledRect(face, predict.getLabel() + "(" + predict.getConfidence() + ")", predict.getConfidence());
                    //LabeledRect labeledRect = new LabeledRect(face, "enrico" + "(" + 1 + ")", 1d);
                    labeledRects.add(labeledRect);
                }

                StringBuilder stringBuilder = new StringBuilder("");
                for (LabeledRect labeledRect : labeledRects) {
                    stringBuilder.append(labeledRect.getLabel()).append("\n");
                }

                updateLabel(stringBuilder.toString());

            }
            catch(Exception ex)
            {
                Log.d("executor", "task crashed");
            }
            finally {
                classifying.release();
            }
        };

        executorService.submit(task);

        return true;
    }

    private void updateLabel(String newLabel)
    {
        Handler mainHandler = new Handler(Looper.getMainLooper());

            mainHandler.post(() -> textView.setText(String.format("%s\n", newLabel)));
    }
}
