package com.jurgendevries.plants;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ListActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    protected List<ParseObject> mPlants;

    @InjectView(R.id.add_plant_button) ImageView mAddPlantButton;
    @InjectView(R.id.progressBarMain) ProgressBar mProgressBarMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
        } else {
            Log.i(TAG, currentUser.getUsername());
            mAddPlantButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, AddPlantActivity.class);
                    startActivity(intent);
                }
            });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Show plants
        mProgressBarMain.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> query = new ParseQuery<>(ParseConstants.CLASS_PLANTS);
        query.whereEqualTo(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> plants, ParseException e) {
                mProgressBarMain.setVisibility(View.INVISIBLE);

                if (e == null) {
                    // succes
                    mPlants = plants;

                    PlantAdapter plantAdapter = new PlantAdapter(
                            MainActivity.this,
                            mPlants
                    );
                    setListAdapter(plantAdapter);

                } else {
                    // error
                }
            }
        });

    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            ParseUser.logOut();
            navigateToLogin();
        }

        return super.onOptionsItemSelected(item);
    }
}
