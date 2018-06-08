package melonizippo.org.facerecognition;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addIdentity);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        updateText();
    }

    private void updateText()
    {
        FaceDatabase db = FaceDatabaseStorage.getFaceDatabase();
        TextView textView = findViewById(R.id.TextView);
        String text = db.KnownIdentities.size() + " known identities:\n";
        for(IdentityEntry ie : db.KnownIdentities)
        {
            text += "Label: " + ie.Label +
                    ", authorized: " + ie.Authorized +
                    ", photos: " + ie.IdentityDataset.size() + "\n";
        }
        textView.setText(text);
    }

}
