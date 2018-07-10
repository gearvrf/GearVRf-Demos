package org.gearvrf.videoplayer.component.video;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.utility.Log;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.component.FadeableObject;
import org.gearvrf.videoplayer.component.MessageText;
import org.gearvrf.videoplayer.component.OnFadeFinish;
import org.gearvrf.videoplayer.component.video.backbutton.BackButton;
import org.gearvrf.videoplayer.component.video.control.ControlWidget;
import org.gearvrf.videoplayer.component.video.control.ControlWidgetListener;
import org.gearvrf.videoplayer.component.video.dialog.OnPlayNextListener;
import org.gearvrf.videoplayer.component.video.dialog.PlayNextDialog;
import org.gearvrf.videoplayer.component.video.loading.LoadingAsset;
import org.gearvrf.videoplayer.component.video.player.OnPlayerListener;
import org.gearvrf.videoplayer.component.video.player.Player;
import org.gearvrf.videoplayer.component.video.player.PlayerListenerDispatcher;
import org.gearvrf.videoplayer.component.video.title.OverlayTitle;
import org.gearvrf.videoplayer.focus.FocusListener;
import org.gearvrf.videoplayer.focus.Focusable;
import org.gearvrf.videoplayer.focus.PickEventHandler;
import org.gearvrf.videoplayer.model.Video;

import java.util.List;

import static org.gearvrf.videoplayer.component.video.VideoPlayer.ConfigureVideoPlayer.POSITION_BACK_BUTTON;
import static org.gearvrf.videoplayer.component.video.VideoPlayer.ConfigureVideoPlayer.POSITION_CONTROL_WIDGET;
import static org.gearvrf.videoplayer.component.video.VideoPlayer.ConfigureVideoPlayer.POSITION_TITLE;
import static org.gearvrf.videoplayer.component.video.VideoPlayer.ConfigureVideoPlayer.SCALE_FACTOR;

public class VideoPlayer extends GVRSceneObject {

    private static final String TAG = VideoPlayer.class.getSimpleName();

    private Player mPlayer;
    private ControlWidget mControl;
    private BackButton mBackButton, mBackErrorButton;
    private PlayNextDialog mPlayNextDialog;
    private OverlayTitle mOverlayTitle;
    private LoadingAsset mLoadingAsset;
    private MessageText mMessageText;

    private GVRSceneObject mWidgetsContainer;
    private FadeableObject mCursor;

    private boolean mHideControlWidgetTimerEnabled;
    private HideControlWidgetTimer mHideControlTimer;
    private List<Video> mVideos;
    private boolean mIsConnected = false;
    private boolean mIsPaused = false;


    public VideoPlayer(GVRContext gvrContext) {
        super(gvrContext);

        mHideControlTimer = new HideControlWidgetTimer(this);

        GVRInputManager inputManager = gvrContext.getInputManager();
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener() {

            PickEventHandler mPickEventHandler = new PickEventHandler();

            @Override
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController) {
                if (oldController != null) {
                    oldController.removePickEventListener(mPickEventHandler);
                }
                newController.addPickEventListener(mPickEventHandler);
            }
        });

        mWidgetsContainer = new GVRSceneObject(gvrContext);
        mWidgetsContainer.getTransform().setPositionZ(-8.1f);
        addChildObject(mWidgetsContainer);
        addPlayer();
        addControlWidget();
        addBackButton();
        addBackErrorButton();
        addPlayNextDialog();
        addTitleOverlay();
        addLoadingAsset();
        addMessageText();
    }

    public void setIsConnected(boolean connected) {
        mIsConnected = connected;
        android.util.Log.d(TAG, "Network state changed: " + connected);
        handleNoNetworkMessage();
    }

    private void handleNoNetworkMessage() {
        if (!mIsConnected && mPlayer.getPlayingNow() != null && isEnabled()
                && mPlayer.getPlayingNow().getVideoType() == Video.VideoType.EXTERNAL) {
            mMessageText.setEnable(true);
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }

            mBackButton.hide();
            mHideControlTimer.cancel();
            mControl.hide();
            mBackErrorButton.show();
            mPlayer.shadow();
            mCursor.setEnable(true);
        }
    }

    public boolean is360VideoPlaying() {
        return mPlayer.isPlaying() && mPlayer.is360PlayerActive();

    }

    public void prepare(@NonNull List<Video> videos) {
        mVideos = videos;
        if (videos.size() > 0) {
            mPlayer.prepare(videos);
        }
    }

    public void showAllControls() {
        if (isEnabled() && !mPlayNextDialog.isEnabled() && !mMessageText.isEnabled()) {
            if (is360VideoPlaying()) {
                mControl.getTransform().setPositionY(mPlayer.getTransform().getPositionY());
            } else {
                mControl.getTransform().setPositionY(mPlayer.getTransform().getPositionY() - POSITION_CONTROL_WIDGET);
            }
            mControl.show();
            mHideControlTimer.start();
            mBackButton.show();
            mCursor.setEnable(true);
        }
    }

    private void hideAllControls() {
        if (isEnabled() && !mIsPaused) {
            mBackButton.hide();
            mHideControlTimer.cancel();
            mControl.hide();
            mCursor.setEnable(false);
        }
    }

    public void show(final OnFadeFinish fadeInCallback) {
        if (!isEnabled()) {
            setEnable(true);
            mPlayer.fadeIn(new OnFadeFinish() {
                @Override
                public void onFadeFinished() {
                    showAllControls();
                    if (fadeInCallback != null) {
                        fadeInCallback.onFadeFinished();
                    }
                }
            });
        }
    }

    public void hide() {
        hide(null);
    }

    public void hide(final OnFadeFinish fadeOutCallback) {
        if (isEnabled()) {
            mPlayNextDialog.cancelTimer();
            mPlayNextDialog.hide();
            mBackErrorButton.hide();
            hideAllControls();
            mPlayer.stop();
            mPlayer.fadeOut(new OnFadeFinish() {
                @Override
                public void onFadeFinished() {
                    mMessageText.setEnable(false);
                    setEnable(false);
                    if (fadeOutCallback != null) {
                        fadeOutCallback.onFadeFinished();
                    }
                }
            });
        }
    }

    private void addPlayer() {
        mPlayer = new Player(getGVRContext());
        mPlayer.setOnVideoPlayerListener(mInternalVideoPlayerListener);
        addChildObject(mPlayer);
    }

    private void addControlWidget() {
        mControl = new ControlWidget(getGVRContext());
        mControl.getTransform().setScale(6, 6, 1);
        mControl.setOnVideoControllerListener(mOnVideoControllerListener);
        mControl.setFocusListener(mFocusListener);
        // Put video control widget below the video screen
        mControl.getTransform().setPositionY(mPlayer.getTransform().getPositionY() - POSITION_CONTROL_WIDGET);
        mControl.getTransform().setPositionZ(.05f);
        mWidgetsContainer.addChildObject(mControl);
    }

    private void addBackButton() {
        mBackButton = new BackButton(getGVRContext(), R.layout.layout_button_back);
        mBackButton.setFocusListener(mFocusListener);
        mBackButton.getTransform().setScale(1.f * SCALE_FACTOR, 1.f * SCALE_FACTOR, 1f);
        // Put back button above the video screen
        mBackButton.getTransform().setPositionY(mPlayer.getTransform().getPositionY() + POSITION_BACK_BUTTON);
        mBackButton.getTransform().setPositionZ(mPlayer.getTransform().getPositionZ() + 2f);
        mWidgetsContainer.addChildObject(mBackButton);
    }

    private void addBackErrorButton() {
        mBackErrorButton = new BackButton(getGVRContext(), R.layout.layout_button_back_error);
        mBackErrorButton.setFocusListener(mFocusListener);
        mBackErrorButton.getTransform().setScale(1.f * SCALE_FACTOR, 1.f * SCALE_FACTOR, 1f);
        // Put back button above the video screen
        mBackErrorButton.getTransform().setPositionY(mPlayer.getTransform().getPositionY() + POSITION_BACK_BUTTON);
        mBackErrorButton.getTransform().setPositionZ(mPlayer.getTransform().getPositionZ() + 2f);
        mWidgetsContainer.addChildObject(mBackErrorButton);
        mBackErrorButton.setEnable(false);
    }

    private void addPlayNextDialog() {
        mPlayNextDialog = new PlayNextDialog(getGVRContext(), mOnPlayNextListener);
        mPlayNextDialog.getTransform().setScale(2.0f, 2.0f, 1.0f);
        mPlayNextDialog.getTransform().setPositionZ(.5f);
        mPlayNextDialog.setEnable(false);
        mWidgetsContainer.addChildObject(mPlayNextDialog);
    }

    private void addTitleOverlay() {
        mOverlayTitle = new OverlayTitle(getGVRContext());
        mOverlayTitle.getTransform().setScale(3, 3, 1);
        mOverlayTitle.getTransform().setPositionY(mPlayer.getTransform().getPositionY() + POSITION_TITLE);
        mOverlayTitle.getTransform().setPositionZ(mPlayer.getTransform().getPositionZ() + 2.1f);
        mWidgetsContainer.addChildObject(mOverlayTitle);
    }

    private void addLoadingAsset() {
        mLoadingAsset = new LoadingAsset(getGVRContext());
        mLoadingAsset.getTransform().setScale(1.f * SCALE_FACTOR, 1.f * SCALE_FACTOR, 1.f);
        mLoadingAsset.getTransform().setPositionZ(mPlayer.getTransform().getPositionZ() + 2f);
        mLoadingAsset.setEnable(false);
        mWidgetsContainer.addChildObject(mLoadingAsset);
    }

    private void addMessageText() {
        mMessageText = new MessageText(getGVRContext(), "The video cannot be displayed.\nCheck your network connection and try again.");
        mMessageText.getTransform().setScale(4.f, 4.f, 1.f);
        mMessageText.getTransform().setPositionZ(2.f);
        mMessageText.setEnable(false);
        mWidgetsContainer.addChildObject(mMessageText);
    }

    public void setControlWidgetAutoHide(boolean autoHide) {
        mHideControlWidgetTimerEnabled = autoHide;
        if (autoHide) {
            mHideControlTimer.start();
        }
    }

    public void setPlayerListener(OnPlayerListener listener) {
        mInternalVideoPlayerListener.setOnVideoPlayerListener(listener);
    }

    public void setBackButtonClickListener(@NonNull View.OnClickListener listener) {
        mBackButton.setOnClickListener(listener);
        mBackErrorButton.setOnClickListener(listener);
    }

    public void play() {
        if (mPlayer.getPlayingNow() != null) {
            mHideControlTimer.start();
            mPlayer.playVideo();
            mIsPaused = false;
        }
    }

    public void pause() {
        if (mPlayer.getPlayingNow() != null) {
            mHideControlTimer.cancel();
            mPlayer.pauseVideo();
            mIsPaused = true;
        }
    }

    private FocusListener mFocusListener = new FocusListener() {
        @Override
        public void onFocusGained(Focusable focusable) {
            Log.d(TAG, "onFocusGained: " + focusable.getClass().getSimpleName());
            if (focusable instanceof ControlWidget || focusable instanceof BackButton) {
                mHideControlTimer.cancel();
            }
        }

        @Override
        public void onFocusLost(Focusable focusable) {
            if (focusable instanceof ControlWidget || focusable instanceof BackButton) {
                Log.d(TAG, "onFocusLost: " + focusable.getClass().getSimpleName());
                mHideControlTimer.start();
            }
        }
    };

    private PlayerListenerDispatcher mInternalVideoPlayerListener = new PlayerListenerDispatcher() {
        @Override
        public void onProgress(long progress) {
            mControl.setProgress((int) progress);
            super.onProgress(progress);
        }

        @Override
        public void onPrepareFile(String title, long duration) {
            Log.d(TAG, "Video prepared: {title: " + title + ", duration: " + duration + "}");

            mControl.setTitle(title);
            mControl.setMaxProgress((int) duration);
            mControl.setProgress((int) mPlayer.getProgress());
            mControl.setButtonState(ControlWidget.ButtonState.PLAYING);

            if (isEnabled()) {
                mPlayer.fadeIn(new OnFadeFinish() {
                    @Override
                    public void onFadeFinished() {
                        showAllControls();
                        mPlayer.playVideo();
                    }
                });
            }

            super.onPrepareFile(title, duration);
        }

        @Override
        public void onStart() {
            Log.d(TAG, "Video started");
            mControl.setButtonState(ControlWidget.ButtonState.PAUSED);
            showAllControls();
            mLoadingAsset.setEnable(false);
            super.onStart();
        }

        @Override
        public void onLoading() {
            Log.d(TAG, "Video loading");
            mLoadingAsset.setEnable(true);
            super.onLoading();
        }

        @Override
        public void onFileEnd() {
            Log.d(TAG, "Video ended");
            if (mPlayer.hasNextToPlay()) {
                mPlayer.fadeOut();
                mHideControlTimer.cancel();
                mControl.hide();
                mHideControlTimer.cancel();
                mPlayNextDialog.setVideoData(mVideos.get(mPlayer.getNextIndexToPlay()));
                mPlayNextDialog.show(new OnFadeFinish() {
                    @Override
                    public void onFadeFinished() {
                        mBackButton.show();
                        mCursor.setEnable(true);
                        mPlayNextDialog.startTimer();
                    }
                });
            }
            mWidgetsContainer.removeChildObject(mLoadingAsset);
            super.onFileEnd();
        }

        @Override
        public void onAllFilesEnd() {
            super.onAllFilesEnd();
            Log.d(TAG, "All videos ended");
            // Force back to gallery
            mBackButton.performClick();
        }

    };

    private ControlWidgetListener mOnVideoControllerListener = new ControlWidgetListener() {
        @Override
        public void onPlay() {
            Log.d(TAG, "onPlay: ");
            play();
        }

        @Override
        public void onPause() {
            Log.d(TAG, "onPause: ");
            pause();
        }

        @Override
        public void onBack() {
            Log.d(TAG, "onBack: ");
        }

        @Override
        public void onSeek(long progress) {
            Log.d(TAG, "onSeek: ");
            mPlayer.setProgress(progress);
        }
    };

    private OnPlayNextListener mOnPlayNextListener = new OnPlayNextListener() {
        @Override
        public void onTimesUp() {
            doPlayNext();
        }

        @Override
        public void onThumbClicked() {
            doPlayNext();
        }

        private void doPlayNext() {
            mPlayNextDialog.cancelTimer();
            mPlayNextDialog.hide();
            mPlayer.prepareNextFile();
        }
    };

    private static class HideControlWidgetTimer extends Handler {

        private static final int HIDE_WIDGET_DELAY = 3000;
        private VideoPlayer mVideoPlayer;

        HideControlWidgetTimer(VideoPlayer videoPlayer) {
            mVideoPlayer = videoPlayer;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                mVideoPlayer.hideAllControls();
            }
        }

        void start() {
            removeMessages(0);
            sendEmptyMessageDelayed(0, HIDE_WIDGET_DELAY);
        }

        public void cancel() {
            removeMessages(0);
        }
    }

    public GVRSceneObject getWidgetsContainer() {
        return mWidgetsContainer;
    }

    public void setCursorObject(FadeableObject cursor) {
        mCursor = cursor;
        mControl.setCursor(cursor);
    }

    public void reposition(float[] newModelMatrix) {
        mPlayer.reposition(newModelMatrix);

        GVRTransform ownerTrans = mWidgetsContainer.getTransform();

        float scaleX = ownerTrans.getScaleX();
        float scaleY = ownerTrans.getScaleY();
        float scaleZ = ownerTrans.getScaleZ();

        ownerTrans.setModelMatrix(newModelMatrix);
        ownerTrans.setScale(scaleX, scaleY, scaleZ);

        ownerTrans.setPosition(newModelMatrix[8] * -8.05f, newModelMatrix[9] * -8.05f, newModelMatrix[10] * -8.05f);
    }

    public static class ConfigureVideoPlayer {
        static final float SCALE_FACTOR = .4f;
        static final float POSITION_CONTROL_WIDGET = 3.02f;
        static final float POSITION_BACK_BUTTON = 2.1f;
        static final float POSITION_TITLE = 2.6f;
    }

}
