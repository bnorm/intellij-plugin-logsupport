package net.sf.logsupport.android;

import android.util.Log;

public class DroidLogger {
    private static final String MY_TAG = "MyTag";

    public static void main(String[] args) {

        if (Log.isLoggable(MY_TAG, Log.DEBUG)) { Log.d(MY_TAG, "fsdfsdf"); }

        Log.i(MY_TAG, "asdsadasd");
        boolean verbose = Log.isLoggable(MY_TAG, Log.VERBOSE);
        if (verbose) { Log.v(MY_TAG, "asdasdsadasdsd"); }

        if (verbose) { Log.v(MY_TAG, "asdasd"); }
    }
}
