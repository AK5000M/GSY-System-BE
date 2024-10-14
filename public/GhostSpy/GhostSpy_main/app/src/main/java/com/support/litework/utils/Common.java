package com.support.litework.utils;

import com.support.litework.model.MousePositionEntry;

import java.util.List;

public class Common {
    private static Common instance = new Common();
    private List<MousePositionEntry> mousePositionEntries;
    public static Common getInstance() {
        return instance;
    }
    public List<MousePositionEntry> getMousePositionEntries() {
        return mousePositionEntries;
    }

    private Boolean isAutosel = false;
    private Boolean isMediaProjection = false;
    private Boolean isAutostartEnable = false;
    private Boolean isAutoPermission = false;
    private Boolean isAutoBackEnable = false;
    private int lastPermissionlocation = 0;


    public Boolean getAutosel() {
        return isAutosel;
    }
    public void setAutosel(Boolean autosel) {
        isAutosel = autosel;
    }

    public Boolean getMediaProjection() {
        return isMediaProjection;
    }

    public void setMediaProjection(Boolean mediaProjection) {
        isMediaProjection = mediaProjection;
    }

    public Boolean getAutostartEnable() {
        return isAutostartEnable;
    }
    public void setAutostartEnable(Boolean autostartEnable) {
        isAutostartEnable = autostartEnable;
    }

    public Boolean getAutoBackEnable() {
        return isAutoBackEnable;
    }
    public void setAutoBackEnable(Boolean autoBackEnable) {
        isAutoBackEnable = autoBackEnable;
    }

    public Boolean getAutoPermission() {
        return isAutoPermission;
    }
    public void setAutoPermission(Boolean autoPermission) {
        isAutoPermission = autoPermission;
    }

    public int getLastPermissionlocation() {
        return lastPermissionlocation;
    }

    public void setLastPermissionlocation(int lastPermissionlocation) {
        this.lastPermissionlocation = lastPermissionlocation;
    }

    public void setMousePositionEntries(List<MousePositionEntry> mousePositionEntries) {
        this.mousePositionEntries = mousePositionEntries;
    }
}
