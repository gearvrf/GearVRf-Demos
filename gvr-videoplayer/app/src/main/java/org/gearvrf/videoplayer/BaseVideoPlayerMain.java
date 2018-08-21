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

package org.gearvrf.videoplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.gearvrf.GVRBitmapImage;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRTexture;


public abstract class BaseVideoPlayerMain extends GVRMain {

    private static final float IMAGE_WIDTH = 1;
    private static final float IMAGE_HEIGHT = IMAGE_WIDTH * .0992f;
    private static final float SPLASH_FADE_DURATION = 1;
    private static final float SPLASH_DURATION = 3;

    @Override
    public float getSplashFadeTime() {
        return SPLASH_FADE_DURATION;
    }

    @Override
    public float getSplashDisplayTime() {
        return SPLASH_DURATION;
    }

    @Override
    public GVRMesh getSplashMesh(GVRContext gvrContext) {
        return gvrContext.createQuad(IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    @Override
    public GVRTexture getSplashTexture(GVRContext gvrContext) {
        Bitmap bitmap = BitmapFactory.decodeResource(
                gvrContext.getContext().getResources(),
                R.drawable.ic_app_name_highlight);
        GVRTexture splashScreen = new GVRTexture(gvrContext);
        splashScreen.setImage(new GVRBitmapImage(gvrContext, bitmap));
        return splashScreen;
    }
}
