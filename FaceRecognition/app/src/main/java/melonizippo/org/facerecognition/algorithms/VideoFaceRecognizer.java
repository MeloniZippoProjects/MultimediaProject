package melonizippo.org.facerecognition.algorithms;

import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import melonizippo.org.facerecognition.algorithms.deep.DNNExtractor;
import melonizippo.org.facerecognition.algorithms.deep.ImgDescriptor;
import melonizippo.org.facerecognition.algorithms.opencv.facerecognition.FaceDetector;
import melonizippo.org.facerecognition.algorithms.opencv.facerecognition.KNNClassifier;
import melonizippo.org.facerecognition.algorithms.opencv.facerecognition.Parameters;
import melonizippo.org.facerecognition.algorithms.opencv.facerecognition.PredictedClass;
import melonizippo.org.facerecognition.algorithms.opencv.facerecognition.tools.BoundingBox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.ImageView;

public class VideoFaceRecognizer {

	private FaceDetector faceDetector;

	private KNNClassifier knnClassifier;

	private FrameGrabber grabber;

	private FFmpegFrameRecorder recorder;

	private OpenCVFrameConverter.ToMat frame2Mat;

	private DNNExtractor extractor;

	private ImageView outputView;

	public VideoFaceRecognizer(
			String haarcascadePath,
			File storageFile,
			ImageView outputView
	)
			throws ClassNotFoundException, IOException
	{
		extractor = new DNNExtractor();
		faceDetector = new FaceDetector(haarcascadePath);
		knnClassifier = new KNNClassifier(storageFile);
		frame2Mat = new OpenCVFrameConverter.ToMat();
		this.outputView = outputView;

		/*canvas = new CanvasFrame("OpenCV Face Recognition");
		canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		canvas.setCanvasSize(Parameters.VIDEO_WIDTH, Parameters.VIDEO_HEIGHT);*/
	}

	public void initVideo(File srcVideo, File destVideoFile)
			throws Exception
	{
		grabber = FFmpegFrameGrabber.createDefault(srcVideo);
		grabber.start();
		
		recorder = new FFmpegFrameRecorder(new FileOutputStream(destVideoFile), Parameters.VIDEO_WIDTH, Parameters.VIDEO_HEIGHT);
		recorder.setFrameRate(30);
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		recorder.setFormat("avi");

		recorder.setAudioChannels(grabber.getAudioChannels());
		recorder.setAudioCodec(grabber.getAudioCodec());
		recorder.start();
	}

	public void closeRecorder()
			throws org.bytedeco.javacv.FrameRecorder.Exception
	{
		recorder.stop();
		recorder.close();
	}

	//TODO
	public void analyzeVideo()
			throws Exception
	{
		Frame frame;
		
		while ((frame = grabber.grab()) != null) {
			
			//Detect all the faces in the frame and classify them
			//highlight them with a bounding box and the classification label using BoundingBox.highlight method
			Mat matFrame = frame2Mat.convert(frame);
			if(matFrame == null)
				continue;

			RectVector faces = faceDetector.detect(matFrame, Parameters.FACE_MIN_SIZE, Parameters.FACE_MAX_SIZE);
			for (int i = 0; i < faces.size(); i++) {
				Rect face = faces.get(i);
				Mat faceMat = matFrame.apply(face);

				float[] features = extractor.extract(faceMat, Parameters.DEEP_LAYER);
				ImgDescriptor query = new ImgDescriptor(features, null, null);

				PredictedClass predictedClass = knnClassifier.predict(query);
				String label = "label: " + predictedClass.getLabel() + " at " + predictedClass.getConfidence();

				BoundingBox.highlight(matFrame, faces.get(i), label);
			}

			showFrame(frame);

			recorder.record(frame);

		}
		closeRecorder();

	}

	//for testing only
	Random rng = new Random();

	//todo: implement print on outputView
	private void showFrame(Frame frame)
	{
        Bitmap bitmap = Bitmap.createBitmap(
            outputView.getHeight(),
            outputView.getWidth(),
            Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(rng.nextInt());

		outputView.draw(canvas);
	}
}