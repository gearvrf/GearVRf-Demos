package org.gearvrf.videoplayer.component.video.control;

import android.annotation.SuppressLint;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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

public class ControlWidget extends FadeableObject implements Focusable, View.OnClickListener, SeekBar.OnSeekBarChangeListener, IViewEvents {

    @IntDef({ButtonState.PLAYING, ButtonState.PAUSED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ButtonState {
        int PLAYING = 0;
        int PAUSED = 1;
    }

    private SeekBar mSeekBar;
    private ImageView mPlayPauseButtonImage;
    private TextView mElapsedTime, mDurationTime, mTitle;
    private ControlWidgetListener mOnVideoControllerListener;
    @DrawableRes
    private int mStateResource;
    private GVRSceneObject mMainSceneObject;
    private FocusListener mFocusListener;

    public ControlWidget(final GVRContext gvrContext) {
        super(gvrContext);
        mMainSceneObject = new GVRViewSceneObject(gvrContext, R.layout.layout_player_controller, this);
        setName(getClass().getSimpleName());
    }

    @NonNull
    @Override
    protected GVRSceneObject getFadeable() {
        return mMainSceneObject;
    }

    private void setSeekTime(float x, int width) {
        //Just to avoid handling negative values for X once the framework sends a negative
        //value when the hover is outside the view even if we use its action (HOVER_MOVE, for
        //example)
        float eventX = x > 0 ? x : .0f;

        int seekTime;
        if (eventX == 0) {
            seekTime = 0;
        } else {
            seekTime = (int) ((eventX / width) * mSeekBar.getMax());
        }
        //TODO: Show the seek time on UI
        Log.d("SeekBar", "seekTime" + TimeUtils.formatDurationFull(seekTime));
    }

    public void setButtonState(@ButtonState int state) {
        if (state == ButtonState.PLAYING) {
            showPlayingState();
        } else if (state == ButtonState.PAUSED) {
            showPausedState();
        }
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
        if (mFocusListener != null) {
            mFocusListener.onFocusGained(this);
        }
    }

    @Override
    public void loseFocus() {
        if (mFocusListener != null) {
            mFocusListener.onFocusLost(this);
        }
    }

    @Override
    public void onInitView(GVRViewSceneObject sceneObject, View view) {
        holdView(view);
        showPlayingState();
    }

    @Override
    public void onStartRendering(GVRViewSceneObject sceneObject, View view) {
        addChildObject(mMainSceneObject);
    }

    private void holdView(View view) {

        mSeekBar = view.findViewById(R.id.progressBar);
        LinearLayout playPauseButton = view.findViewById(R.id.playPauseButton);
        mPlayPauseButtonImage = playPauseButton.findViewById(R.id.image);
        mElapsedTime = view.findViewById(R.id.elapsedTimeText);
        mDurationTime = view.findViewById(R.id.durationTimeText);
        mTitle = view.findViewById(R.id.titleText);

        playPauseButton.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                setSeekTime(event.getX(), v.getWidth());
                return false;
            }
        });
    }
}
