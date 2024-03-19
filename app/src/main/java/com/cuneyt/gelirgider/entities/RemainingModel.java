package com.cuneyt.gelirgider.entities;

public class RemainingModel {
    private String id;
    private int remainig;

    public RemainingModel() {
    }

    public RemainingModel(String id, int remainig) {
        this.id = id;
        this.remainig = remainig;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRemainig() {
        return remainig;
    }

    public void setRemainig(int remainig) {
        this.remainig = remainig;
    }
}
