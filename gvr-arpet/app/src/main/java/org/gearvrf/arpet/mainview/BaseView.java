/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.arpet.mainview;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

public abstract class BaseView implements IView {

    private View mView;
    private IViewController mViewController;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private OnViewShownCallback mOnViewShownCallback;

    public BaseView(View view, IViewController viewController) {
        this.mView = view;
        this.mViewController = viewController;
    }

    @Override
    public void show() {
        mViewController.showView(this);
    }

    @Override
    public void show(OnViewShownCallback callback) {
        mOnViewShownCallback = callback;
        mViewController.showView(this);
    }

    void onShown() {
        if (mOnViewShownCallback != null) {
            mOnViewShownCallback.onShown();
        }
    }

    public View getView() {
        return mView;
    }

    public void runOnUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }

    public void runOnUiThread(Runnable runnable, long delay) {
        mHandler.postDelayed(runnable, delay);
    }
}
