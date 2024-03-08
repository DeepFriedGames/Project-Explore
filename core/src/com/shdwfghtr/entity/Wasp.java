package com.shdwfghtr.entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.explore.GdxGame;

public class Wasp extends Enemy implements Pool.Poolable {
    public static final Pool<Wasp> POOL = new Pool<Wasp>() {
        @Override
        protected Wasp newObject() {
            return new Wasp();
        }
    };
    
    private Wasp() {
        super("enemy_wasp");
        this.health = 3;
        this.power = 18;
        this.speed = 0.6f;
    }

    @Override
    void setAnimation(Animation<TextureRegion> animation) {
        animation.setFrameDuration(0.05f);
        super.setAnimation(animation);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if(d.x == 0) {
            left = Player.CURRENT.getCenterX() < getCenterX();
            
            if (Math.abs(getCenterY() - Player.CURRENT.getCenterY()) < getHeight() / 2)
                if (left) d.set(-3 * speed, 0);
                else d.set(3 * speed, 0);
        }
    }

    @Override
    public void draw(Batch batch) {
        TextureRegion tr = getAnimation().getKeyFrame(TimeService.GetTime());
        if((!tr.isFlipX() && !left) || (tr.isFlipX() && left))
            tr.flip(true, false);
        batch.draw(tr, getX(), getY());
    }

    @Override
    public void respawn() {
        super.respawn();
        this.health = 3;
    }

    @Override
    public void takeDamage(float amount) {
        if(!hurt) GdxGame.audioService.playSound("enemy_damage");
        super.takeDamage(amount);
    }

    @Override
    public void reset() {
        setPosition(0, 0);
    }
    
    @Override
    public void destroy() {
        super.destroy();
        POOL.free(this);
    }
}
