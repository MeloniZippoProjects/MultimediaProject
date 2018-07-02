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
import melonizippo.org.facerecognition.database.FaceDatabase;
import melonizippo.org.facerecognition.database.FaceDatabaseStorage;
import melonizippo.org.facerecognition.deep.DNNExtractor;
import melonizippo.org.facerecognition.deep.Parameters;

public class FaceDetectionExecutor {

    private ExecutorService classificationService = Executors.newSingleThreadExecutor();
    private ExecutorService logAndStoreService = Executors.newSingleThreadExecutor();

    private DNNExtractor extractor;
    private KNNClassifier knnClassifier;

    private TextView classificationTextView;

    private Semaphore classifying;

    public FaceDetectionExecutor(FaceRecognitionApp app, TextView classificationTextView)
    {
        extractor = app.extractor;
        knnClassifier = app.knnClassifier;
        this.classificationTextView = classificationTextView;
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
                    updateClassificationText("No face detected");
                    return;
                }

                //todo: check bad db states
                if(FaceDatabaseStorage.getFaceDatabase().getSampleCount() < Parameters.K)
                {
                    updateClassificationText("Not enough samples in database for classification");
                    return;
                }

                List<PredictedClass> predictedClasses = new LinkedList<>();
                for (Rect face : faces.toArray())
                {
                    faceMat = frameMat.submat(face);
                    float[] faceFeatures = extractor.extract(faceMat);
                    FaceData query = new FaceData(faceMat, faceFeatures);

                    PredictedClass predict = knnClassifier.predict(query);

                    predictedClasses.add(predict);
                }

                logAndStoreService.submit(
                        () -> logAndStore(predictedClasses));

                StringBuilder stringBuilder = new StringBuilder("");
                for (PredictedClass predictedClass: predictedClasses)
                {
                    stringBuilder.append(predictedClass.toString()).append("\n");
                }

                updateClassificationText(stringBuilder.toString());
            }
            catch(Exception ex)
            {
                Log.d("executor", "task crashed");
            }
            finally
            {
                classifying.release();
            }
        };

        classificationService.submit(task);

        return true;
    }

    private void logAndStore(List<PredictedClass> predictedClasses)
    {
        boolean databaseChanged = false;
        for (PredictedClass predictedClass: predictedClasses)
        {
            //todo: add log entry?

            if (!predictedClass.isClassified())
            {
                FaceDatabase db = FaceDatabaseStorage.getFaceDatabase();
                db.uncategorizedData.put(db.nextMapId.incrementAndGet(), predictedClass.getFaceData());
                databaseChanged = true;
            }
        }

        if(databaseChanged)
            FaceDatabaseStorage.storeToInternalStorage();
    }

    private void updateClassificationText(String newLabel)
    {
        Handler mainHandler = new Handler(Looper.getMainLooper());

            mainHandler.post(() -> classificationTextView.setText(String.format("%s\n", newLabel)));
    }
}
