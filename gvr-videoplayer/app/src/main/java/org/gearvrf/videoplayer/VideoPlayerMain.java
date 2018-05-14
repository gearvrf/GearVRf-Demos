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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject.GVRVideoType;
import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;

public class VideoPlayerMain extends GVRMain
{
    VideoPlayerMain(GVRVideoSceneObjectPlayer<?> player) {
        mPlayer = player;
    }

    /** Called when the activity is first created. */
    @Override
    public void onInit(GVRContext gvrContext) {

        GVRScene scene = gvrContext.getMainScene();

        // set up camerarig position (default)
        scene.getMainCameraRig().getTransform().setPosition( 0.0f, 0.0f, 0.0f );

        // create sphere / mesh
        GVRSphereSceneObject sphere = new GVRSphereSceneObject(gvrContext, 72, 144, false);
        GVRMesh mesh = sphere.getRenderData().getMesh();

        // create video scene
        GVRVideoSceneObject video = new GVRVideoSceneObject( gvrContext, mesh, mPlayer, GVRVideoType.MONO );
        video.getTransform().setScale(100f, 100f, 100f);
        video.setName( "video" );

        // apply video to scene
        scene.addSceneObject( video );
    }

    @Override
    public void onStep() {
    }

    private final GVRVideoSceneObjectPlayer<?> mPlayer;
}
