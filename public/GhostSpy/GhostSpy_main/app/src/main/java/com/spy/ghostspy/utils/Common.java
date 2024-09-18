package com.spy.ghostspy.utils;

import com.spy.ghostspy.model.MousePositionEntry;

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

    public void setMousePositionEntries(List<MousePositionEntry> mousePositionEntries) {
        this.mousePositionEntries = mousePositionEntries;
    }
}
