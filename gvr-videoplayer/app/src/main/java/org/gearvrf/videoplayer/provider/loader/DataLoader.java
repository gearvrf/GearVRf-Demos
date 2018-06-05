package org.gearvrf.videoplayer.provider.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

public abstract class DataLoader<T> extends AsyncTaskLoader<T> {

    private T mData;
    private ForceLoadContentObserver mContentObserver;

    public DataLoader(Context context) {
        super(context);
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public abstract T loadInBackground();

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(T data) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (data != null) {
                onReleaseResources(data);
            }
        }
        T oldData = mData;
        mData = data;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(data);
        }

        // At this point we can release the resources associated with
        // 'oldData' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldData != null) {
            onReleaseResources(oldData);
        }
    }

    /**
     * Handles a request to startTimer the Loader.
     */
    @Override
    protected void onStartLoading() {

        if (mData != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mData);
        }

        // Start watching for changes in the app data.
        if (mContentObserver == null) {
            registerContentObserver();
        }

        if (takeContentChanged() || mData == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, startTimer a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancelTimer the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancelTimer a load.
     */
    @Override
    public void onCanceled(T data) {
        super.onCanceled(data);

        // At this point we can release the resources associated with 'data'
        // if needed.
        onReleaseResources(data);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'data'
        // if needed.
        if (mData != null) {
            onReleaseResources(mData);
            mData = null;
        }

        // Stop monitoring for changes.
        if (mContentObserver != null) {
            unregisterContentObserver();
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(T data) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }

    private void registerContentObserver() {
        //mContentObserver = new ForceLoadContentObserver();
        //getContext().getContentResolver().registerContentObserver(
        //        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, false, mContentObserver);
    }

    private void unregisterContentObserver() {
        //getContext().getContentResolver().unregisterContentObserver(mContentObserver);
        //mContentObserver = null;
    }
}