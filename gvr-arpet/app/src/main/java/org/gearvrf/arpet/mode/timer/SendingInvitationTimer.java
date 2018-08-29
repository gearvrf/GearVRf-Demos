package org.gearvrf.arpet.mode.timer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.gearvrf.arpet.mode.SendingInvitationScene;

public class SendingInvitationTimer extends Handler {
    private static final int CHANGE_SCREEN = 10000;
    SendingInvitationScene mSendingInvitationScene;

    public SendingInvitationTimer(SendingInvitationScene sendingInvitationScene) {
        mSendingInvitationScene = sendingInvitationScene;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == 0) {
            mSendingInvitationScene.OnSending();
        }
    }

    public void start() {
        removeMessages(0);
        sendEmptyMessageDelayed(0, CHANGE_SCREEN);
        Log.d("XX", "Start Timer");
    }

    public void cancel() {
        removeMessages(0);
    }
}
