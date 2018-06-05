package melonizippo.org.facerecognition.algorithms.opencv.facerecognition;

import static org.bytedeco.javacpp.opencv_imgproc.putText;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;


public class BoundingBox {
	
	public static void highlight(Mat image, Rect rect) {
		highlight(image, rect, null);
	}
	
	public static void highlight(Mat image, Rect rect, String text) {
		int xMin = rect.x();
		int yMin = rect.y();
		int xMax = rect.width() + rect.x();
		int yMax = rect.height() + rect.y();
		int thick = 3;

		Point pt1 = new Point(xMin, yMin);
		Point pt2 = new Point(xMax, yMax);
		Scalar color = new Scalar(255, 0, 0, 0); // blue [green] [red]
		rectangle(image, pt1, pt2, color, thick, 3, 0);
		Point point = new Point(xMin, yMax+50);
		double fontSize = 1.2;
		if (text != null)
			putText(image, text, point, thick, fontSize, color);
	}

	public static Mat getImageROI(Mat img, Rect face) {
		Rect rect = new Rect(face.x(), face.y(), face.width(), face.height());
		return new Mat(img, rect);
	}
	
	public static void imshow(String title, Mat img) {
		OpenCVFrameConverter frame2Mat = new OpenCVFrameConverter.ToMat();

		CanvasFrame canvas = new CanvasFrame(title);
		canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		canvas.setCanvasSize(img.cols(), img.rows());
		canvas.showImage(frame2Mat.convert(img));	
	}
}
