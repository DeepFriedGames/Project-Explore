package com.shdwfghtr.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.World;
import com.shdwfghtr.screens.GameScreen;

/**
 * Created by Stuart on 7/14/2015.
 * A wasp nest is a spawner that periodically creats @Wasp instances.
 */
public class WaspNest extends Enemy {
    private Wasp wasp;
    private final TimeService.Timer spawnTimer = new TimeService.Timer(1.2f) {
        @Override
        public boolean onCompletion() {
            if (wasp == null && Player.CURRENT.getCenterY() < getY()) {
                wasp = Wasp.POOL.obtain();
                wasp.respawn();
                wasp.setPosition(getCenterX() - wasp.getWidth() / 2, getCenterY() - wasp.getHeight() / 2);
                wasp.d.set(0, -wasp.speed);
                World.CURRENT.addEntity(wasp);
                spawnTimer.reset();
                return true;
            }
            return false;
        }
    };

    public WaspNest(float x, float y) {
        super("enemy_wasp_nest", x, y);
        this.health = 10;
        this.drawLayer = DrawLayer.FOREGROUND;
   
    }

    @Override
    public void update(float delta) {
        if(wasp != null){
            if (!GdxGame.getCamera().getBox().overlaps(wasp.getBox())
                    && Player.CURRENT.getCenterY() < getY()) {
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
        if(!hurt) GdxGame.audioService.playSound("damage1");
        super.takeDamage(amount);
    }

    @Override
    public void respawn() {
        super.respawn();
        this.health = 10;
    }
}
