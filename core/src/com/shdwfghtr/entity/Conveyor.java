package com.shdwfghtr.entity;

public class Conveyor extends Entity {
    
    public Conveyor(float x, float y, boolean left) {
        super("conveyor", x, y, 16, 5.2f);
        this.speed = 0.5f;
        if(left) {
            this.speed *= -1;
            this.left = true;
        }
        this.drawLayer = DrawLayer.FOREGROUND;
    }

    @Override
    public void collideWith(Entity e) {
        if(e.speed != 0)
            e.translateX(speed);
    }
}
