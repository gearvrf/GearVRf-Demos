package org.gearvrf.arpet.permission;

public interface OnPermissionResultListener {

    void onPermissionGranted(@PermissionManager.PermissionType int type);

    void onPermissionDenied(@PermissionManager.PermissionType int type);
}
