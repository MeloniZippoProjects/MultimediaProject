package melonizippo.org.facerecognition;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import melonizippo.org.facerecognition.database.FaceData;
import melonizippo.org.facerecognition.database.FaceDatabase;
import melonizippo.org.facerecognition.database.FaceDatabaseStorage;
import melonizippo.org.facerecognition.database.Identity;
import melonizippo.org.facerecognition.deep.DNNExtractor;
import melonizippo.org.facerecognition.deep.Parameters;
import melonizippo.org.facerecognition.facerecognition.FaceDetector;
import melonizippo.org.facerecognition.facerecognition.KNNClassifier;

public class AddUnknownIdentityActivity extends AppCompatActivity {

    private final static String TAG = "AddUnknownIdentityActivity";

    //private List<FaceData> faceDataset = new ArrayList<>();
    private static FaceDataAdapter faceDataAdapter;

    private FaceDetector faceDetector;
    private DNNExtractor extractor;
    private KNNClassifier knnClassifier;

    private GridView previewsView;

    private boolean isDefaultLabel = true;
    private TextInputEditText labelField;

    private static final String LABEL_TEXT_KEY = "identity_label_text";
    private static final String DATASET_KEY = "face_dataset";
    private static final String IS_DEFAULT_LABEL_KEY = "is_default_label";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_unknown_identity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        labelField = findViewById(R.id.identityLabelField);

        //Load saved state
        if(savedInstanceState != null)
        {
            labelField.setText(savedInstanceState.getString(LABEL_TEXT_KEY));
            isDefaultLabel = savedInstanceState.getBoolean(IS_DEFAULT_LABEL_KEY);
        }

        //Load face recognition references
        /*FaceRecognitionApp app = (FaceRecognitionApp) getApplication();
        faceDetector = app.faceDetector;
        extractor = app.extractor;
        knnClassifier = app.knnClassifier;
        */

        //Setup grid view
        List<FaceData> faceDataset = new ArrayList<>(FaceDatabaseStorage.getFaceDatabase().uncategorizedData.values());
        faceDataAdapter = new FaceDataAdapter(faceDataset, getApplicationContext());
        previewsView = findViewById(R.id.previewsView);
        previewsView.setAdapter(faceDataAdapter);


        previewsView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        previewsView.setMultiChoiceModeListener(new MultiChoiceModeListener());

        /*
        //this should reset selection
        Button clearFormButton = findViewById(R.id.clearFormButton);
        clearFormButton.setOnClickListener((view) -> clearForm());

        //this clears placeholder text
        labelField.setOnClickListener(view -> clearPlaceholderText());
        labelField.setOnFocusChangeListener((view, l) -> clearPlaceholderText());

        //this should commit new identity
        Button saveIdentityButton = findViewById(R.id.saveIdentityButton);
        saveIdentityButton.setOnClickListener(view -> commitAddIdentity());
        */
    }

    public class MultiChoiceModeListener implements
            GridView.MultiChoiceModeListener {
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("Select Items");
            mode.setSubtitle("One item selected");
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
        }

        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            int selectCount = previewsView.getCheckedItemCount();
            switch (selectCount) {
                case 1:
                    mode.setSubtitle("One item selected");
                    break;
                default:
                    mode.setSubtitle("" + selectCount + " items selected");
                    break;
            }

            View clickedImage = previewsView.getChildAt(position);
            if(checked)
                clickedImage.setBackgroundColor(Color.parseColor("#669df4"));
            else
                clickedImage.setBackgroundColor(Color.parseColor("#ffffff"));
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(LABEL_TEXT_KEY, labelField.getText().toString());
        outState.putBoolean(IS_DEFAULT_LABEL_KEY, isDefaultLabel);
    }


    /*
    private void commitAddIdentity()
    {
        Identity identity = new Identity();
        identity.label = ((TextInputEditText)findViewById(R.id.identityLabelField)).getText().toString().trim();
        identity.authorized = !((CheckBox)findViewById(R.id.sendAlertCheckbox)).isChecked();
        identity.identityDataset = new ArrayList<>(faceDataset);

        if(!validateIdentity(identity))
            return;
        else
        {
            FaceDatabaseStorage.getFaceDatabase().knownIdentities.add(identity);
            FaceDatabaseStorage.storeToInternalStorage();

            showSnackBar(R.string.info_add_success);
            clearForm();
        }
    }

    private void clearForm()
    {
        labelField.getText().clear();
        faceDataset.clear();
        faceDataAdapter.notifyDataSetChanged();

        Log.i(TAG, "Form cleared");
    }

    private boolean validateIdentity(Identity identity)
    {
        if(identity.label.matches(""))
        {
            showSnackBar(R.string.error_no_name);
            return false;
        }

        FaceDatabase fd = FaceDatabaseStorage.getFaceDatabase();
        if(
                fd.knownIdentities.stream().anyMatch(
                        (dbIdentity) -> dbIdentity.label.matches(identity.label))
                )
        {
            showSnackBar(R.string.error_name_duplicate);
            return false;
        }

        identity.filterDuplicatesFromDataset();

        int datasetCount = identity.identityDataset.size();
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

    private void showSnackBar(int stringId)
    {
        Snackbar errorBar = Snackbar.make(
                findViewById(R.id.addIdentityCoordinatorLayout),
                stringId,
                Snackbar.LENGTH_SHORT);
        errorBar.show();
    }

    private void clearPlaceholderText()
    {
        if(isDefaultLabel)
        {
            labelField.getText().clear();
            isDefaultLabel = false;
        }
    }

    */

}
