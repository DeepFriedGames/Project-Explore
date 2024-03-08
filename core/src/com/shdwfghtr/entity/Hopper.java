package com.shdwfghtr.entity;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.World;

public class Hopper extends Enemy {
	private static final float TURN_PROB = 0.02f, JUMP_PROB = 0.2f;
	private static final float JUMP_HEIGHT = 4.2f;

	public Hopper(float x, float y) {
		super("enemy_hopper", x, y);
		this.power = 4;
		this.health = 8;
		this.speed = 0.8f;
	}
	
	@Override
	public void update(float delta) {
        super.update(delta);
		if(onGround()) {
			if(MathUtils.random() < TURN_PROB)
				left = !left;
			if(MathUtils.random() < JUMP_PROB)
				if(MathUtils.randomBoolean()) {
					d.set(speed, JUMP_HEIGHT * 0.6f);
				} else
					d.set(speed, JUMP_HEIGHT);
		} else if(MathUtils.randomBoolean())
			d.x = speed;
		
		if(left) d.x = -Math.abs(d.x);
	}
	
	@Override
	public void draw(Batch batch) {	
        TextureRegion texture;
		if(onGround()) texture = getAnimation().getKeyFrame(TimeService.GetTime());
		else texture = getAnimation().getKeyFrame(0);
		if((!texture.isFlipX() && left) || (texture.isFlipX() && !left))
			texture.flip(true, false);
		
		if(!hurt || MathUtils.randomBoolean())
			batch.draw(texture, getX() - 1, getY());
	}

	@Override
	public void checkCollisions() {
		Rectangle box;
		d.sub(0, World.CURRENT.getGravity());
		float[] xs = {getX(), getRight()};
		float[] ys = {getY(), getTop()};
		for(float y : ys) {
			if(d.x > 0 && World.CURRENT.isBlocked(getRight() + d.x, y)) {
				box = World.getTileBox(getRight() + d.x, y);
				setPosition(box.x - getWidth(), getY());
				d.x = 0;
			}
			if(d.x < 0 && World.CURRENT.isBlocked(getX() + d.x, y)) {
				box = World.getTileBox(getX() + d.x, y);
				setPosition(box.x + box.width, getY());
				d.x = 0;
			}
		}
		for(float x : xs) {
			if(d.y <= 0 && World.CURRENT.isBlocked(x + d.x, getY() + d.y)) {
				box = World.getTileBox(x + d.x, getY() + d.y);
				setPosition(getX(), box.y + box.height);
				d.y = 0;
			}
			if(d.y >= 0 && World.CURRENT.isBlocked(x + d.x, getTop() + d.y)) {
				box = World.getTileBox(x + d.x, getTop() + d.y);
				setPosition(getX(), box.y - getHeight());
				d.y = -0.001f;
			}
		}
	}

    @Override
    public void takeDamage(float amount) {
        if(!hurt) GdxGame.audioService.playSound("enemy_damage");
        super.takeDamage(amount);
    }

	@Override
	public void respawn() {
		this.health = 8;
        super.respawn();
	}
}
