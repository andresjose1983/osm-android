package com.osm.soft.sf;

import android.app.Application;
import android.content.Intent;

import com.osm.soft.sf.service.CustomerJobService;

/**
 * Created by andres on 08/06/16.
 */
public class OsmApplication extends Application{

    private static OsmApplication sInstance;


    public static OsmApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        startService(new Intent(this, CustomerJobService.class));
    }

}