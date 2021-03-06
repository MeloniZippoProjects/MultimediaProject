package melonizippo.org.facerecognition;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.icu.util.Calendar;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;


import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import melonizippo.org.facerecognition.database.FaceData;
import melonizippo.org.facerecognition.database.FaceDatabase;
import melonizippo.org.facerecognition.database.FaceDatabaseStorage;
import melonizippo.org.facerecognition.database.Identity;
import melonizippo.org.facerecognition.deep.DNNExtractor;
import melonizippo.org.facerecognition.facerecognition.FaceDetector;
import melonizippo.org.facerecognition.facerecognition.KNNClassifier;

public class AddIdentityActivity extends AppCompatActivity
{
    private static final String TAG = "AddIdentityActivity";

    private static final int PICK_IMAGE = 1;
    private static final int PICK_IMAGE_MULTIPLE = 2;
    private static final int PICK_VIDEO = 3;
    private static final int SHOOT_IMAGE = 4;
    private static final int SHOOT_VIDEO = 5;
    private static final int PICK_IMAGE_MULTIPLE_UNCLASSIFIED = 6;

    Thread workerThread;

    private boolean isDefaultLabel = true;

    private File cameraPictureFile;
    private Uri cameraPictureUri;

    private List<FaceData> faceDataset = new ArrayList<>();
    private static FaceDataAdapter faceDataAdapter;

    private FaceDetector faceDetector;
    private DNNExtractor extractor;

    private TextInputEditText labelField;
    ProgressBar progressBar;


    private static final String LABEL_TEXT_KEY = "identity_label_text";
    private static final String DATASET_KEY = "face_dataset";
    private static final String IS_DEFAULT_LABEL_KEY = "is_default_label";

    private File faceDatasetFile = null;

    private List<Integer> unclassifiedIdsToRemoveOnCommit = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_identity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        labelField = findViewById(R.id.identityLabelField);
        progressBar = findViewById(R.id.progressBar);

        //Load saved state
        if(savedInstanceState != null)
        {
            labelField.setText(savedInstanceState.getString(LABEL_TEXT_KEY));
            isDefaultLabel = savedInstanceState.getBoolean(IS_DEFAULT_LABEL_KEY);
            String datasetPath = savedInstanceState.getString(DATASET_KEY);
            if(datasetPath != null) {
                faceDatasetFile = new File(datasetPath);
                restoreFaceDataset();
            }
        }

        //get tmp file to save faceDataset
        if(faceDatasetFile == null) {
            try {
                File outputDir = getCacheDir(); // context being the Activity pointer
                faceDatasetFile = File.createTempFile("prefix", "extension", outputDir);
            } catch (IOException ex) {
                Log.e(TAG, "Cannot create file to store face dataset");
            }
        }

        //Load face recognition references
        FaceRecognitionApp app = (FaceRecognitionApp) getApplication();
        faceDetector = app.faceDetector;
        extractor = app.extractor;

        //Setup grid view
        faceDataAdapter = new FaceDataAdapter(faceDataset, getApplicationContext());
        GridView previewsView = findViewById(R.id.previewsView);
        previewsView.setAdapter(faceDataAdapter);

        //Setup listeners
        labelField.setOnClickListener(view -> clearPlaceholderText());
        labelField.setOnFocusChangeListener((view, l) -> clearPlaceholderText());

        Button addSamplesButton = findViewById(R.id.addSamplesButton);
        addSamplesButton.setOnClickListener((view) -> showPictureDialog());

        Button clearFormButton = findViewById(R.id.clearFormButton);
        clearFormButton.setOnClickListener((view) -> clearForm());

        Button saveIdentityButton = findViewById(R.id.saveIdentityButton);
        saveIdentityButton.setOnClickListener(view -> commitAddIdentity());
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(workerThread != null && workerThread.isAlive())
            workerThread.interrupt();
    }

    private void saveFaceDataset()
    {
        try(FileOutputStream fileOutputStream = new FileOutputStream(faceDatasetFile))
        {
            try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream))
            {
                objectOutputStream.writeObject(faceDataset);
            }
        }
        catch(IOException ex)
        {
            Log.e(TAG, "Cannot write dataset on file");
        }
    }

    private void restoreFaceDataset()
    {
        try(FileInputStream fileInputStream = new FileInputStream(faceDatasetFile))
        {
            try(ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream))
            {
                //noinspection unchecked
                faceDataset = (List<FaceData>) objectInputStream.readObject();
            }
        }
        catch(IOException ex)
        {
            Log.e(TAG, "Cannot read dataset from file");
        }
        catch(ClassNotFoundException ex)
        {
            Log.e(TAG, "Dataset is corrupted");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(LABEL_TEXT_KEY, labelField.getText().toString());
        outState.putBoolean(IS_DEFAULT_LABEL_KEY, isDefaultLabel);
        outState.putString(DATASET_KEY, faceDatasetFile.getPath());
        saveFaceDataset();
    }

    private void clearPlaceholderText()
    {
        if(isDefaultLabel)
        {
            labelField.getText().clear();
            isDefaultLabel = false;
        }
    }


    private void commitAddIdentity()
    {
        if(workerThread != null && workerThread.isAlive())
        {
            showSnackBar("Adding samples, please wait...");
            return;
        }

        Identity identity = new Identity();
        identity.label = ((TextInputEditText)findViewById(R.id.identityLabelField)).getText().toString().trim();
        identity.authorized = !((CheckBox)findViewById(R.id.sendAlertCheckbox)).isChecked();
        identity.identityDataset = new ArrayList<>(faceDataset);

        if (validateIdentity(identity)) {
            FaceDatabaseStorage.getFaceDatabase().knownIdentities.add(identity);
            cleanupUnclassifiedIds();
            FaceDatabaseStorage.storeToInternalStorage();

            showSnackBar(R.string.info_add_success);
            clearForm();
        }
    }

    private boolean validateIdentity(Identity identity)
    {
        if(identity.label.matches(""))
        {
            showSnackBar(R.string.error_no_name);
            return false;
        }

        FaceDatabase fd = FaceDatabaseStorage.getFaceDatabase();
        Optional<Identity> dbIdentityMatch = fd.knownIdentities.stream()
                .filter((dbIdentity) -> dbIdentity.label.matches(identity.label))
                .findAny();

        if(dbIdentityMatch.isPresent())
        {
            askToAddSamples(identity, dbIdentityMatch.get());
            return false;
        }

        identity.filterDuplicatesFromDataset();

        int datasetCount = identity.identityDataset.size();
        if(datasetCount < Parameters.MIN_IDENTITY_SAMPLES)
        {
            String errorMessage = getString(R.string.error_not_enough_samples, Parameters.MIN_IDENTITY_SAMPLES);
            showSnackBar(errorMessage);
            return false;
        }

        return true;
    }

    private void askToAddSamples(Identity identity, Identity dbIdentity)
    {
        new AlertDialog.Builder(this)
                .setTitle("Name already used")
                .setMessage("Do you want to add the samples to the same identity?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    dbIdentity.identityDataset.addAll(identity.identityDataset);
                    dbIdentity.filterDuplicatesFromDataset();
                    cleanupUnclassifiedIds();
                    FaceDatabaseStorage.storeToInternalStorage();

                    showSnackBar(R.string.info_edit_success);
                    clearForm();
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void clearForm()
    {
        if(workerThread != null && workerThread.isAlive())
        {
            workerThread.interrupt();
            try{workerThread.join();}
            catch (InterruptedException ignored){}
        }

        labelField.getText().clear();
        faceDataset.clear();
        faceDataAdapter.notifyDataSetChanged();
        unclassifiedIdsToRemoveOnCommit.clear();
        progressBar.setVisibility(View.GONE);

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

    private void showSnackBar(String string)
    {
        Snackbar errorBar = Snackbar.make(
                findViewById(R.id.addIdentityCoordinatorLayout),
                string,
                Snackbar.LENGTH_SHORT);
        errorBar.show();
    }

    private void showPictureDialog()
    {
        if(workerThread != null && workerThread.isAlive())
        {
            showSnackBar("Already adding samples, please wait...");
            return;
        }

        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photos from gallery",
                "Select video from gallery",
                "Capture photo from camera",
                "Capture video from camera",
                "Select from unclassified"
        };
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) ->
                {
                    switch (which) {
                        case 0:
                            choosePhotosFromGallery();
                            break;
                        case 1:
                            chooseVideoFromGallery();
                            break;
                        case 2:
                            takePhotoFromCamera();
                            break;
                        case 3:
                            takeVideoFromCamera();
                            break;
                        case 4:
                            choosePhotosFromUnclassified();
                            break;
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

    private void chooseVideoFromGallery()
    {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Video"), PICK_VIDEO);
    }

    private void takePhotoFromCamera()
    {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        try
        {
            // place where to store camera taken picture
            cameraPictureFile = this.createPictureFile();
        }
        catch(Exception e)
        {
            Log.v(TAG, "Can't create file to take picture!");
            Toast.makeText(this, "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG).show();
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

    private File createPictureFile() throws Exception
    {
        File picturesDir= getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String name = "picture" + Calendar.getInstance().getTimeInMillis() + ".jpg";
        File pictureFile = new File(picturesDir, name);
        pictureFile.createNewFile();
        return pictureFile;
    }

    private void takeVideoFromCamera()
    {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, SHOOT_VIDEO);
        }
    }

    private void choosePhotosFromUnclassified()
    {
        Intent intent = new Intent(AddIdentityActivity.this, SelectFromUnclassifiedActivity.class);
        if(unclassifiedIdsToRemoveOnCommit.size() > 0)
        {
            int[] filteredIds = new int[unclassifiedIdsToRemoveOnCommit.size()];
            for(int i = 0; i < filteredIds.length; i++)
                filteredIds[i] = unclassifiedIdsToRemoveOnCommit.get(i);

            intent.putExtra("filteredIds", filteredIds);
        }

        startActivityForResult(intent, PICK_IMAGE_MULTIPLE_UNCLASSIFIED);
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
                //Not deleting it makes the shot available for future use
                //cameraPictureFile.delete();
            }
            else if (requestCode == PICK_IMAGE_MULTIPLE && data != null)
            {
                processImages(data);
            }
            else if (requestCode == PICK_IMAGE_MULTIPLE_UNCLASSIFIED && data != null)
            {
                processUnclassifiedImages(data);
            }
            else if ( (requestCode == SHOOT_VIDEO || requestCode == PICK_VIDEO) && data != null)
            {
                processVideo(data);
            }
        }
    }

    private void processUnclassifiedImages(Intent data)
    {
        int[] ids = data.getIntArrayExtra("selectedIDs");
        if(ids.length < 1)
            return;

        Map<Integer, FaceData> unclassifiedFaces = FaceDatabaseStorage.getFaceDatabase().unclassifiedFaces;
        for (int id : ids)
        {
            if(unclassifiedIdsToRemoveOnCommit.contains(id))
                continue;

            FaceData faceData = unclassifiedFaces.get(id);
            faceDataset.add(faceData);
            faceDataAdapter.notifyDataSetChanged();

            //delayed cleanup
            unclassifiedIdsToRemoveOnCommit.add(id);
        }
    }

    private void cleanupUnclassifiedIds()
    {
        Map<Integer, FaceData> unclassifiedFaces = FaceDatabaseStorage.getFaceDatabase().unclassifiedFaces;
        for (Integer id : unclassifiedIdsToRemoveOnCommit)
            unclassifiedFaces.remove(id);
        unclassifiedIdsToRemoveOnCommit.clear();
    }

    private void processImages(Intent data)
    {
        if( data.getData() != null )
        {
            Uri imageUri = data.getData();
            addImage(imageUri);
        }
        else
        {
            if (data.getClipData() != null)
            {
                progressBar.setProgress(0);
                progressBar.setVisibility(View.VISIBLE);

                workerThread = new Thread(() ->
                {
                    ClipData clipData = data.getClipData();
                    int itemCount = clipData.getItemCount();
                    int progressStep = Math.floorDiv(100, itemCount);
                    for (int i = 0; i < itemCount; i++)
                    {
                        if(Thread.interrupted())
                        {
                            Log.i(TAG, "Worker thread interrupted, aborting");
                            return;
                        }

                        Log.i(TAG, "Processing " + (i + 1) + " out of " + itemCount);
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri imageUri = item.getUri();
                        addImage(imageUri);
                        runOnUiThread(() -> progressBar.incrementProgressBy(progressStep));
                    }
                    runOnUiThread(() ->
                    {
                        progressBar.setProgress(100);
                        progressBar.setVisibility(View.GONE);
                    });
                });
                workerThread.start();
            }
        }
    }

    private void processVideo(Intent data)
    {
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

        workerThread = new Thread(() ->
        {
            Uri videoUri = data.getData();
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, videoUri);
            String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long duration = Long.parseLong(durationString) * 1000;
            long step = duration / (Parameters.VIDEO_FRAMES_TO_EXTRACT + 1);

            int progressStep = (int) Math.floorDiv(100, Parameters.VIDEO_FRAMES_TO_EXTRACT);

            for(long time = step; time < duration; time += step)
            {
                if(Thread.interrupted())
                {
                    Log.i(TAG, "Worker thread interrupted, aborting");
                    return;
                }

                Log.i(TAG, "Processing frame at " + time + " out of " + duration);
                Bitmap frame = retriever.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST);
                addImage(frame);
                runOnUiThread(() -> progressBar.incrementProgressBy(progressStep));
            }
            runOnUiThread(() ->
            {
                progressBar.setProgress(100);
                progressBar.setVisibility(View.GONE);
            });
        });
        workerThread.start();
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

        addImage(imageBitmap);
    }

    private void addImage(Bitmap imageBitmap)
    {
        imageBitmap = scaleBitmap(imageBitmap);
        Utils.bitmapToMat(imageBitmap, imageMat);
        addImage(imageMat);
    }

    private Mat imageMat = new Mat();
    private void addImage(Mat imageMat)
    {
        MatOfRect facesMat = faceDetector.detect(imageMat);
        Rect[] faces = facesMat.toArray();

        if(faces.length < 1)
        {
            Log.i(TAG, "No face detected");
            return;
        }

        Log.i(TAG, "Face detected, adding to dataset");

        Rect faceRect = faces[0];

        FaceData fd = new FaceData();
        fd.setFaceMat(imageMat.submat(faceRect));
        fd.setFeatures(extractor.extract(fd.getFaceMat()));

        faceDataset.add(fd);
        runOnUiThread(() -> faceDataAdapter.notifyDataSetChanged());
    }

    private static Bitmap scaleBitmap(Bitmap tmpBitmap) {
        Bitmap imageBitmap;
        if(tmpBitmap.getWidth() > Parameters.MAX_DIMENSION || tmpBitmap.getHeight() > Parameters.MAX_DIMENSION)
        {
            double factor;
            int dstWidth;
            int dstHeight;
            if(tmpBitmap.getWidth() >= tmpBitmap.getHeight())
            {
                factor = Parameters.MAX_DIMENSION / (double) tmpBitmap.getWidth();
                dstWidth = Parameters.MAX_DIMENSION;
                dstHeight = (int)(tmpBitmap.getHeight() * factor);
            }
            else
            {
                factor = Parameters.MAX_DIMENSION / (double) tmpBitmap.getHeight();
                dstHeight = Parameters.MAX_DIMENSION;
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
