package com.jurgendevries.plants;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Jurgen on 6-3-2015.
 */
public class PlantsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "oh2dtoyZKzAv4M5vDAvSy6MvoNfG5oDKcbVF40A4", "6HjkY5p1iUxjac64d96ja8U2dvRKh95n5Eugi4t7");
    }
}
