package main.core;

import main.main;

import java.awt.*;

public class Quad {

    private Integer x;
    private Integer y;
    private Color color;

    public Quad(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public Quad(Color color){
        this.color = color;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
