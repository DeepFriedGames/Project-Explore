package com.shdwfghtr.entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.shdwfghtr.explore.GdxGame;

/**
 * Created by Stuart on 7/6/2015.
 * A snail is a type of crawler enemy which makes the tile below it @Slick, reducing the friction
 * of the tile.
 */
public class Snail extends Enemy {

    private static final Rectangle RECTANGLE = new Rectangle();

    Snail() {
        this(0, 0);
    }

    public Snail(float x, float y) {
        super("enemy_snail", x, y);
        this.health = 4;
        this.power = 0;
        this.speed = 0.1f;
    }

    @Override
    void setAnimation(Animation<TextureRegion> animation) {
        animation.setFrameDuration(0.5f);
        animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        super.setAnimation(animation);
    }

    @Override
    public void checkCollisions() {
        Enemy.geemerCollisionAI(this);
        if(onGround()) Slick.spawn(getCenterX(), getY() - speed);
            
    }

    @Override
    public void collideWith(Entity entity) {
        if (entity instanceof Bullet || entity instanceof Bomb.Explosion) {
            Rectangle hitBox = RECTANGLE;
            if(left) {
                if (d.angle() == 0)
                    hitBox.set(getCenterX(), getCenterY(), getWidth() / 2, getHeight() / 2);
                else if (d.angle() == 90)
                    hitBox.set(getX(), getCenterY(), getWidth(), getHeight() / 2);
                else if (d.angle() == 180)
                    hitBox.set(getX(), getY(), getWidth() / 2, getHeight() / 2);
                else if (d.angle() == 270)
                    hitBox.set(getCenterX(), getY(), getWidth() / 2, getHeight() / 2);
            } else {
                if (d.angle() == 0)
                    hitBox.set(getCenterX(), getY(), getWidth() / 2, getHeight() / 2);
                else if (d.angle() == 90)
                    hitBox.set(getCenterX(), getCenterY(), getWidth(), getHeight() / 2);
                else if (d.angle() == 180)
                    hitBox.set(getX(), getCenterY(), getWidth() / 2, getHeight() / 2);
                else if (d.angle() == 270)
                    hitBox.set(getX(), getY(), getWidth() / 2, getHeight() / 2);
            }
            if (hitBox.overlaps(entity.getBox())) takeDamage(entity.power);
        }
    }

    @Override
    void takeDamage(float amount) {
        if(!hurt) GdxGame.audioService.playSound("damage1");
        super.takeDamage(amount);
    }

    @Override
    public void respawn() {
        super.respawn();
        this.health = 4;
        this.d.setAngle(0);
    }
}
