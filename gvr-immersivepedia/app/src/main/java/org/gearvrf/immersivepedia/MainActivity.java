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

import org.gearvrf.GVRActivity;
import org.gearvrf.immersivepedia.input.TouchPadInput;
import org.gearvrf.io.GVRTouchPadGestureListener;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class MainActivity extends GVRActivity
{
	private Main main;
	private GestureDetector touchPadGestureDetector;
	private GVRTouchPadGestureListener swipeListener = new GVRTouchPadGestureListener()
	{
		@Override
		public boolean onSingleTapUp(MotionEvent e)
		{

			main.onSingleTapConfirmed();
			return false;
		}

		@Override
		public boolean onSwipe(MotionEvent e, Action action, float vx, float vy)
		{
			TouchPadInput.onSwipe(action);
			main.onSwipe();
			return false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		main = new Main();
		touchPadGestureDetector = new GestureDetector(getBaseContext(), swipeListener);
		setMain(main, "gvr.xml");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		touchPadGestureDetector.onTouchEvent(event);
		TouchPadInput.input(event);
		return super.onTouchEvent(event);
	}


	@Override
	protected void onResume() {
		super.onResume();
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
