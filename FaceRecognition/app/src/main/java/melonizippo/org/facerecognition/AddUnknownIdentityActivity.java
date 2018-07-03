package melonizippo.org.facerecognition;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import melonizippo.org.facerecognition.database.FaceData;
import melonizippo.org.facerecognition.database.FaceDatabaseStorage;
import melonizippo.org.facerecognition.deep.DNNExtractor;
import melonizippo.org.facerecognition.facerecognition.FaceDetector;
import melonizippo.org.facerecognition.facerecognition.KNNClassifier;

public class AddUnknownIdentityActivity extends AppCompatActivity {

    private final static String TAG = "AddUnknownIdentityActivity";

    //private List<FaceData> faceDataset = new ArrayList<>();
    private Map<Integer, FaceData> uncategorizedFaces;
    private static UncategorizedFaceAdapter uncategorizedFaceAdapter;
    private Set<Integer> selectedPositions = new TreeSet<>();
    List<Integer> idIndexMapping = new ArrayList<>();

    private GridView previewsView;

    private static final String LABEL_TEXT_KEY = "identity_label_text";
    private static final String DATASET_KEY = "face_dataset";
    private static final String IS_DEFAULT_LABEL_KEY = "is_default_label";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_unknown_identity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Load saved state
        if(savedInstanceState != null)
        {
            //add checked state
        }

        //Setup grid view
        uncategorizedFaces = FaceDatabaseStorage.getFaceDatabase().uncategorizedData;
        idIndexMapping.addAll(uncategorizedFaces.keySet());
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

        SparseBooleanArray checkedItemPositions = previewsView.getCheckedItemPositions();
        List<Integer> selectedIds = new ArrayList<>();

        for(int i = 0; i < uncategorizedFaces.keySet().size(); ++i)
        {
            if(checkedItemPositions.get(i))
            {
                s.append(i).append(", ");
                selectedIds.add(idIndexMapping.get(i));
            }
        }
        s.delete(s.length() - 2, s.length());
        Log.i(TAG, s.toString());

        int[] result = new int[selectedIds.size()];
        for(int i = 0; i < selectedIds.size(); i++)
            result[i] = selectedIds.get(i);

        sendBackResult(result);
    }

    private void sendBackResult(int[] result)
    {
        Intent data = new Intent();
        data.putExtra("selectedIDs", result);
        setResult(RESULT_OK, data);
        finish();
    }

    public class MultiChoiceModeListener implements
            GridView.MultiChoiceModeListener {
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
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

            //UncategorizedFaceView clickedFaceView = (UncategorizedFaceView) previewsView.getItemAtPosition(position);
            //clickedFaceView.setChecked(checked);
            if(checked)
                selectedPositions.add(position);
            else
                selectedPositions.remove(position);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        //save checked state
    }

    public class UncategorizedFaceAdapter extends BaseAdapter
    {
        Map<Integer, FaceData> uncategorizedFaces;

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
            return uncategorizedFaces.get(idIndexMapping.get(position));
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
            faceView.setChecked(AddUnknownIdentityActivity.this.selectedPositions.contains(position));
            return faceView;
        }
    }
}
