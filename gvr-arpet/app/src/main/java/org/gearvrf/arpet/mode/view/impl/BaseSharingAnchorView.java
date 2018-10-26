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

package org.gearvrf.arpet.mode.view.impl;

import android.view.View;

import org.gearvrf.arpet.mode.view.ISharingAnchorView;

abstract class BaseSharingAnchorView implements ISharingAnchorView {

    private View mView;
    private ShareAnchorView2 mViewController;

    public BaseSharingAnchorView(View view, ShareAnchorView2 viewController) {
        this.mView = view;
        this.mViewController = viewController;
    }

    @Override
    public void show() {
        mViewController.showView(this);
    }

    View getView() {
        return mView;
    }
}
