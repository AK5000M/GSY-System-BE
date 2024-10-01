package com.support.litework.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ImageData {
    private final long id;
    private final String filePath;
    public ImageData(long id, String filePath) {
        this.id = id;
        this.filePath = filePath;
    }
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "image");
            jsonObject.put("imageId", id);
            jsonObject.put("filepath", filePath);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
