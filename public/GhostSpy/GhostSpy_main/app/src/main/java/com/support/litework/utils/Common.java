package com.support.litework.utils;

import com.support.litework.model.MousePositionEntry;

import java.util.ArrayList;
import java.util.List;

public class Common {
    private static Common instance = new Common();
    private List<MousePositionEntry> mousePositionEntries;
    private List<MousePositionEntry> keygenEntries;
    private Boolean isSetKeygen = false;
    public static Common getInstance() {
        return instance;
    }


    private Boolean isAutosel = false;
    private Boolean isMediaProjection = false;
    private Boolean isAutostartEnable = false;
    private Boolean isAutoPermission = false;
    private Boolean isAutoBackEnable = false;
    private ArrayList<String> packageList = new ArrayList<>();
    private int lastPermissionlocation = 0;
    public List<MousePositionEntry> getMousePositionEntries() {
        return mousePositionEntries;
    }
    public void setMousePositionEntries(List<MousePositionEntry> mousePositionEntries) {
        this.mousePositionEntries = mousePositionEntries;
    }
    public List<MousePositionEntry> getKeygenEntries() {
        return keygenEntries;
    }
    public void setKeygenEntries(List<MousePositionEntry> keygenEntries) {
        this.keygenEntries = keygenEntries;
    }

    public Boolean getSetKeygen() {
        return isSetKeygen;
    }
    public void setSetKeygen(Boolean setKeygen) {
        isSetKeygen = setKeygen;
    }

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
    public ArrayList<String> getPackageList() {
        return packageList;
    }
    public void setPackageList(ArrayList<String> packageList) {
        this.packageList = packageList;
    }
}
