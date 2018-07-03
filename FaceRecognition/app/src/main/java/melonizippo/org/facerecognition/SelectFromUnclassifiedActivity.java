package melonizippo.org.facerecognition;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import melonizippo.org.facerecognition.database.FaceData;
import melonizippo.org.facerecognition.database.FaceDatabaseStorage;

public class SelectFromUnclassifiedActivity extends AppCompatActivity {

    private final static String TAG = "SelectFromUnclassifiedActivity";

    private Map<Integer, FaceData> unclassifiedFaces;
    private UnclassifiedFacesAdapter unclassifiedFacesAdapter;
    List<Integer> idIndexMapping = new ArrayList<>();

    private GridView previewsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_from_unclassified);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Setup grid view
        unclassifiedFaces = FaceDatabaseStorage.getFaceDatabase().unclassifiedFaces;
        idIndexMapping.addAll(unclassifiedFaces.keySet());
        unclassifiedFacesAdapter = new UnclassifiedFacesAdapter(unclassifiedFaces);
        previewsView = findViewById(R.id.previewsView);
        previewsView.setAdapter(unclassifiedFacesAdapter);

        previewsView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        previewsView.setMultiChoiceModeListener(new MultiChoiceModeListener());

        //Setup listeners
        Button selectAllButton = findViewById(R.id.selectAllButton);
        selectAllButton.setOnClickListener((view) -> selectAll());

        Button deselectAllButton = findViewById(R.id.deselectAllButton);
        deselectAllButton.setOnClickListener((view) -> deselectAll());

        Button addSelectedButton = findViewById(R.id.addSelectedButton);
        addSelectedButton.setOnClickListener(view -> commitSelection());
    }

    private void selectAll()
    {
        for(int i = 0; i < previewsView.getCount(); i++)
            previewsView.setItemChecked(i, true);
    }

    private void deselectAll()
    {
        for(int i = 0; i < previewsView.getCount(); i++)
            previewsView.setItemChecked(i, false);
    }

    private void commitSelection()
    {
        StringBuilder s = new StringBuilder("Selected items: ");

        SparseBooleanArray checkedItemPositions = previewsView.getCheckedItemPositions();
        List<Integer> selectedIds = new ArrayList<>();

        for(int i = 0; i < unclassifiedFaces.keySet().size(); ++i)
        {
            if(checkedItemPositions.get(i))
            {
                s.append(i).append(", ");
                selectedIds.add(idIndexMapping.get(i));
            }
        }
        s.delete(s.length() - 2, s.length());
        Log.i(TAG, s.toString());

        int[] selectedIdsArray = new int[selectedIds.size()];
        for(int i = 0; i < selectedIds.size(); i++)
            selectedIdsArray[i] = selectedIds.get(i);

        sendBackResult(selectedIdsArray);
    }

    private void sendBackResult(int[] selectedIDs)
    {
        Intent data = new Intent();
        data.putExtra("selectedIDs", selectedIDs);
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
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        //save checked state
    }

    public class UnclassifiedFacesAdapter extends BaseAdapter
    {
        Map<Integer, FaceData> unclassifiedFaces;

        UnclassifiedFacesAdapter(Map<Integer, FaceData> unclassifiedFaces)
        {
            this.unclassifiedFaces = unclassifiedFaces;
        }

        @Override
        public int getCount()
        {
            return unclassifiedFaces.size();
        }

        @Override
        public Object getItem(int position)
        {
            return unclassifiedFaces.get(idIndexMapping.get(position));
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

            UnclassifiedFaceView faceView = (convertView == null) ?
                    new UnclassifiedFaceView(SelectFromUnclassifiedActivity.this) : (UnclassifiedFaceView) convertView;
            faceView.setContent(fd.toBitmap());
            return faceView;
        }
    }
}
