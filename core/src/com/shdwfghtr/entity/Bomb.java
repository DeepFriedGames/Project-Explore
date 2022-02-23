package com.shdwfghtr.entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.Timer;
import com.shdwfghtr.explore.World;

public class Bomb extends Entity implements Pool.Poolable{
    public static final Pool<Bomb> POOL = new Pool<Bomb>() {
        @Override
        protected Bomb newObject() {
            return new Bomb();
        }
    };
    private static final float TRAUMA_RATIO = 0.3f / 4;  //trauma per damage
    public final Timer timer = new Timer(1.2f) {
        @Override
        public boolean onCompletion() {
            Asset.CAMERA.addTrauma(power * TRAUMA_RATIO);
            Asset.getMusicHandler().playSound("explosion", 1, Asset.RANDOM.nextFloat() * 0.4f + 0.8f, 0);
            Explosion explosion = new Explosion("small", power, getCenterX(), getCenterY(), 16, 16);
            World.CURRENT.addEntity(explosion);
            destroy();
            return true;
        }
    };

    private Bomb() {
        this.name = "bomb";
        this.importance = 0.5f;
    }

    public void setPower(float magnitude) {
        this.power = (int) (4 * magnitude);
        this.setSize(4 + magnitude, 4 + magnitude);
    }

    @Override
    public void draw(Batch batch) {
        TextureRegion frame;
        float stateTime = Asset.TIME;
        if(Asset.TIME - timer.start >= timer.duration * 2/3)
            stateTime *= 2;
        frame = getAnimation().getKeyFrame(stateTime);
        batch.draw(frame, getX(), getY(), getWidth() / 2, getHeight() / 2,
                getWidth(), getHeight(), 1.0f, 1.0f, stateTime * 720 / (float) Math.PI);
    }

    @Override
    public void reset() {
        this.name = "bomb";
    }

    @Override
    void destroy() {
        super.destroy();
        POOL.free(this);
    }

    public static class Explosion extends Entity {
        Explosion(String name, float pwr, float cx, float cy, float w, float h) {
            this.name = name;
            this.setSize(w * pwr / 4, h * pwr / 4);
            this.setPosition(cx - getWidth() / 2, cy - getHeight() / 2);
            this.power = pwr;
        }

        @Override
        public void update(float delta) {
            super.update(delta);
            if (getAnimation().isAnimationFinished(stateTime))
                destroy();
        }

        @Override
        public void loadAnimation(World world) {
            Array<TextureRegion> regions = new Array<TextureRegion>();
            regions.addAll(Asset.getExplosionAtlas().findRegions(name));

            setAnimation(new Animation<TextureRegion>(0.04f, regions, Animation.PlayMode.NORMAL));
        }

        @Override
        public void collideWith(Entity e) {
            if(e instanceof Player) {
                Asset.VECTOR2.set(e.getCenterX() - getCenterX(), getHeight());
                Asset.VECTOR2.setLength(2.4f);
                e.d.set(Asset.VECTOR2);
            }
        }

        @Override
        public void checkCollisions() {
            float[] xs = {getX(), getCenterX(), getRight()},
                    ys = {getY(), getCenterY(), getTop()};
            for (float y : ys)
                for (float x : xs)
                    World.CURRENT.breakTile(x, y);
        }
    }
}
