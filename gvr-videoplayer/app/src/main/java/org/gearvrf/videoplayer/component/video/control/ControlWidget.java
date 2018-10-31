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

package org.gearvrf.videoplayer.component.video.control;

import android.annotation.SuppressLint;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.component.FadeableObject;
import org.gearvrf.videoplayer.focus.FocusListener;
import org.gearvrf.videoplayer.focus.Focusable;
import org.gearvrf.videoplayer.util.TimeUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ControlWidget extends FadeableObject implements Focusable, View.OnClickListener, SeekBar.OnSeekBarChangeListener, IViewEvents, View.OnHoverListener {

    @IntDef({ButtonState.PLAYING, ButtonState.PAUSED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ButtonState {
        int PLAYING = 0;
        int PAUSED = 1;
    }

    private SeekBar mSeekBar;
    private View mTimelineHoverLayout;
    private TextView mTimelineHoverText;
    private ImageView mPlayPauseButtonImage;
    private TextView mElapsedTime, mDurationTime, mTitle;
    private ControlWidgetListener mOnVideoControllerListener;
    @DrawableRes
    private int mStateResource;
    private GVRSceneObject mMainSceneObject;
    private FocusListener mFocusListener;
    private final AlphaAnimation mFadeOut;
    private final AlphaAnimation mFadeIn;
    private FadeableObject mCursor;

    public ControlWidget(final GVRContext gvrContext) {
        super(gvrContext);

        mFadeIn = new AlphaAnimation(0, 1);
        mFadeIn.setInterpolator(new DecelerateInterpolator());
        mFadeIn.setDuration(200);

        mFadeOut = new AlphaAnimation(1, 0);
        mFadeOut.setInterpolator(new AccelerateInterpolator());
        mFadeOut.setDuration(200);

        mMainSceneObject = new GVRViewSceneObject(gvrContext, R.layout.layout_player_controller, this);
        setName(getClass().getSimpleName());
    }

    @NonNull
    @Override
    protected GVRSceneObject getFadeable() {
        return mMainSceneObject;
    }

    public void setButtonState(@ButtonState int state) {
        if (state == ButtonState.PLAYING) {
            showPlayingState();
        } else if (state == ButtonState.PAUSED) {
            showPausedState();
        }
    }

    public void setCursor(FadeableObject cursor) {
        mCursor = cursor;
    }

    private void showPausedState() {
        mStateResource = R.drawable.ic_pause;
        mPlayPauseButtonImage.post(new Runnable() {
            @Override
            public void run() {
                mPlayPauseButtonImage.setImageResource(mStateResource);
            }
        });
    }

    private void showPlayingState() {
        mStateResource = R.drawable.ic_play;
        mPlayPauseButtonImage.post(new Runnable() {
            @Override
            public void run() {
                mPlayPauseButtonImage.setImageResource(mStateResource);
            }
        });
    }

    public void setTitle(final CharSequence title) {
        mTitle.post(new Runnable() {
            @Override
            public void run() {
                mTitle.setText(title);
            }
        });
    }

    private void updateElapsedTimeText(final int progress) {
        mElapsedTime.post(new Runnable() {
            @Override
            public void run() {
                mElapsedTime.setText(TimeUtils.formatDurationFull(progress));
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateDurationTimeText(final int maxProgress) {
        mDurationTime.post(new Runnable() {
            @Override
            public void run() {
                mDurationTime.setText(TimeUtils.formatDurationFull(maxProgress));
            }
        });
    }

    public void setProgress(final int progress) {
        mSeekBar.post(new Runnable() {
            @Override
            public void run() {
                mSeekBar.setProgress(progress);
                updateElapsedTimeText(progress);
            }
        });
    }

    public void setMaxProgress(final int maxProgress) {
        mSeekBar.post(new Runnable() {
            @Override
            public void run() {
                mSeekBar.setMax(maxProgress);
                updateDurationTimeText(maxProgress);
            }
        });
    }

    public void setOnVideoControllerListener(ControlWidgetListener listener) {
        this.mOnVideoControllerListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.playPauseButton) {
            if (mOnVideoControllerListener != null) {
                if (mStateResource == R.drawable.ic_play) {
                    showPausedState();
                    mOnVideoControllerListener.onPlay();
                } else {
                    showPlayingState();
                    mOnVideoControllerListener.onPause();
                }
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mOnVideoControllerListener != null && fromUser) {
            mOnVideoControllerListener.onSeek(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public void setFocusListener(@NonNull FocusListener listener) {
        mFocusListener = listener;
    }

    @Override
    public void gainFocus() {

    }

    @Override
    public void loseFocus() {

    }

    @Override
    public void onInitView(GVRViewSceneObject sceneObject, View view) {
        holdView(view);
        showPlayingState();
    }

    @Override
    public void onStartRendering(GVRViewSceneObject sceneObject, View view) {
        addChildObject(sceneObject);
    }

    private void holdView(View view) {

        mSeekBar = view.findViewById(R.id.seekBar);
        mTimelineHoverLayout = view.findViewById(R.id.timelineHoverLayout);
        mTimelineHoverText = view.findViewById(R.id.timelineHoverText);
        View seekBarOverlay = view.findViewById(R.id.seekBarOverlay);

        LinearLayout playPauseButton = view.findViewById(R.id.playPauseButton);
        mPlayPauseButtonImage = playPauseButton.findViewById(R.id.image);
        mElapsedTime = view.findViewById(R.id.elapsedTimeText);
        mDurationTime = view.findViewById(R.id.durationTimeText);
        mTitle = view.findViewById(R.id.titleText);

        mFadeOut.setAnimationListener(new DefaultAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mTimelineHoverLayout.setVisibility(View.GONE);
            }
        });

        playPauseButton.setOnClickListener(this);
        playPauseButton.setOnHoverListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
        seekBarOverlay.setOnHoverListener(this);
        view.setOnHoverListener(this);
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        if (v.getId() == R.id.seekBarOverlay) {
            updateTimelineHoverText(event.getX(), v.getWidth(), event);
        }

        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER || v.getId() == R.id.playPauseButton) {
            if (mFocusListener != null) {
                mFocusListener.onFocusGained(ControlWidget.this);
            }
        } else {
            if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                if (mFocusListener != null) {
                    mFocusListener.onFocusLost(ControlWidget.this);
                }
            }
        }
        return false;
    }

    private void updateTimelineHoverText(float x, int width, MotionEvent event) {
        //Just to avoid handling negative values for X once the framework sends a negative
        //value when the hover is outside the view even if we use its action (HOVER_MOVE, for
        //example)
        float eventX = x > 0 ? x : 0;

        int seekTime;
        if (eventX == 0) {
            seekTime = 0;
        } else {
            seekTime = (int) ((eventX / width) * mSeekBar.getMax());
        }

        mTimelineHoverText.setText(TimeUtils.formatDurationFull(seekTime));
        mTimelineHoverLayout.setX(mTimelineHoverLayout.getLeft() + eventX);

        if (event.getAction() != MotionEvent.ACTION_HOVER_EXIT) {
            if (mTimelineHoverLayout.getVisibility() != View.VISIBLE) {
                mTimelineHoverLayout.setVisibility(View.VISIBLE);
                mTimelineHoverLayout.startAnimation(mFadeIn);
                if (mCursor.isEnabled()) {
                    mCursor.setEnable(false);
                }
            }
        } else {
            if (mTimelineHoverLayout.getVisibility() == View.VISIBLE) {
                mTimelineHoverLayout.startAnimation(mFadeOut);
                if (!mCursor.isEnabled()) {
                    mCursor.setEnable(true);
                }
            }
        }
    }

    private static class DefaultAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

    }
}
