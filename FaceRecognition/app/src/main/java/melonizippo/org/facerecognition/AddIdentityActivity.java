package melonizippo.org.facerecognition;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
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
import melonizippo.org.facerecognition.deep.Parameters;
import melonizippo.org.facerecognition.facerecognition.FaceDetector;
import melonizippo.org.facerecognition.facerecognition.KNNClassifier;

public class AddIdentityActivity extends AppCompatActivity
{
    private static final String TAG = "AddIdentityActivity";
    private static final int PICK_IMAGE = 1;
    private static final int SHOOT_IMAGE = 2;
    private static final int PICK_IMAGE_MULTIPLE = 3;


    private static final int MAX_DIMENSION = Parameters.MAX_DIMENSION;

    private boolean isDefaultLabel = true;

    private File cameraPictureFile;
    private Uri cameraPictureUri;

    private List<FaceData> faceDataset = new ArrayList<>();
    private static FaceDataAdapter faceDataAdapter;

    private FaceDetector faceDetector;
    private DNNExtractor extractor;
    private KNNClassifier knnClassifier;

    private TextInputEditText labelField;

    private static final String LABEL_TEXT_KEY = "identity_label_text";
    private static final String DATASET_KEY = "face_dataset";
    private static final String IS_DEFAULT_LABEL_KEY = "is_default_label";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_identity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        labelField = findViewById(R.id.identityLabelField);

        //Load saved state
        if(savedInstanceState != null)
        {
            labelField.setText(savedInstanceState.getString(LABEL_TEXT_KEY));
            isDefaultLabel = savedInstanceState.getBoolean(IS_DEFAULT_LABEL_KEY);
            for(Object faceDataObject : (Object[]) savedInstanceState.getSerializable(DATASET_KEY))
            {
                FaceData faceData = (FaceData) faceDataObject;
                faceDataset.add(faceData);
            }
        }

        //Load face recognition references
        FaceRecognitionApp app = (FaceRecognitionApp) getApplication();
        faceDetector = app.faceDetector;
        extractor = app.extractor;
        knnClassifier = app.knnClassifier;

        //Setup grid view
        faceDataAdapter = new FaceDataAdapter(faceDataset, getApplicationContext());
        GridView previewsView = findViewById(R.id.previewsView);
        previewsView.setAdapter(faceDataAdapter);

        //Setup listeners
        FloatingActionButton addPhotosButton = findViewById(R.id.addPhotosButton);
        addPhotosButton.setOnClickListener((view) -> showPictureDialog());

        FloatingActionButton clearFormButton = findViewById(R.id.clearFormButton);
        clearFormButton.setOnClickListener((view) -> clearForm());

        labelField.setOnClickListener(view -> clearPlaceholderText());
        labelField.setOnFocusChangeListener((view, l) -> clearPlaceholderText());

        final Button commitButton = findViewById(R.id.commitButton);
        commitButton.setOnClickListener(view -> commitAddIdentity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(LABEL_TEXT_KEY, labelField.getText().toString());
        outState.putBoolean(IS_DEFAULT_LABEL_KEY, isDefaultLabel);
        outState.putSerializable(DATASET_KEY, faceDataset.toArray());
    }

    private void clearPlaceholderText()
    {
        if(isDefaultLabel)
        {
            labelField.getText().clear();
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
        identityEntry.identityDataset = new ArrayList<>(faceDataset);

        if(!validateIdentity(identityEntry))
            return;
        else
        {
            FaceDatabaseStorage.getFaceDatabase().knownIdentities.add(identityEntry);
            FaceDatabaseStorage.store();

            showSnackBar(R.string.info_add_success);
            clearForm();
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

        identityEntry.filterDuplicatesFromDataset();

        int datasetCount = identityEntry.identityDataset.size();
        if(datasetCount < Parameters.MIN_IDENTITY_SAMPLES)
        {
            Resources res = getResources();
            String errorMessage = getString(R.string.error_not_enough_samples, Parameters.MIN_IDENTITY_SAMPLES);
            Snackbar errorBar = Snackbar.make(
                    findViewById(R.id.addIdentityCoordinatorLayout),
                    errorMessage,
                    Snackbar.LENGTH_SHORT);
            errorBar.show();
            return false;
        }

        return true;
    }

    private void clearForm()
    {
        labelField.getText().clear();
        faceDataset.clear();
        faceDataAdapter.notifyDataSetChanged();

        Log.i(TAG, "Form cleared");
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

        imageBitmap = scaleBitmap(imageBitmap);

        Mat imageMat = new Mat();
        Utils.bitmapToMat(imageBitmap, imageMat);


        //Mat resizedMat = imageMat.clone();

        Bitmap testBitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imageMat, testBitmap);

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

        faceDataset.add(fd);
        faceDataAdapter.notifyDataSetChanged();
    }

    private static Bitmap scaleBitmap(Bitmap tmpBitmap) {
        Bitmap imageBitmap;
        if(tmpBitmap.getWidth() > MAX_DIMENSION || tmpBitmap.getHeight() > MAX_DIMENSION)
        {
            double factor;
            int dstWidth;
            int dstHeight;
            if(tmpBitmap.getWidth() >= tmpBitmap.getHeight())
            {
                factor = MAX_DIMENSION / (double) tmpBitmap.getWidth();
                dstWidth = MAX_DIMENSION;
                dstHeight = (int)(tmpBitmap.getHeight() * factor);
            }
            else
            {
                factor = MAX_DIMENSION / (double) tmpBitmap.getHeight();
                dstHeight = MAX_DIMENSION;
                dstWidth = (int)(tmpBitmap.getWidth() * factor);
            }

            imageBitmap = Bitmap.createScaledBitmap(tmpBitmap, dstWidth, dstHeight, false);
        }
        else
        {
            imageBitmap = tmpBitmap;
        }
        return imageBitmap;
    }
}
