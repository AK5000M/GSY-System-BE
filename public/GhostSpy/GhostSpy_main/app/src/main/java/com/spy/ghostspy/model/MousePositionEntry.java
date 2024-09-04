package com.spy.ghostspy.model;

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
}
