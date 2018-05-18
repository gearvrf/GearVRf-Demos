package org.gearvrf.videoplayer.component.video;

import android.annotation.SuppressLint;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.util.TimeUtils;

public class VideoControllerComponent extends GVRSceneObject implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private View mMainView;
    private SeekBar mSeekBar;
    private View mPlayPauseButton;
    private TextView mElapsedTime, mDurationTime, mTitle;
    private OnVideoControllerListener mOnVideoControllerListener;
    @DrawableRes
    private int mStateResource;

    @SuppressLint("InflateParams")
    VideoControllerComponent(GVRContext gvrContext, float width, float height) {
        super(gvrContext, 0, 0);

        mMainView = LayoutInflater.from(gvrContext.getContext()).inflate(R.layout.player_controller, null);
        GVRViewSceneObject sceneObject = new GVRViewSceneObject(gvrContext, mMainView, width, height);
        addChildObject(sceneObject);
        initView();
    }

    private void initView() {

        mSeekBar = mMainView.findViewById(R.id.progressBar);
        mPlayPauseButton = mMainView.findViewById(R.id.playPauseButton);
        mElapsedTime = mMainView.findViewById(R.id.elapsedTimeText);
        mDurationTime = mMainView.findViewById(R.id.durationTimeText);
        mTitle = mMainView.findViewById(R.id.titleText);

        mPlayPauseButton.setOnClickListener(this);
        mMainView.findViewById(R.id.backButton).setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);

        showPlay();
    }

    public void showPause() {
        mStateResource = R.drawable.selector_button_pause;
        mPlayPauseButton.setBackgroundResource(mStateResource);
    }

    public void showPlay() {
        mStateResource = R.drawable.selector_button_play;
        mPlayPauseButton.setBackgroundResource(mStateResource);
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
                mElapsedTime.setText(TimeUtils.formatDuration(progress));
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateDurationTimeText(final int maxProgress) {
        mDurationTime.post(new Runnable() {
            @Override
            public void run() {
                mDurationTime.setText(TimeUtils.formatDuration(maxProgress));
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

    public void setPlayPauseButtonEnabled(final boolean enabled) {
        mPlayPauseButton.post(new Runnable() {
            @Override
            public void run() {
                mPlayPauseButton.setEnabled(enabled);
            }
        });
    }

    public void setOnVideoControllerListener(OnVideoControllerListener listener) {
        this.mOnVideoControllerListener = listener;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.playPauseButton) {
            if (mOnVideoControllerListener != null) {
                if (mStateResource == R.drawable.selector_button_play) {
                    showPause();
                    mOnVideoControllerListener.onPlay();
                } else {
                    showPlay();
                    mOnVideoControllerListener.onPause();
                }
            }
        } else if (v.getId() == R.id.backButton) {
            if (mOnVideoControllerListener != null) {
                mOnVideoControllerListener.onBack();
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
}
