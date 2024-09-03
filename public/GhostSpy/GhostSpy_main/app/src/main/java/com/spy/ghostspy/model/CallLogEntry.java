package com.spy.ghostspy.model;

import org.json.JSONException;
import org.json.JSONObject;

public class CallLogEntry {
    private final String number;
    private final String type;
    private final String date;
    private final int duration;

    public CallLogEntry(String number, String type, String date, int duration) {
        this.number = number;
        this.type = type;
        this.date = date;
        this.duration = duration;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("number", number);
            jsonObject.put("type", type);
            jsonObject.put("date", date);
            jsonObject.put("duration", duration);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
