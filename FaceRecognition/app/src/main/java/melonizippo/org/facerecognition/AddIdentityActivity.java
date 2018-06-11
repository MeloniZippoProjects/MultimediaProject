package melonizippo.org.facerecognition;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.Toast;


import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import melonizippo.org.facerecognition.database.FaceData;
import melonizippo.org.facerecognition.database.FaceDatabase;
import melonizippo.org.facerecognition.database.FaceDatabaseStorage;
import melonizippo.org.facerecognition.database.IdentityEntry;
import melonizippo.org.facerecognition.deep.DNNExtractor;
import melonizippo.org.facerecognition.facerecognition.FaceDetector;
import melonizippo.org.facerecognition.facerecognition.KNNClassifier;

public class AddIdentityActivity extends AppCompatActivity
{
    private static final String TAG = "AddIdentityActivity";
    private static final int PICK_IMAGE = 1;
    private static final int SHOOT_IMAGE = 2;
    private static final int PICK_IMAGE_MULTIPLE = 3;

    private boolean isDefaultLabel = true;

    private File cameraPictureFile;
    private Uri cameraPictureUri;

    private List<FaceData> faceData = new ArrayList<>();
    private static FaceDataAdapter faceDataAdapter;

    private FaceDetector faceDetector;
    private DNNExtractor extractor;
    private KNNClassifier knnClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_identity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FaceRecognitionApp app = (FaceRecognitionApp) getApplication();
        faceDetector = app.faceDetector;
        extractor = app.extractor;
        knnClassifier = app.knnClassifier;

        faceDataAdapter = new FaceDataAdapter(faceData, getApplicationContext());

        GridView previewsView = findViewById(R.id.previewsView);
        previewsView.setAdapter(faceDataAdapter);

        FloatingActionButton addPhotosButton = findViewById(R.id.addPhotos);
        addPhotosButton.setOnClickListener((view) -> showPictureDialog());

        final TextInputEditText labelEditor = findViewById(R.id.identityLabelField);
        labelEditor.setOnClickListener(view -> clearPlaceholderText());
        labelEditor.setOnFocusChangeListener((view, l) -> clearPlaceholderText());

        final Button commitButton = findViewById(R.id.commitButton);
        commitButton.setOnClickListener(view -> commitAddIdentity());
    }

    private void clearPlaceholderText()
    {
        final TextInputEditText labelEditor = findViewById(R.id.identityLabelField);
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

    private void commitAddIdentity()
    {
        IdentityEntry identityEntry = new IdentityEntry();
        identityEntry.label = ((TextInputEditText)findViewById(R.id.identityLabelField)).getText().toString().trim();
        identityEntry.authorized = !((CheckBox)findViewById(R.id.sendAlertCheckbox)).isChecked();
        identityEntry.identityDataset = new ArrayList<>(faceData);

        if(!validateIdentity(identityEntry))
            return;
        else
        {
            FaceDatabaseStorage.getFaceDatabase().knownIdentities.add(identityEntry);
            FaceDatabaseStorage.store();

            showSnackBar(R.string.info_add_success);
            //should clear or exit the activity?
        }
    }

    private boolean validateIdentity(IdentityEntry identityEntry)
    {
        if(identityEntry.label.matches(""))
        {
            showSnackBar(R.string.error_no_name);
            return false;
        }

        FaceDatabase fd = FaceDatabaseStorage.getFaceDatabase();
        if(
                fd.knownIdentities.stream().anyMatch(
                        (dbIdentity) -> dbIdentity.label.matches(identityEntry.label))
        )
        {
            showSnackBar(R.string.error_name_duplicate);
            return false;
        }

        //todo: add checks for duplicate feature, not enough images...

        return true;
    }

    private void showSnackBar(int stringId)
    {
        Snackbar errorBar = Snackbar.make(
                findViewById(R.id.addIdentityCoordinatorLayout),
                stringId,
                Snackbar.LENGTH_SHORT);
        errorBar.show();
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
        try
        {
            // place where to store camera taken picture
            cameraPictureFile = this.createPictureFile("picture", ".jpg");
        }
        catch(Exception e)
        {
            Log.v(TAG, "Can't create file to take picture!");
            Toast.makeText(this, "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG);
            return;
        }
        cameraPictureUri = Uri.fromFile(cameraPictureFile);
        Uri cameraPicturePublicUri = FileProvider.getUriForFile(
                this,
                "melonizippo.org.facerecognition",
                cameraPictureFile
        );
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPicturePublicUri);

        //start camera intent
        startActivityForResult(intent, SHOOT_IMAGE);
    }

    private File createPictureFile(String part, String ext) throws Exception
    {
        File picturesDir= getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String name = part + Calendar.getInstance().getTimeInMillis() + ext;
        File pictureFile = new File(picturesDir, name);
        pictureFile.createNewFile();
        return pictureFile;
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
                addImage(cameraPictureUri);
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
                        int itemCount = clipData.getItemCount();
                        for (int i = 0; i < itemCount; i++)
                        {
                            Log.i(TAG, "Processing " + (i + 1) + " out of " + itemCount);
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

        MatOfRect facesMat = faceDetector.detect(imageMat);
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

        Log.i(TAG, "Face detected, adding to dataset");

        Rect faceRect = faces[0];

        FaceData fd = new FaceData();
        fd.setFaceMat(imageMat.submat(faceRect));
        fd.setFeatures(extractor.extract(fd.getFaceMat()));

        faceData.add(fd);
        faceDataAdapter.notifyDataSetChanged();
    }
}
