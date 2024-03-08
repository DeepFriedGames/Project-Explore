package com.shdwfghtr.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.shdwfghtr.asset.InventoryService;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.explore.GameCamera;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.World;
import com.shdwfghtr.screens.GameScreen;


public class Bullet extends Entity  implements Pool.Poolable {
    public static final Pool<Bullet> POOL = new Pool<Bullet>() {
        @Override
        protected Bullet newObject() {
            return new Bullet();
        }
    };
    private static final float TRAUMA_RATIO = 0.1f / 6; //0.1 trauma for every 3 power
    static final float SPEED = 6f;
    private static final Vector2 VECTOR2 = new Vector2();
    Entity target;
    final TimeService.Timer rangeTimer = new TimeService.Timer(0.18f) {
        @Override
        public boolean onCompletion() {
            setDelete(true);
            return true;
        }
    };

    @Override
    void setAnimation(Animation<TextureRegion> animation) {
        animation.setFrameDuration(0.03f);
        super.setAnimation(animation);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if(!GdxGame.getCamera().getBox().overlaps(getBox()))
            destroy();

        if(target != null) {
            VECTOR2.set(target.getCenter().sub(getCenterX(), getCenterY()).limit(speed));
            if(VECTOR2.angleDeg() > d.angleDeg())
                d.setAngleDeg(d.angleDeg() + 5);
            else if (VECTOR2.angleDeg() < d.angleDeg())
                d.setAngleDeg(d.angleDeg() - 5);
        }
    }

    @Override
    public void collideWith(Entity entity) {
        if(entity instanceof Enemy) {
            destroy();
        }
    }

    @Override
    public void checkCollisions() {
        if(World.CURRENT.isBlocked(getCenterX(), getCenterY())) {
            if (!InventoryService.isActive("phase_shot")) destroy();
            World.CURRENT.breakTile(getCenterX(), getCenterY());
            if(getName().matches("missile")){
                GdxGame.getCamera().addTrauma(power * TRAUMA_RATIO);
            }
        }
    }

    @Override
    public void reset() {
        super.respawn();
    }

    @Override
    public void destroy() {
        ParticleEffectPool.PooledEffect effect = GdxGame.particleService.obtain("bullet", false);
        effect.setPosition(getCenterX(), getCenterY());
        GdxGame.particleService.add(effect);
        POOL.free(this);
        super.destroy();
    }
}
