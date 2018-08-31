package org.gearvrf.arpet.connection;

@FunctionalInterface
public interface OnMessageListener {
    void onMessageReceived(Message message);
}
