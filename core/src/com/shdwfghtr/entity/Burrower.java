package com.shdwfghtr.entity;

import com.shdwfghtr.explore.World;

/**
 * Created by Stuart on 2/17/2018.
 */

public class Burrower extends ChainEnemy {
    private Mode mode = Mode.BURROW;
    private float jump_speed = 3.2f;

    public Burrower(float x, float y, int length) {
        super("enemy_burrower_head", x, y, new Entity[length]);
        this.power = 18;
        this.speed = 1.7f;
        this.persistent = true;
        for(int n = 0; n < length ; n++) {
            Entity body;
            if(n < length - 1) {
                body = new Entity("enemy_burrower_body", x, y);
                body.health = 15;
            } else {
                body = new Enemy("enemy_burrower_tail", x, y);
                body.power = 18;
            }

            body.persistent = true;
            bodyLinks[n] = body;

        }
    }

    @Override
    void checkCollisions() {
        switch (mode) {
            case BURROW:
                if(Math.abs(Player.CURRENT.getCenterX() - getCenterX()) < getWidth() * 2) {
                    d.y = jump_speed;
                    mode = Mode.TRANSITION;
                } else
                    d.set(Math.copySign(speed, Player.CURRENT.getCenterX() - getCenterX()), 0);

                break;
            case TRANSITION:
                d.y -= World.CURRENT.getGravity();

                float y = World.CURRENT.getSector(getCenter()).y;
                if(getY() < y) {
                    mode = Mode.BURROW;
                    setY(y);
                }

                break;
        }
    }

    private enum Mode {
        TRANSITION, BURROW
    }
}
