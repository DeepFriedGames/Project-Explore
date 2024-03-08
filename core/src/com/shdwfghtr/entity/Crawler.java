package com.shdwfghtr.entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.shdwfghtr.explore.GdxGame;

public class Crawler extends Enemy {
    Crawler() {
        this(0, 0);
    }

    public Crawler(float x, float y) {
        super("enemy_crawler", x, y);
        this.health = 2;
        this.power = 8;
        this.speed = 0.45f;
    }

    @Override
    void setAnimation(Animation<TextureRegion> animation) {
        animation.setPlayMode(Animation.PlayMode.LOOP);
        animation.setFrameDuration(0.1f);
        super.setAnimation(animation);
    }

    @Override
    void checkCollisions() {
        Enemy.geemerCollisionAI(this);
    }

    @Override
    public void takeDamage(float amount) {
        if(!hurt) GdxGame.audioService.playSound("enemy_damage");
        super.takeDamage(amount);
    }

    @Override
    public void respawn() {
        super.respawn();
        this.health = 2;
        this.setSize(16, 8);
        this.d.setAngle(0);
    }
}
