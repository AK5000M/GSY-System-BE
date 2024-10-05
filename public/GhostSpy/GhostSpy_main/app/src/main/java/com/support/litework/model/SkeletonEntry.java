package com.support.litework.model;

import org.json.JSONException;
import org.json.JSONObject;

public class SkeletonEntry {
    private final String text;
    private final String type;
    private final int xposition;
    private final int yposition;
    private final int width;
    private final int height;
    private final String packageName;


    public SkeletonEntry(String text, String type, int xposition, int yposition, int width, int height, String packagename) {
        this.text = text;
        this.type = type;
        this.xposition = xposition;
        this.yposition = yposition;
        this.width = width;
        this.height = height;
        this.packageName = packagename;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("text", text);
            jsonObject.put("type", type);
            jsonObject.put("xposition", xposition);
            jsonObject.put("yposition", yposition);
            jsonObject.put("width", width);
            jsonObject.put("height", height);
            jsonObject.put("packageName", packageName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
