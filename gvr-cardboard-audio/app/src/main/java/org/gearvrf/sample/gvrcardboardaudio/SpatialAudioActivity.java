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

package org.gearvrf.sample.gvrcardboardaudio;

import android.app.Activity;
import android.os.Bundle;
import org.gearvrf.GVRActivity;
import com.google.vrtoolkit.cardboard.audio.CardboardAudioEngine;

public class SpatialAudioActivity extends GVRActivity
{
    private CardboardAudioEngine cardboardAudioEngine;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        cardboardAudioEngine = new CardboardAudioEngine(getAssets(), CardboardAudioEngine.RenderingQuality.HIGH);

        setScript(new SpatialAudioScript(cardboardAudioEngine), "gvr.xml");
    }

    @Override
    public void onPause() {
        cardboardAudioEngine.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        cardboardAudioEngine.resume();
    }

}
