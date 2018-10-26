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

import org.gearvrf.arpet.R;
import org.gearvrf.arpet.mode.view.ILetsStartView;

class LetsStartView extends BaseSharingAnchorView implements ILetsStartView {

    private View mBackButton;
    private View mHostButton;
    private View mGuestButton;

    public LetsStartView(View view, ShareAnchorView2 controller) {
        super(view, controller);
        this.mBackButton = view.findViewById(R.id.button_back);
        this.mHostButton = view.findViewById(R.id.button_host);
        this.mGuestButton = view.findViewById(R.id.button_guest);
    }

    @Override
    public void setBackClickListener(View.OnClickListener listener) {
        mBackButton.setOnClickListener(listener);
    }

    @Override
    public void setHostClickListener(View.OnClickListener listener) {
        mHostButton.setOnClickListener(listener);
    }

    @Override
    public void setGuestClickListener(View.OnClickListener listener) {
        mGuestButton.setOnClickListener(listener);
    }
}
