//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.shdwfghtr.entity;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.Tile;
import com.shdwfghtr.explore.Timer;
import com.shdwfghtr.explore.World;

public class Slick extends Entity implements Poolable {
    public static final Pool<Slick> POOL = new Pool<Slick>() {
        protected Slick newObject() {
            return new Slick();
        }
    };
    public static final float FRICTION = 0.95f;
        
    
    private final transient ParticleEffectPool.PooledEffect effect;
    private final Timer lifeTimer = new Timer(14.0F) {
        public boolean onCompletion() {
            destroy();
            return true;
        }
    };

    private Slick() {
        super("enemy_slick");
        setSize(Tile.WIDTH, Tile.HEIGHT);
        this.effect = Asset.getParticles().obtain("slick", false);
        this.drawLayer = DrawLayer.FOREGROUND;
    }

    @Override
    public void draw(Batch batch) {
        if(!Asset.CONTROLS.getBoolean("particles")) super.draw(batch);
    }

    public void set(float x, float y) {
        this.setPosition(x, y);
        this.lifeTimer.reset();
        this.effect.setPosition(getX(), getTop());
        Asset.getParticles().add(this.effect);
    }

    @Override
    public void reset() {
        respawn();
        setPosition(0, 0);
        World.CURRENT.removeEntity(this);
    }

    @Override
    void destroy() {
        super.destroy();
        POOL.free(this);
    }

    public static void spawn(float x, float y) {
        return;
//        if(!World.CURRENT.isSlick(x, y)) {
//            Slick spawn = POOL.obtain();
//            spawn.set(World.getTileX(x), World.getTileY(y));
//            World.CURRENT.addEntity(spawn);
//        }
    }
}
