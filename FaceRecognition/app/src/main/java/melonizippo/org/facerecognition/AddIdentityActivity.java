package melonizippo.org.facerecognition;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;


import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import melonizippo.org.facerecognition.database.FaceData;
import melonizippo.org.facerecognition.deep.DNNExtractor;
import melonizippo.org.facerecognition.deep.Parameters;
import melonizippo.org.facerecognition.facerecognition.FaceDetector;

public class AddIdentityActivity extends AppCompatActivity
{
    private static final String TAG = "AddIdentityActivity";
    private static final int PICK_IMAGE = 1;
    private static final int SHOOT_IMAGE = 2;
    private static final int PICK_IMAGE_MULTIPLE = 3;

    private boolean isDefaultLabel = true;

    private Uri cameraImageUri;

    private FaceDetector faceDetector;
    private DNNExtractor extractor;

    private List<FaceData> faceData = new ArrayList<>();
    private static FaceDataAdapter faceDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_identity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        faceDetector = new FaceDetector(
                InternalStorageFiles.getFile(
                        InternalStorageFiles.HAARCASCADE_FRONTALFACE)
                        .getPath());
        extractor = new DNNExtractor(
                InternalStorageFiles.getFile(InternalStorageFiles.VGG_PROTOTXT),
                InternalStorageFiles.getFile(InternalStorageFiles.VGG_CAFFE_MODEL)
        );

        faceDataAdapter = new FaceDataAdapter(faceData, getApplicationContext());

        ListView previewsView = (ListView) findViewById(R.id.previewsView);
        previewsView.setAdapter(faceDataAdapter);

        FloatingActionButton addPhotosButton = (FloatingActionButton) findViewById(R.id.addPhotos);
        addPhotosButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showPictureDialog();
            }
        });

        final TextInputEditText labelEditor = (TextInputEditText) findViewById(R.id.identityLabelField);
        labelEditor.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                clearPlaceholderText();
            }
        });
    }

    private void clearPlaceholderText()
    {
        final TextInputEditText labelEditor = (TextInputEditText) findViewById(R.id.identityLabelField);
        if(isDefaultLabel)
        {
            labelEditor.setText("");
            isDefaultLabel = false;
        }
    }

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Select multiple photos from gallery",
                "Capture photo from camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallery();
                                break;
                            case 1:
                                choosePhotosFromGallery();
                                break;
                            case 2:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery()
    {
        Intent galleryIntent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, PICK_IMAGE);
    }

    public void choosePhotosFromGallery()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_MULTIPLE);
    }

    private void takePhotoFromCamera()
    {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo;
        try
        {
            // place where to store camera taken picture
            photo = this.createTemporaryFile("picture", ".jpg");
            photo.delete();
        }
        catch(Exception e)
        {
            Log.v(TAG, "Can't create file to take picture!");
            Toast.makeText(this, "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG);
            return;
        }
        cameraImageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        //start camera intent
        startActivityForResult(intent, SHOOT_IMAGE);
    }

    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getDataDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == RESULT_OK )
        {
            if (requestCode == PICK_IMAGE && data != null)
            {
                Uri imageUri = data.getData();
                addImage(imageUri);
            }
            else if (requestCode == SHOOT_IMAGE)
            {
                addImage(cameraImageUri);
            }
            else if (requestCode == PICK_IMAGE_MULTIPLE && data != null)
            {
                if( data.getData() != null )
                {
                    Uri imageUri = data.getData();
                    addImage(imageUri);
                }
                else
                {
                    if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();
                        for (int i = 0; i < clipData.getItemCount(); i++)
                        {
                            ClipData.Item item = clipData.getItemAt(i);
                            Uri imageUri = item.getUri();
                            addImage(imageUri);
                        }
                    }
                }
            }
        }
    }

    private void addImage(Uri imageUri)
    {
        Bitmap imageBitmap;
        try
        {
             imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        }
        catch (Exception ex)
        {
            Log.i(TAG, "Failed to open the image");
            return;
        }

        Mat imageMat = new Mat();
        Utils.bitmapToMat(imageBitmap, imageMat);

        MatOfRect facesMat = faceDetector.detect(imageMat, Parameters.FACE_MIN_SIZE, Parameters.FACE_MAX_SIZE);
        Rect[] faces = facesMat.toArray();

        //bug: for some reason, it's always more than 1 face
        //if(faces.length != 1)
        if(false)
        {
            //todo: show that kind of notification on black background, bottom side of the screen, which disappears after a while

            Log.i(TAG, "Detected more than one face");
            return;
        }

        if(faces.length < 1)
        {
            //todo: show that kind of notification on black background, bottom side of the screen, which disappears after a while

            Log.i(TAG, "No face detected");
            return;

        }

        Rect faceRect = faces[0];

        FaceData fd = new FaceData();
        fd.FaceMat = imageMat.submat(faceRect);
        fd.Features = extractor.extract(fd.FaceMat, Parameters.DEEP_LAYER);

        faceData.add(fd);
        faceDataAdapter.notifyDataSetChanged();
    }

    public class FaceDataPreview
    {
        public FaceData faceData;
        public Bitmap preview;

        public FaceDataPreview(FaceData fd)
        {
            faceData = fd;
            preview = fd.toBitmap();
        }
    }
}
