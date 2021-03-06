package com.shdwfghtr.entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.World;

public abstract class Switch extends Entity {

	private Animation<TextureRegion> unlockedAnimation;
	private Animation<TextureRegion> lockedAnimation;

	protected Switch() {
		super("door_switch_locked");
		this.setSize(16, 16);
		this.importance = 0.5f;
	}

	public void trigger() {
		name = name.replace("_locked", "");
		setAnimation(unlockedAnimation);
	}

	@Override
	public void loadAnimation(World world) {
		String unlockedName = this.name.replace("_locked", "");
		String lockedName = unlockedName.concat("_locked");

		this.unlockedAnimation = new Animation<TextureRegion>(0.1f,
				world.entityAtlas.findRegions(unlockedName), Animation.PlayMode.NORMAL);
		this.lockedAnimation = new Animation<TextureRegion>(0.1f,
				world.entityAtlas.findRegions(lockedName), Animation.PlayMode.NORMAL);

		setAnimation(this.lockedAnimation);

	}

	@Override
	public void collideWith(Entity e) {
		if(e instanceof Bullet) {
			Asset.getMusicHandler().playSound("switch");
			e.destroy();
			trigger();
		}
	}
}
