package com.support.litework.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ApplistEntry {
    private final String packagename;
    private final String appname;
    private final Boolean lockStatus;
    public ApplistEntry(String packagename, String appname, Boolean lockstatus) {
        this.packagename = packagename;
        this.appname = appname;
        this.lockStatus = lockstatus;
    }
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("packagename", packagename);
            jsonObject.put("appname", appname);
            jsonObject.put("locked", lockStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
