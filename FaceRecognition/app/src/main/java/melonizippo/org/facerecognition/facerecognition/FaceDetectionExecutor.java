package melonizippo.org.facerecognition.facerecognition;

import android.util.Log;
import android.widget.TextView;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.w3c.dom.Text;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

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
                List<LabeledRect> labeledRects = new LinkedList<>();

                for (Rect face : faces.toArray()) {
                    faceMat = frameMat.submat(face);
                    float[] faceFeatures = extractor.extract(faceMat);
                    FaceData query = new FaceData(faceMat, faceFeatures);
                    PredictedClass predict = knnClassifier.predict(query);

                    LabeledRect labeledRect = new LabeledRect(face, predict.getLabel() + "(" + predict.getConfidence() + ")", predict.getConfidence());
                    //LabeledRect labeledRect = new LabeledRect(face, "enrico" + "(" + 1 + ")", 1d);
                    labeledRects.add(labeledRect);
                }

                for (LabeledRect labeledRect : labeledRects) {
                    //labelBuffer.append(labeledRect.getLabel()).append("\n").append(textView.getText());
                    //textView.setText(labelBuffer.toString());
                    //textView.requestLayout();

                    //todo: only threads which create the view hierarchy can access this view
                    textView.append(labeledRect.getLabel() + "\n");
                }
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
}
