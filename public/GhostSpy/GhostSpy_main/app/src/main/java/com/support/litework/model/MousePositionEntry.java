package com.support.litework.model;

import org.json.JSONException;
import org.json.JSONObject;

public class MousePositionEntry {
    private final double xPosition;
    private final double yPosition;

    public MousePositionEntry(double xPosition, double yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
    }

    public double getxPosition() {
        return xPosition;
    }

    public double getyPosition() {
        return yPosition;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("x", xPosition);
            jsonObject.put("y", yPosition);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
