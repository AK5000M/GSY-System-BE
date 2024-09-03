package com.spy.ghostspy.model;


import org.json.JSONException;
import org.json.JSONObject;

public class SkeletonEntry {
    private final String text;
    private final String type;
    private final int xposition;
    private final int yposition;
    private final int width;
    private final int height;


    public SkeletonEntry(String text, String type, int xposition, int yposition, int width, int height) {
        this.text = text;
        this.type = type;
        this.xposition = xposition;
        this.yposition = yposition;
        this.width = width;
        this.height = height;
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}