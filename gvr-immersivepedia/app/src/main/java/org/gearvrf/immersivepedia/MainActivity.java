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

package org.gearvrf.immersivepedia;

import android.os.Bundle;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.immersivepedia.input.TouchPadInput;

public class MainActivity extends GVRActivity
{
	private Main main;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		main = new Main();
		setMain(main, "gvr.xml");
		enableGestureDetector();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		TouchPadInput.input(event);
		return super.onTouchEvent(event);
	}

	@Override
	protected void onPause() {
		super.onPause();
		main.onPause();
	}

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
