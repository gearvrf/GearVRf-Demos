package org.gearvrf.videoplayer.component.video;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.utility.Log;
import org.gearvrf.videoplayer.component.FadeableObject;
import org.gearvrf.videoplayer.component.FadeableViewObject;
import org.gearvrf.videoplayer.component.video.backbutton.BackButton;
import org.gearvrf.videoplayer.component.video.control.ControlWidget;
import org.gearvrf.videoplayer.component.video.control.ControlWidgetListener;
import org.gearvrf.videoplayer.component.video.dialog.OnPlayNextListener;
import org.gearvrf.videoplayer.component.video.dialog.PlayNextDialog;
import org.gearvrf.videoplayer.component.video.player.OnPlayerListener;
import org.gearvrf.videoplayer.component.video.player.Player;
import org.gearvrf.videoplayer.component.video.player.PlayerListenerDispatcher;
import org.gearvrf.videoplayer.component.video.title.OverlayTitle;
import org.gearvrf.videoplayer.focus.FocusListener;
import org.gearvrf.videoplayer.focus.FocusableViewSceneObject;
import org.gearvrf.videoplayer.focus.PickEventHandler;
import org.gearvrf.videoplayer.model.Video;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class VideoPlayer extends GVRSceneObject {

    private static final String TAG = VideoPlayer.class.getSimpleName();
    private static final float CONTROLLER_WIDGET_FACTOR = .70f;
    private static final float CONTROLLER_HEIGHT_FACTOR = .25f;
    private static final float BACK_BUTTON_SIZE_FACTOR = .1f;
    private static final float OVERLAY_TITLE_WIDTH_FACTOR = .34f;
    private static final float OVERLAY_TITLE_HEIGHT_FACTOR = .06f;
    private static final float PLAY_NEXT_DIALOG_WIDTH_FACTOR = .19f;
    private static final float PLAY_NEXT_DIALOG_HEIGHT_FACTOR = .34f;

    private Player mPlayer;
    private ControlWidget mControl;
    private BackButton mBackButton;
    private PlayNextDialog mPlayNextDialog;
    private OverlayTitle mOverlayTitle;

    private boolean mVideoPlayerActive = true;
    private boolean mIsControlActive = true;
    private boolean mBackButtonActive = true;
    private boolean mIsPlayNextDialogActive = true;
    private boolean mHideControlWidgetTimerEnabled;
    private HideControlWidgetTimer mHideControlTimer;
    private List<Video> mVideos;

    public VideoPlayer(GVRContext gvrContext, float playerWidth, float playerHeight) {
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


        addPlayer(playerWidth, playerHeight);
        addControlWidget(CONTROLLER_WIDGET_FACTOR * playerWidth, CONTROLLER_HEIGHT_FACTOR * playerHeight);
        addBackButton(BACK_BUTTON_SIZE_FACTOR * playerHeight, BACK_BUTTON_SIZE_FACTOR * playerHeight);
        addPlayNextDialog(PLAY_NEXT_DIALOG_WIDTH_FACTOR * playerWidth, PLAY_NEXT_DIALOG_HEIGHT_FACTOR * playerHeight);
        addTitleOverlay(OVERLAY_TITLE_WIDTH_FACTOR * playerWidth, OVERLAY_TITLE_HEIGHT_FACTOR * playerHeight);
    }

    public void prepare(@NonNull List<Video> videos) {
        mVideos = videos;
        List<File> videoFiles = new LinkedList<>();
        for (Video video : mVideos) {
            videoFiles.add(new File(video.getPath()));
        }
        if (videos.size() > 0) {
            mPlayer.prepare(videoFiles.toArray(new File[videoFiles.size()]));
        }
    }

    private void showControlWidget() {
        showControlWidget(mHideControlWidgetTimerEnabled);

    }

    private void showControlWidget(boolean autoHide) {
        Log.d(TAG, "showControlWidget: ");
        if (!mIsControlActive) {
            addChildObject(mControl);
            mControl.fadeIn(new FadeableViewObject.FadeInCallback() {
                @Override
                public void onFadeIn() {
                    mIsControlActive = true;
                }
            });
        }
        if (autoHide) {
            mHideControlTimer.start();
        }
    }

    private void hideControlWidget() {
        Log.d(TAG, "hideControlWidget: ");
        mHideControlTimer.cancel();
        if (mIsControlActive) {
            mIsControlActive = false;
            mControl.fadeOut(new FadeableViewObject.FadeOutCallback() {
                @Override
                public void onFadeOut() {
                    removeChildObject(mControl);
                }
            });
        }
    }

    private void showBackButton() {
        if (!mBackButtonActive) {
            addChildObject(mBackButton);
            mBackButton.fadeIn(new FadeableViewObject.FadeInCallback() {
                @Override
                public void onFadeIn() {
                    Log.d(TAG, "showBackButton");
                    mBackButtonActive = true;
                }
            });
        }
    }

    private void hideBackButton() {
        if (mBackButtonActive) {
            mBackButton.fadeOut(new FadeableViewObject.FadeOutCallback() {
                @Override
                public void onFadeOut() {
                    mBackButtonActive = false;
                    removeChildObject(mBackButton);
                }
            });
        }
    }

    private void showPlayNextDialog() {
        if (mVideoPlayerActive && !mIsPlayNextDialogActive) {
            mHideControlTimer.cancel();
            mPlayNextDialog.setVideoData(mVideos.get(mPlayer.getNextIndexToPlay()));
            addChildObject(mPlayNextDialog);
            mPlayNextDialog.fadeIn(new FadeableViewObject.FadeInCallback() {
                @Override
                public void onFadeIn() {
                    mIsPlayNextDialogActive = true;
                    showBackButton();
                    mPlayNextDialog.startTimer();
                }
            });
        }
    }

    private void hidePlayNextDialog() {
        if (mIsPlayNextDialogActive) {
            mIsPlayNextDialogActive = false;
            mPlayNextDialog.cancelTimer();
            mPlayNextDialog.fadeOut(new FadeableViewObject.FadeOutCallback() {
                @Override
                public void onFadeOut() {
                    removeChildObject(mPlayNextDialog);
                }
            });
        }
    }

    public void showAllControls() {
        if (mVideoPlayerActive) {
            if (!mIsPlayNextDialogActive) {
                showControlWidget();
            }
            showBackButton();
        }
    }

    private void hideAllControls() {
        if (!mIsPlayNextDialogActive) {
            hideBackButton();
        }
        hideControlWidget();
    }

    public void setControlWidgetAutoHide(boolean autoHide) {
        mHideControlWidgetTimerEnabled = autoHide;
        if (autoHide) {
            mHideControlTimer.start();
        }
    }

    private void addPlayer(float width, float height) {
        mPlayer = new Player(getGVRContext(), width, height);
        mPlayer.setOnVideoPlayerListener(mInternalVideoPlayerListener);
        addChildObject(mPlayer);
    }

    private void addControlWidget(float width, float height) {

        mControl = new ControlWidget(getGVRContext(), width, height);
        mControl.setOnVideoControllerListener(mOnVideoControllerListener);
        mControl.setFocusListener(mFocusListener);

        // Put video control widget below the video screen
        float positionY = -(height / CONTROLLER_HEIGHT_FACTOR / 2f);
        mControl.getTransform().setPositionY(positionY * 1.02f);

        mControl.getTransform().setPositionZ(mPlayer.getTransform().getPositionZ() + .05f);

        addChildObject(mControl);
    }

    private void addBackButton(float width, float height) {

        mBackButton = new BackButton(getGVRContext(), width, height);
        mBackButton.setFocusListener(mFocusListener);

        // Put back button above the video screen
        float positionY = (height / BACK_BUTTON_SIZE_FACTOR / 2f);
        mBackButton.getTransform().setPositionY(positionY - (positionY * .08f));

        mBackButton.getTransform().setPositionZ(mPlayer.getTransform().getPositionZ() + .05f);

        addChildObject(mBackButton);
    }

    private void addPlayNextDialog(float width, float height) {
        mPlayNextDialog = new PlayNextDialog(getGVRContext(), width, height, mOnPlayNextListener);
        mPlayNextDialog.getTransform().setPositionZ(mPlayer.getTransform().getPositionZ() + .5f);
        addChildObject(mPlayNextDialog);
    }

    private void addTitleOverlay(float width, float height) {
        mOverlayTitle = new OverlayTitle(getGVRContext(), width);
        float positionY = (height / OVERLAY_TITLE_HEIGHT_FACTOR / 2f);
        mOverlayTitle.getTransform().setPositionY(positionY + .5f);
        addChildObject(mOverlayTitle);
    }

    public void setPlayerListener(OnPlayerListener listener) {
        mInternalVideoPlayerListener.setOnVideoPlayerListener(listener);
    }

    public void setBackButtonClickListener(@NonNull View.OnClickListener listener) {
        mBackButton.setOnClickListener(listener);
    }

    public void play() {
        mHideControlTimer.start();
        mPlayer.playVideo();
    }

    public void pause() {
        mHideControlTimer.cancel();
        mPlayer.pauseVideo();
    }

    private FocusListener<FocusableViewSceneObject> mFocusListener = new FocusListener<FocusableViewSceneObject>() {
        @Override
        public void onFocusGained(FocusableViewSceneObject focusable) {
            Log.d(TAG, "onFocusGained: " + focusable.getClass().getSimpleName());
            if (focusable instanceof ControlWidget || focusable instanceof BackButton) {
                mHideControlTimer.cancel();
            }
        }

        @Override
        public void onFocusLost(FocusableViewSceneObject focusable) {
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
            mControl.showPlay();

            if (mVideoPlayerActive) {
                mPlayer.fadeIn(new FadeableObject.FadeInCallback() {
                    @Override
                    public void onFadeIn() {
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
            mControl.showPause();
            showAllControls();
            super.onStart();
        }

        @Override
        public void onLoading() {
            Log.d(TAG, "Video loading");
            super.onLoading();
        }

        @Override
        public void onFileEnd() {
            Log.d(TAG, "Video ended");
            if (mPlayer.hasNextToPlay()) {
                mPlayer.fadeOut();
                hideControlWidget();
                showPlayNextDialog();
            }
            super.onFileEnd();
        }

        @Override
        public void onAllFilesEnd() {
            super.onAllFilesEnd();
            Log.d(TAG, "All videos ended");
            final View view = mBackButton.getRootView();
            // Force back to gallery
            view.post(new Runnable() {
                @Override
                public void run() {
                    view.performClick();
                }
            });
        }
    };

    private ControlWidgetListener mOnVideoControllerListener = new ControlWidgetListener() {
        @Override
        public void onPlay() {
            Log.d(TAG, "onPlay: ");
            mPlayer.playVideo();
        }

        @Override
        public void onPause() {
            Log.d(TAG, "onPause: ");
            mPlayer.pauseVideo();
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
            hidePlayNextDialog();
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
            super.handleMessage(msg);
            mVideoPlayer.hideAllControls();
        }

        void start() {
            removeMessages(0);
            sendEmptyMessageDelayed(0, HIDE_WIDGET_DELAY);
        }

        public void cancel() {
            removeMessages(0);
        }
    }

    public void show(final FadeableObject.FadeInCallback fadeInCallback) {
        if (!mVideoPlayerActive) {
            mPlayer.fadeIn(new FadeableObject.FadeInCallback() {
                @Override
                public void onFadeIn() {
                    mVideoPlayerActive = true;
                    showAllControls();
                    if (fadeInCallback != null) {
                        fadeInCallback.onFadeIn();
                    }
                }
            });
            addChildObject(mOverlayTitle);
        }
    }

    public void hide() {
        hide(null);
    }

    public void hide(final FadeableObject.FadeOutCallback fadeOutCallback) {
        if (mVideoPlayerActive) {
            hidePlayNextDialog();
            hideAllControls();
            mPlayer.stop();
            mPlayer.fadeOut(new FadeableObject.FadeOutCallback() {
                @Override
                public void onFadeOut() {
                    mVideoPlayerActive = false;
                    if (fadeOutCallback != null) {
                        fadeOutCallback.onFadeOut();
                    }
                }
            });
            removeChildObject(mOverlayTitle);
        }
    }
}
