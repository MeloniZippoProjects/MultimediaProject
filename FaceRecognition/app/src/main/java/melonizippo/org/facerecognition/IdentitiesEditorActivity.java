package melonizippo.org.facerecognition;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import melonizippo.org.facerecognition.database.FaceDatabase;
import melonizippo.org.facerecognition.database.FaceDatabaseStorage;
import melonizippo.org.facerecognition.database.IdentityEntry;

public class IdentitiesEditorActivity extends AppCompatActivity
{


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identities_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.addIdentity);
        fab.setOnClickListener((view) -> {
                Intent intent = new Intent(IdentitiesEditorActivity.this, AddIdentityActivity.class);
                startActivity(intent);
        });

        TextView textView = findViewById(R.id.TextView);

        updateText();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updateText();
    }

    private void updateText()
    {
        FaceDatabase db = FaceDatabaseStorage.getFaceDatabase();
        TextView textView = findViewById(R.id.TextView);
        String text = db.knownIdentities.size() + " known identities:\n";
        for(IdentityEntry ie : db.knownIdentities)
        {
            text += "Label: " + ie.label +
                    ", authorized: " + ie.authorized +
                    ", photos: " + ie.identityDataset.size() + "\n";
        }
        textView.setText(text);
    }
}
