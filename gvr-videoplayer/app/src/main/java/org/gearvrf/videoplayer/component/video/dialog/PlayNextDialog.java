package org.gearvrf.videoplayer.component.video.dialog;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.component.FadeableViewObject;
import org.gearvrf.videoplayer.model.Video;
import org.gearvrf.videoplayer.provider.asyntask.ThumbnailLoader;
import org.gearvrf.videoplayer.util.TimeUtils;

public class PlayNextDialog extends FadeableViewObject implements View.OnClickListener {

    private TextView mTitle;
    private TextView mDuration;
    private ImageView mThumbnail;
    private TextView mTime;
    private CountdownTimer mCountdownTimer;
    private OnPlayNextListener mOnPlayNextListener;

    public PlayNextDialog(GVRContext gvrContext, float width, float height, @NonNull OnPlayNextListener listener) {
        super(gvrContext, getMainView(gvrContext, R.layout.layout_play_next), width, height);
        setName(getClass().getSimpleName());
        mOnPlayNextListener = listener;
        mCountdownTimer = new CountdownTimer(this);
    }

    private static View getMainView(GVRContext gvrContext, @LayoutRes int layout) {
        return LayoutInflater.from(gvrContext.getContext()).inflate(layout, null);
    }

    @Override
    protected void onInitView() {
        super.onInitView();
        View mainView = getRootView();
        mTitle = mainView.findViewById(R.id.title);
        mDuration = mainView.findViewById(R.id.duration);
        mThumbnail = mainView.findViewById(R.id.thumbnail);
        mTime = mainView.findViewById(R.id.time);

        mThumbnail.setOnClickListener(this);
    }

    private void setTime(final int time) {
        getRootView().post(new Runnable() {
            @Override
            public void run() {
                mTime.setText(String.valueOf(time));
            }
        });
    }

    private void notifyTimesUp() {
        mOnPlayNextListener.onTimesUp();
    }

    public void startTimer() {
        mCountdownTimer.start();
    }

    public void cancelTimer() {
        mCountdownTimer.cancel();
    }

    public void setVideoData(final Video video) {
        getRootView().post(new Runnable() {
            @Override
            public void run() {
                mTitle.setText(video.getTitle());
                mDuration.setText(TimeUtils.formatDurationFull(video.getDuration()));
                new ThumbnailLoader(mThumbnail).execute(video.getId());
            }
        });
    }

    @NonNull
    @Override
    protected GVRSceneObject getFadeable() {
        return this;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.thumbnail) {
            mOnPlayNextListener.onThumbClicked();
        }
    }

    private static class CountdownTimer extends Handler {

        static final int MAX_COUNT = 5;
        PlayNextDialog mPlayNextDialog;
        int count = MAX_COUNT;

        CountdownTimer(PlayNextDialog mPlayNextDialog) {
            this.mPlayNextDialog = mPlayNextDialog;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (count < 0) {
                mPlayNextDialog.notifyTimesUp();
                reset();
            } else {
                tick();
            }
        }

        private void tick() {
            mPlayNextDialog.setTime(count);
            count--;
            sendEmptyMessageDelayed(0, 1000);
        }

        void start() {
            reset();
            tick();
        }

        void cancel() {
            reset();
        }

        private void reset() {
            removeMessages(0);
            count = MAX_COUNT;
        }
    }
}
