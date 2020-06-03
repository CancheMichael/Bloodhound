/**
 * Author: Michael Canche
 * Dartmouth College, Spring 2020, Professor Campbell
 */
package edu.dartmouth.cs65.searchandrescue.code.structures;

import android.app.Application;
import android.content.Context;

//Used to get context in fragments
public class SearchAndRescueContext extends Application {
    private static SearchAndRescueContext instance;

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
            Context context = getApplicationContext();
    }

    public static Context getContext(){ return instance; }


}
