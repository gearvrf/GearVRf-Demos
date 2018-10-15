/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.arpet;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.arpet.constant.ArPetObjectType;
import org.gearvrf.arpet.manager.connection.PetConnectionManager;
import org.gearvrf.arpet.service.share.PlayerSceneObject;
import org.gearvrf.arpet.service.share.SharedMixedReality;
import org.gearvrf.mixedreality.IMRCommon;

import java.util.ArrayList;
import java.util.List;

public class PetContext {
    private final GVRActivity mActivity;
    private final HandlerThread mHandlerThread;
    private final Handler mHandler;
    private final Runnable mPauseTask;
    private boolean mPaused;
    private long mResumeTime;
    private GVRContext mGvrContext;
    private IMRCommon mMixedReality;
    private List<OnPetContextListener> mOnPetContextListeners = new ArrayList<>();
    private PlayerSceneObject mPlayer;

    public PetContext(GVRActivity activity) {
        mActivity = activity;
        mHandlerThread = new HandlerThread("arpet-main");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mPaused = true;
        mResumeTime = 0;

        mPauseTask = new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (mPauseTask) {
                        mPauseTask.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void init(GVRContext context) {
        mGvrContext = context;
        PetConnectionManager.getInstance().init(this);
        mMixedReality = new SharedMixedReality(this);
        mMixedReality.resume();

        mPlayer = new PlayerSceneObject(mGvrContext);
        ((SharedMixedReality) mMixedReality).registerSharedObject(mPlayer, ArPetObjectType.PLAYER);

        // FIXME: Workaround to
        // You may only use GestureDetector constructor from a {@link android.os.Looper} thread.
        BallThrowHandler.getInstance(this);
    }

    public GVRActivity getActivity() {
        return mActivity;
    }

    public GVRContext getGVRContext() {
        return mGvrContext;
    }

    public IMRCommon getMixedReality() {
        return mMixedReality;
    }

    public GVRScene getMainScene() {
        return mGvrContext.getMainScene();
    }

    public PlayerSceneObject getPlayer() {
        return mPlayer;
    }

    public long getResumeTime() {
        return mResumeTime;
    }

    public boolean runOnPetThread(Runnable r) {
        return mHandler.post(r);
    }

    public boolean runDelayedOnPetThread(Runnable r, long delayMillis) {
        return mHandler.postDelayed(r, delayMillis);
    }

    public void removeTask(Runnable r) {
        mHandler.removeCallbacks(r);
    }

    void pause() {
        mPaused = true;
        synchronized (mPauseTask) {
            mHandler.postAtFrontOfQueue(mPauseTask);
        }
    }

    void resume() {
        mPaused = false;
        mResumeTime = SystemClock.uptimeMillis();
        synchronized (mPauseTask) {
            mHandler.removeCallbacks(mPauseTask);
            mPauseTask.notify();
        }
    }

    public boolean isPaused() {
        return mPaused;
    }

    public void addOnPetContextListener(OnPetContextListener listener) {
        removeOnPetContextListener(listener);
        mOnPetContextListeners.add(listener);
    }

    public void removeOnPetContextListener(OnPetContextListener listener) {
        mOnPetContextListeners.remove(listener);
    }

    public void notifyActivityResult(int requestCode, int resultCode, Intent data) {
        for (OnPetContextListener mOnPetContextListener : mOnPetContextListeners) {
            mOnPetContextListener.onActivityResult(requestCode, resultCode, data);
        }
    }

    public interface OnPetContextListener {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }
}
