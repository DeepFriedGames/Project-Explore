package com.shdwfghtr.entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.Timer;
import com.shdwfghtr.explore.World;


public class Bullet extends Entity  implements Pool.Poolable {
    public static final Pool<Bullet> POOL = new Pool<Bullet>() {
        @Override
        protected Bullet newObject() {
            return new Bullet();
        }
    };
    private static final float TRAUMA_RATIO = 0.1f / 6; //0.1 trauma for every 3 power
    static final float SPEED = 6f;
    Entity target;
    final Timer rangeTimer = new Timer(0.18f) {
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
        if(!Asset.CAMERA.getBox().overlaps(getBox()))
            destroy();

        if(target != null) {
            Asset.VECTOR2.set(target.getCenter().sub(getCenterX(), getCenterY()).limit(speed));
            if(Asset.VECTOR2.angle() > d.angle())
                d.setAngle(d.angle() + 5);
            else if (Asset.VECTOR2.angle() < d.angle())
                d.setAngle(d.angle() - 5);
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
            if (!Player.CURRENT.itemActive("phase_shot")) destroy();
            World.CURRENT.breakTile(getCenterX(), getCenterY());
            if(getName().matches("missile"))
                Asset.CAMERA.addTrauma(power * TRAUMA_RATIO);
        }
    }

    @Override
    public void reset() {
        super.respawn();
    }

    @Override
    void destroy() {
        ParticleEffectPool.PooledEffect effect = Asset.getParticles().obtain("bullet", false);
        effect.setPosition(getCenterX(), getCenterY());
        Asset.getParticles().add(effect);
        POOL.free(this);
        super.destroy();
    }
}
