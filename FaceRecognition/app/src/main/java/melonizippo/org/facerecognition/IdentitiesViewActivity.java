package melonizippo.org.facerecognition;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import melonizippo.org.facerecognition.database.FaceDatabase;
import melonizippo.org.facerecognition.database.FaceDatabaseStorage;
import melonizippo.org.facerecognition.database.Identity;

public class IdentitiesViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private static final String EXPORTED_DATABASE_NAME = "FaceRecognition_database.dat";
    private static final String TAG = "IdentitiesEditor";
    private DrawerLayout drawerLayout;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private IdentityEntryAdapter identityEntryAdapter;
    private List<Identity> identitiesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identities_view);

        verifyStoragePermissions(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        //Setup list view
        identityEntryAdapter = new IdentityEntryAdapter(identitiesList, getApplicationContext());
        ListView identitiesView = findViewById(R.id.identitiesView);
        identitiesView.setAdapter(identityEntryAdapter);

        updateView();

        FloatingActionButton addIdentityButton = findViewById(R.id.addIdentity);
        addIdentityButton.setOnClickListener((view) -> {
                Intent intent = new Intent(IdentitiesViewActivity.this, AddIdentityActivity.class);
                startActivity(intent);
        });

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        updateText();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        boolean result = true;
        switch (item.getItemId())
        {
            case R.id.exportItem:
                result = exportDatabase();
                break;
            case R.id.importItem:
                result = importDatabase();
                break;
            case R.id.clearDatabase:
                clearDatabase();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        Snackbar resultSnackbar;
        if(result)
            resultSnackbar = Snackbar.make(findViewById(R.id.identitiesView), "Operation completed", Snackbar.LENGTH_SHORT);
        else
            resultSnackbar = Snackbar.make(findViewById(R.id.identitiesView), "Operation failed", Snackbar.LENGTH_SHORT);

        resultSnackbar.show();
        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updateText();
        updateView();
    }

    private void updateView()
    {
        FaceDatabase db = FaceDatabaseStorage.getFaceDatabase();
        identitiesList.clear();
        identitiesList.addAll(db.knownIdentities);
        identityEntryAdapter.notifyDataSetChanged();
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private boolean exportDatabase()
    {
        File exportedDatabase = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), EXPORTED_DATABASE_NAME);

        try
        {
            if (!exportedDatabase.exists())
                exportedDatabase.createNewFile();
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Can't create exported database file");
        }

        try(
                FileOutputStream fos = new FileOutputStream(exportedDatabase);
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        )
        {
            oos.writeObject(FaceDatabaseStorage.getFaceDatabase());
            Log.i(TAG, "Database exported");
        }
        catch(Exception ex)
        {
            Log.e(TAG, "Failed to export database");
        }

        return true;
    }

    private boolean importDatabase()
    {
        File exportedDatabase = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), EXPORTED_DATABASE_NAME);

        try(
                FileInputStream fis = new FileInputStream(exportedDatabase);
                ObjectInputStream ois = new ObjectInputStream(fis)
        )
        {
            FaceDatabaseStorage.setFaceDatabase((FaceDatabase) ois.readObject());
            FaceDatabaseStorage.storeToInternalStorage();

            Log.i(TAG, "Database imported");
        }
        catch(Exception ex)
        {
            Log.e(TAG, "Failed to import database");
        }

        updateText();
        updateView();
        return true;
    }

    private void clearDatabase()
    {
        FaceDatabaseStorage.clear();
        updateText();
        updateView();
    }

    private void updateText()
    {
        FaceDatabase db = FaceDatabaseStorage.getFaceDatabase();
        TextView textView = findViewById(R.id.TextView);
        StringBuilder text = new StringBuilder(db.knownIdentities.size() + " known identities:\n");
        for(Identity ie : db.knownIdentities)
        {
            text.append("Label: ").append(ie.label)
                .append(", authorized: ").append(ie.authorized)
                .append(", photos: ").append(ie.identityDataset.size())
                .append("\n");
        }
        text.append("Unclassified samples: ").append(db.unclassifiedFaces.size());
        textView.setText(text.toString());
    }
}
