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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
    private Map<Integer, FaceData> uncategorizedFaces;
    private static UncategorizedFaceAdapter uncategorizedFaceAdapter;
    private Set<Integer> selectedIds = new TreeSet<>();

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
        uncategorizedFaces = FaceDatabaseStorage.getFaceDatabase().uncategorizedData;
        uncategorizedFaceAdapter = new UncategorizedFaceAdapter(uncategorizedFaces);
        previewsView = findViewById(R.id.previewsView);
        previewsView.setAdapter(uncategorizedFaceAdapter);

        previewsView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        previewsView.setMultiChoiceModeListener(new MultiChoiceModeListener());

        /*
        //this should reset selection
        Button clearFormButton = findViewById(R.id.clearFormButton);
        clearFormButton.setOnClickListener((view) -> clearForm());

        //this clears placeholder text
        labelField.setOnClickListener(view -> clearPlaceholderText());
        labelField.setOnFocusChangeListener((view, l) -> clearPlaceholderText());
        */

        //this should commit new identity
        Button saveIdentityButton = findViewById(R.id.saveIdentityButton);
        saveIdentityButton.setOnClickListener(view -> commitAddIdentity());
    }

    private void commitAddIdentity()
    {
        StringBuilder s = new StringBuilder("Selected items: ");
        for (Integer i :
                selectedIds)
        {
            s.append(i).append(", ");
        }
        s.delete(s.length() - 2, s.length());
        Log.i(TAG, s.toString());
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

        public void onItemCheckedStateChanged(
                ActionMode mode, int position, long id, boolean checked)
        {
            int selectCount = previewsView.getCheckedItemCount();
            switch (selectCount)
            {
                case 1:
                    mode.setSubtitle("One item selected");
                    break;
                default:
                    mode.setSubtitle("" + selectCount + " items selected");
                    break;
            }

            UncategorizedFaceView clickedFaceView = (UncategorizedFaceView) previewsView.getChildAt(position);
            clickedFaceView.setChecked(checked);
            if(checked)
                selectedIds.add(position);
            else
                selectedIds.remove(position);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(LABEL_TEXT_KEY, labelField.getText().toString());
        outState.putBoolean(IS_DEFAULT_LABEL_KEY, isDefaultLabel);
    }

    public class UncategorizedFaceAdapter extends BaseAdapter
    {
        Map<Integer, FaceData> uncategorizedFaces;
        List<Integer> selectedPositions = new ArrayList<>();

        UncategorizedFaceAdapter(Map<Integer, FaceData> uncategorizedFaces)
        {
            this.uncategorizedFaces = uncategorizedFaces;
        }

        @Override
        public int getCount()
        {
            return uncategorizedFaces.size();
        }

        @Override
        public Object getItem(int position)
        {
            return uncategorizedFaces.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent)
        {
            FaceData fd = (FaceData) getItem(position);

            UncategorizedFaceView faceView = (convertView == null) ?
                    new UncategorizedFaceView(AddUnknownIdentityActivity.this) : (UncategorizedFaceView) convertView;
            faceView.setContent(fd.toBitmap());
            faceView.setChecked(AddUnknownIdentityActivity.this.selectedIds.contains(position));
            return faceView;
        }
    }
}
