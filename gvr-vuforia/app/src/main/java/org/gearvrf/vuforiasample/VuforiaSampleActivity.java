package org.gearvrf.vuforiasample;

import java.util.ArrayList;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.HINT;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.SampleApplicationControl;
import com.vuforia.samples.SampleApplication.SampleApplicationException;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;

import org.gearvrf.GVRActivity;

public class VuforiaSampleActivity extends GVRActivity implements
        SampleApplicationControl {

    private static final String TAG = "gvr-vuforia";
    private VuforiaSampleMain main;

    private SampleApplicationSession vuforiaAppSession;

    private static int VUFORIA_INACTIVE = 0;
    private static int VUFORIA_ACTIVE_INITIALIZED = 1;
    private static int VUFORIA_ACTIVE_PAUSED = 2;
    private static int VUFORIA_ACTIVE_RESUMED = 3;
    private static int VUFORIA_INITIALIZING = 4;

    private static int vuforiaState = VUFORIA_INACTIVE;

    private final int MAX_SIMULTANEOUS_IMAGE_TARGETS = 2;

    private DataSet currentDataset;
    private int currentDatasetSelectionIndex = 0;
    private ArrayList<String> datasetStrings = new ArrayList<String>();

    private boolean extendedTracking = false;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.d(TAG, "onCreate");

        datasetStrings.add("StonesAndChips.xml");

        vuforiaState = VUFORIA_INITIALIZING;
        vuforiaAppSession = new SampleApplicationSession(this);
        vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        main = new VuforiaSampleMain();
        main.vuforiaAppSession = vuforiaAppSession;
        setMain(main, "gvr.xml");
    }

    public static boolean isVuforiaActive() {
        if (vuforiaState == VUFORIA_ACTIVE_INITIALIZED
                || vuforiaState == VUFORIA_ACTIVE_RESUMED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (vuforiaState == VUFORIA_ACTIVE_PAUSED) {
            Log.d(TAG, "onResume");

            try {
                vuforiaAppSession.resumeAR();
                vuforiaState = VUFORIA_ACTIVE_RESUMED;
            } catch (SampleApplicationException e) {
                Log.e(TAG, e.getString());
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (vuforiaState == VUFORIA_ACTIVE_INITIALIZED
                || vuforiaState == VUFORIA_ACTIVE_RESUMED) {
            Log.d(TAG, "onPause");

            try {
                vuforiaAppSession.pauseAR();
                vuforiaState = VUFORIA_ACTIVE_PAUSED;
            } catch (SampleApplicationException e) {
                Log.e(TAG, e.getString());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        main = null;

        if (vuforiaState != VUFORIA_INACTIVE) {
            Log.d(TAG, "onDestroy");

            try {
                vuforiaAppSession.stopAR();
                vuforiaState = VUFORIA_INACTIVE;
            } catch (SampleApplicationException e) {
                Log.e(TAG, e.getString());
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        Log.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);
        vuforiaAppSession.onConfigurationChanged();
    }

    // Initializes AR application components.
    private void initApplicationAR() {
        Vuforia.setHint(HINT.HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS,
                MAX_SIMULTANEOUS_IMAGE_TARGETS);
        vuforiaState = VUFORIA_ACTIVE_INITIALIZED;
    }

    @Override
    public void onInitARDone(SampleApplicationException exception) {

        if (exception == null) {
            initApplicationAR();
            main.isReady = true;

            try {
                vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (SampleApplicationException e) {
                Log.e(TAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (result == false) {
                Log.e(TAG, "Unable to enable continuous autofocus");
            }

            main.onVuforiaInitialized();
        } else {
            Log.e(TAG, exception.getString());
        }
    }

    // To be called to initialize the trackers
    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker tracker;

        // Trying to initialize the image tracker
        tracker = (ObjectTracker)tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null) {
            Log.e(TAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(TAG, "Tracker successfully initialized ");
        }
        return result;
    }

    // To be called to load the trackers' data
    @Override
    public boolean doLoadTrackersData() {
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker imageTracker =  (ObjectTracker)tManager
                .getTracker(ObjectTracker.getClassType());

        if (imageTracker == null) {
            return false;
        }

        if (currentDataset == null) {
            currentDataset = imageTracker.createDataSet();
        }

        if (currentDataset == null) {
            return false;
        }

        if (!currentDataset.load(
                datasetStrings.get(currentDatasetSelectionIndex),
                STORAGE_TYPE.STORAGE_APPRESOURCE)) {
            return false;
        }

        if (!imageTracker.activateDataSet(currentDataset)) {
            return false;
        }

        int numTrackables = currentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++) {
            Trackable trackable = currentDataset.getTrackable(count);

            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(TAG, "UserData:Set the following user data "
                    + (String) trackable.getUserData());
        }

        return true;
    }

    // To be called to start tracking with the initialized trackers and their
    // loaded data
    @Override
    public boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        boolean result = true;

        ObjectTracker imageTracker = (ObjectTracker)TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (imageTracker != null) {
            imageTracker.start();
        }

        return result;
    }

    // To be called to stop the trackers
    @Override
    public boolean doStopTrackers() {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        if (isVuforiaActive()) {
            ObjectTracker imageTracker = (ObjectTracker)TrackerManager.getInstance().getTracker(
                    ObjectTracker.getClassType());
            if (imageTracker != null)
                imageTracker.stop();
        }
        
        return result;
    }

    // To be called to destroy the trackers' data
    @Override
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        if (isVuforiaActive()) {
            TrackerManager tManager = TrackerManager.getInstance();
            ObjectTracker imageTracker = (ObjectTracker)tManager
                    .getTracker(ObjectTracker.getClassType());
            if (imageTracker == null)
                return false;

            if (currentDataset != null && currentDataset.isActive()) {
                if (imageTracker.getActiveDataSet().equals(currentDataset)
                        && !imageTracker.deactivateDataSet(currentDataset)) {
                    result = false;
                } else if (!imageTracker.destroyDataSet(currentDataset)) {
                    result = false;
                }

                currentDataset = null;
            }
        }
        return result;
    }

    // To be called to deinitialize the trackers
    @Override
    public boolean doDeinitTrackers() {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        if (isVuforiaActive()) {
            TrackerManager tManager = TrackerManager.getInstance();
            tManager.deinitTracker(ObjectTracker.getClassType());
        }

        return result;
    }

    boolean isExtendedTrackingActive() {
        return extendedTracking;
    }

    @Override
    public void onVuforiaUpdate(State s) {
        // Update in onStep()
    }
}
