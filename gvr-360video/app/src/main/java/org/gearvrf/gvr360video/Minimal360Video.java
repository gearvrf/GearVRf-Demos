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

package org.gearvrf.gvr360video;

import java.io.File;
import java.io.IOException;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.net.Uri;
import android.media.MediaPlayer;
import android.content.res.AssetFileDescriptor;
import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRMain;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject.GVRVideoType;
import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;

public class Minimal360Video extends GVRMain
{
    Minimal360Video(GVRVideoSceneObjectPlayer<?> player) {
        mPlayer = player;
    }

    /** Called when the activity is first created. */
    @Override
    public void onInit(GVRContext gvrContext) {
        GVRScene scene = gvrContext.getMainScene();

        /*// create sphere / mesh
        GVRSphereSceneObject sphere = new GVRSphereSceneObject(gvrContext, 72, 144, false);
        GVRMesh mesh = sphere.getRenderData().getMesh();

        // create video scene
        GVRVideoSceneObject video = new GVRVideoSceneObject( gvrContext, mesh, mPlayer, GVRVideoType.MONO );
        video.getTransform().setScale(100f, 100f, 100f);
        video.setName( "video" );

        // apply video to scene
        scene.addSceneObject( video );*/

        MediaPlayer mediaPlayer = getMediaPlayer(gvrContext);
        GVRVideoSceneObject video = new GVRVideoSceneObject( gvrContext, 4, 2, mediaPlayer, GVRVideoType.MONO );
        video.getTransform().setPositionZ(-4);
        video.getTransform().rotateByAxisWithPivot(30, 0, 1, 0, 0, 0, 0);
        video.setName( "video" );
        scene.addSceneObject( video );

        mediaPlayer = getMediaPlayer(gvrContext);
        video = new GVRVideoSceneObject( gvrContext, 4, 2, mediaPlayer, GVRVideoType.MONO );
        video.getTransform().setPositionZ(-4);
        video.getTransform().rotateByAxisWithPivot(60, 0, 1, 0, 0, 0, 0);
        video.setName( "video2" );
        scene.addSceneObject( video );

        mediaPlayer = getMediaPlayer(gvrContext);
        video = new GVRVideoSceneObject( gvrContext, 4, 2, mediaPlayer, GVRVideoType.MONO );
        video.getTransform().setPositionZ(-4);
        video.getTransform().rotateByAxisWithPivot(90, 0, 1, 0, 0, 0, 0);
        video.setName( "video2" );
        scene.addSceneObject( video );

        mediaPlayer = getMediaPlayer(gvrContext);
        video = new GVRVideoSceneObject( gvrContext, 4, 2, mediaPlayer, GVRVideoType.MONO );
        video.getTransform().setPositionZ(-4);
        video.getTransform().rotateByAxisWithPivot(120, 0, 1, 0, 0, 0, 0);
        video.setName( "video2" );
        scene.addSceneObject( video );

        mediaPlayer = getMediaPlayer(gvrContext);
        video = new GVRVideoSceneObject( gvrContext, 4, 2, mediaPlayer, GVRVideoType.MONO );
        video.getTransform().setPositionZ(-4);
        video.getTransform().rotateByAxisWithPivot(150, 0, 1, 0, 0, 0, 0);
        video.setName( "video2" );
        scene.addSceneObject( video );

        mediaPlayer = getMediaPlayer(gvrContext);
        video = new GVRVideoSceneObject( gvrContext, 4, 2, mediaPlayer, GVRVideoType.MONO );
        video.getTransform().setPositionZ(-4);
        video.getTransform().rotateByAxisWithPivot(180, 0, 1, 0, 0, 0, 0);
        video.setName( "video2" );
        scene.addSceneObject( video );

        mediaPlayer = getMediaPlayer(gvrContext);
        video = new GVRVideoSceneObject( gvrContext, 4, 2, mediaPlayer, GVRVideoType.MONO );
        video.getTransform().setPositionZ(-4);
        video.getTransform().rotateByAxisWithPivot(210, 0, 1, 0, 0, 0, 0);
        video.setName( "video2" );
        scene.addSceneObject( video );

        mediaPlayer = getMediaPlayer(gvrContext);
        video = new GVRVideoSceneObject( gvrContext, 4, 2, mediaPlayer, GVRVideoType.MONO );
        video.getTransform().setPositionZ(-4);
        video.getTransform().rotateByAxisWithPivot(240, 0, 1, 0, 0, 0, 0);
        video.setName( "video2" );
        scene.addSceneObject( video );
    }

    private MediaPlayer getMediaPlayer(GVRContext gvrContext) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        AssetFileDescriptor afd;
        try {
            afd = gvrContext.getContext().getAssets().openFd("videos_s_3.mp4");
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            gvrContext.getActivity().finish();
            android.util.Log.e("Minimal360Video", "Assets were not loaded. Stopping application!");
        }

        mediaPlayer.setLooping( true );
        mediaPlayer.start();
        return mediaPlayer;
    }

    private final GVRVideoSceneObjectPlayer<?> mPlayer;

}
