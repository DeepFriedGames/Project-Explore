package com.shdwfghtr.entity;

import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.Timer;
import com.shdwfghtr.explore.World;

/**
 * Created by Stuart on 7/14/2015.
 * A wasp nest is a spawner that periodically creats @Wasp instances.
 */
public class WaspNest extends Enemy {
    private Wasp wasp;
    private final Timer spawnTimer = new Timer(1.2f);

    public WaspNest(float x, float y) {
        super("enemy_wasp_nest", x, y);
        this.health = 10;
        this.drawLayer = DrawLayer.FOREGROUND;
   
    }

    @Override
    public void update(float delta) {
        if(wasp == null && spawnTimer.isComplete()) {
            if (Player.CURRENT.getCenterY() < getY()) {
                wasp = Wasp.POOL.obtain();
                wasp.respawn();
                wasp.setPosition(getCenterX() - wasp.getWidth() / 2, getCenterY() - wasp.getHeight() / 2);
                wasp.d.set(0, -wasp.speed);
                World.CURRENT.addEntity(wasp);
                spawnTimer.reset();
            }
        } else if(wasp != null){
            if (!Asset.CAMERA.getBox().overlaps(wasp.getBox()) && Player.CURRENT.getCenterY() < getY()) {
                wasp.setPosition(getCenterX() - wasp.getWidth() / 2, getCenterY() - wasp.getHeight() / 2);
                wasp.d.set(0, -wasp.speed);
            }
            if (wasp.isDead()) {
                Wasp.POOL.free(wasp);
                wasp = null;
            }
        }
    }

    @Override
    public void takeDamage(float amount) {
        if(!hurt) Asset.getMusicHandler().playSound("damage1");
        super.takeDamage(amount);
    }

    @Override
    public void respawn() {
        super.respawn();
        this.health = 10;
    }
}
