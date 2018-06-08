package melonizippo.org.facerecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class ImageMatTestActivity extends AppCompatActivity {

    boolean imgShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_mat_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("prova", "onclick launched");

                ImageView imageView = findViewById(R.id.bludraw);
                Context context = getApplicationContext();

                BitmapDrawable bitmapDrawable = (BitmapDrawable)ContextCompat.getDrawable(context, R.drawable.blus);
                Bitmap colorBitmap = bitmapDrawable.getBitmap();

                Mat imgMat = new Mat();
                Mat grayMat = new Mat();

                Utils.bitmapToMat(colorBitmap, imgMat);

                Imgproc.cvtColor(imgMat, grayMat, Imgproc.COLOR_RGB2GRAY);
                Bitmap grayBitmap = Bitmap.createBitmap(grayMat.cols(),grayMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(grayMat, grayBitmap);
                imageView.setImageBitmap(grayBitmap);
            }
        });
    }

}
