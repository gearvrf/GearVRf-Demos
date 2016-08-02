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

package org.gearvrf.video.movie;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRSceneObject;

import java.io.IOException;
import java.util.ArrayList;

public class MovieManager {

    private MediaPlayer mMediaPlayer = null;

    private MovieTheater mTheaters[] = null;
    public final int CINEMAS_COUNT = 2;
    private int mCurrentTheaterIndex = 0;

    public MovieManager (GVRContext context) {
        // media player
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setLooping(true);

        try {
            AssetFileDescriptor fileDescriptor = context.getContext().getAssets().openFd("tron.mp4");
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            fileDescriptor.close();
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // cinemas
        GVRExternalTexture screenTexture = new GVRExternalTexture(context);
        mTheaters = new MovieTheater[CINEMAS_COUNT];
        mTheaters[0] = new MultiplexMovieTheater(context, mMediaPlayer, screenTexture);
        mTheaters[1] = new IMAXMovieTheater(context, mMediaPlayer, screenTexture);
    }

    public MovieTheater[] getAllMovieTheater() {
        return mTheaters;
    }

    public MovieTheater getCurrentMovieTheater() {
        return mTheaters[mCurrentTheaterIndex];
    }

    public void switchMovieTheater() {
        // hide the current theater
        mTheaters[mCurrentTheaterIndex].hideCinemaTheater();
        mCurrentTheaterIndex++;
        if (mCurrentTheaterIndex >= CINEMAS_COUNT) {
            mCurrentTheaterIndex = 0;
        }
        // show the new active theater
        mTheaters[mCurrentTheaterIndex].showCinemaTheater();
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }
}
