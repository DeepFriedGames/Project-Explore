package com.shdwfghtr.entity;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.World;

/**
 * Created by Stuart on 8/28/2015.
 * A turret is an enemy which fires bullets in the @Player 's direction
 */
public class Turret extends Enemy {
    private final Vector2 pivot = new Vector2();
    private final int angle;
    private float arm_angle;
    
    public Turret(Rectangle tile, World world) {
        super("enemy_smart_turret", tile.x, tile.y);
        if(Asset.RANDOM.nextBoolean()) this.name = "enemy_turret";
        this.health = 12;
        this.speed = 0;
        this.power = 0;
        if(world.isBlocked(tile.x - 1, tile.y + tile.height / 2)) {
            this.angle = 270;
            this.pivot.set(getX() + 2, getCenterY() - 2); //this represents the arm's pivot point
        } else if(world.isBlocked(tile.x + tile.width + 1, tile.y + tile.height / 2)) {
            this.angle = 90;
            this.pivot.set(getRight() - 2, getCenterY() - 2); //this represents the arm's pivot point
        } else if(world.isBlocked(tile.x + tile.width / 2, tile.y + tile.height + 1)) {
            this.angle = 180;
            this.pivot.set(getCenterX(), getTop() - 4); //this represents the arm's pivot point
        } else {
            this.angle = 0;
            this.pivot.set(getCenterX(), getY()); //this represents the arm's pivot point
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if(name.contains("smart")) {
            float dist = Player.CURRENT.getCenter().dst(pivot);
            float frames = dist / Projectile.SPEED;
            Asset.VECTOR2.set(Player.CURRENT.getCenter());
            Asset.VECTOR2.add(Player.CURRENT.d.x * frames, Player.CURRENT.d.y * frames);
        } else
            Asset.VECTOR2.set(Player.CURRENT.getCenter());
        
        arm_angle = Asset.VECTOR2.sub(pivot).angle();
        if(arm_angle < angle) arm_angle = angle;
        else if(arm_angle > angle + 180) arm_angle = angle + 180;
    }

//    @Override
//    public void draw(Batch batch) {
//        TextureRegion arm = Asset.getEntityAtlas().findRegion("enemy_turret_arm");
//        TextureRegion tr = getAnimation().getKeyFrame(Asset.TIME);
//
//        if (!hurt || Asset.RANDOM.nextBoolean()) {
//            batch.draw(arm, pivot.x, pivot.y, 0, arm.getRegionHeight() / 2,
//                    arm.getRegionWidth(), arm.getRegionHeight(), 1.0f, 1.0f, arm_angle);
//            batch.draw(tr, x, y, width / 2, height / 2, width, height, 1.0f, 1.0f, angle);
//        }
//    }
    
    public void fire() {
        Projectile bullet = Projectile.POOL.obtain();
        bullet.setPosition(pivot.x, pivot.y);
        bullet.d.set(0, bullet.speed);
        bullet.d.setAngle(arm_angle);
        World.CURRENT.addEntity(bullet);
    }
    
    private static class Projectile extends Enemy implements Pool.Poolable {
        private static final Pool<Projectile> POOL = new Pool<Projectile>() {
            @Override
            protected Projectile newObject() {
                return new Projectile();
            }
        };
        private static final float SPEED = 3.8f;
        
        private Projectile() {
            super("enemy_turret_bullet", 0, 0);
            this.power = 1;
            this.health = 1;
            this.speed = SPEED;
        }

        @Override
        public void reset() {
            super.respawn();
            setBounds(0, 0, 2, 2);
            health = 1;
            d.setZero();
            World.CURRENT.removeEntity(this);
        }
    }
}
