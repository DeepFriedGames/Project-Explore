package com.shdwfghtr.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.shdwfghtr.asset.PaletteService;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.World;
import com.shdwfghtr.screens.GameScreen;

public class Enemy extends Entity {
    private static final Vector2 VECTOR2 = new Vector2();

	Enemy(String name) {
		super(name);
	}
	
	Enemy(String name, float x, float y) {
		super(name, x, y);
	}
	
	void initiateMovement() {
        Rectangle check = World.getTileBox(getCenterX(), getCenterY());
        Vector2 center = check.getCenter(VECTOR2);
        if (World.CURRENT.isBlocked(center.x, check.getY() - speed)) {
            setPosition(center.x - getWidth() / 2, check.getY());
            d.set(left ? -speed : speed, 0);
        } else if (World.CURRENT.isBlocked(check.getX() - speed,center.y)) {
            setPosition(check.getX(), center.y - getWidth() / 2);
            d.set(0, left ? speed : -speed);
        } else if (World.CURRENT.isBlocked(check.getX() + check.width + speed,center.y)) {
            setPosition(check.getX() + check.width - getHeight(), center.y - getWidth() / 2);
            d.set(0, left ? -speed : speed);
        } else if(World.CURRENT.isBlocked(center.x, check.getY() + check.height + speed)) {
            setPosition(center.x - getWidth() / 2, check.getY() + check.height - getHeight());
            d.set(left ? speed : -speed, 0);
        }
    }

    @Override
    public void draw(Batch batch) {
        setRegion(getAnimation().getKeyFrame(TimeService.GetTime()));
        setFlip(left, false);
        //this top if statement creates a flashing effect if the entity is hurt
        if(!hurt || MathUtils.randomBoolean()) {
            if (d.angleDeg() == 270 || d.angleDeg() == 90)
                batch.draw(this, getX(), getY(), getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), 1, 1, 0, d.angle() == (left ? 90 : 270));
            else
                batch.draw(this, getX(), getY(), getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), 1, 1, d.angle() + (left ? 180 : 0));
        }
    }

    static void geemerCollisionAI(Enemy e) {
        if(e.d.isZero())
            e.initiateMovement();
        float right = e.getRight(), top = e.getTop(),
                centerX = e.getCenterX(), centerY = e.getCenterY();
        if (e.d.angle() == 0) {
            if (!World.CURRENT.isBlocked(centerX, e.left ? top + e.speed : e.getY() - e.speed)) {
                e.setPosition(centerX, (e.left ? top : e.getY()) - e.getWidth() / 2);
                e.d.rotate90(e.left ? 1 : -1);
            }
            if (World.CURRENT.isBlocked(right + e.speed, centerY)) {
                e.setPosition(right - e.getHeight(), e.left ? top - e.getWidth() : e.getY());
                e.d.rotate90(e.left ? -1 : 1);
            }
        } else if (e.d.angle() == 180) {
            if (!World.CURRENT.isBlocked(centerX, e.left ? e.getY() - e.speed : top + e.speed)) {
                e.setPosition(centerX - e.getHeight(), (e.left ? e.getY() : top) - e.getWidth() / 2);
                e.d.rotate90(e.left ? 1 : -1);
            }
            if (World.CURRENT.isBlocked(e.getX() - e.speed, centerY)) {
                if(e.left) e.setY(top - e.getWidth());
                e.d.rotate90(e.left ? -1 : 1);
            }
        } else if (e.d.angle() == 90) {
            if (!World.CURRENT.isBlocked(e.left ? e.getX() - e.speed : right + e.speed, centerY)) {
                e.setPosition((e.left ? e.getX() : right) - e.getHeight() / 2, centerY);
                e.d.rotate90(e.left ? 1 : -1);
            }
            if (World.CURRENT.isBlocked(centerX, top + e.speed)) {
                e.setPosition(e.left ? e.getX() : right - e.getHeight(), top - e.getWidth());
                e.d.rotate90(e.left ? -1 : 1);
            }
        } else if (e.d.angle() == 270) {
            if (!World.CURRENT.isBlocked(e.left ? right + e.speed : e.getX() - e.speed, centerY)) {
                e.setPosition((e.left ? right : e.getX()) - e.getHeight() / 2, centerY - e.getWidth());
                e.d.rotate90(e.left ? 1 : -1);
            }
            if (World.CURRENT.isBlocked(centerX, e.getY() - e.speed)) {
                e.setPosition(e.left ? right - e.getHeight() : e.getX(), e.getY());
                e.d.rotate90(e.left ? -1 : 1);
            }
        }
        if(e.d.y != 0) e.setSize(e.getRegionHeight(), e.getRegionWidth());
        else e.setSize(e.getRegionWidth(), e.getRegionHeight());
        e.setOriginCenter();
    }

	@Override
    public void destroy() {
        GdxGame.audioService.playSound("explosion", 1, MathUtils.random(0.8f, 1.2f), 0);
		ParticleEffectPool.PooledEffect effect = GdxGame.particleService.obtain("explosion", false);
		PaletteService.colorEffect(effect, World.CURRENT.palette[8]);
		effect.setPosition(getCenterX(), getCenterY());
        GdxGame.particleService.add(effect);
		if(!isDead()) dropItem(getCenterX(), getCenterY());
		super.destroy();
	}

	private static void dropItem(float x, float y) {
		float rand = MathUtils.random();
		double probArmor = Math.exp(((double) -3 * Player.CURRENT.armor / Player.CURRENT.maxArmor));
		double probBigHealth = Math.exp(((double) -3 * Player.CURRENT.health / Player.CURRENT.maxHealth));
        double probSmallHealth = Math.exp(((double) -3 * (Player.CURRENT.health - 20) / Player.CURRENT.maxHealth));
		double probAmmo = Math.exp(((double) -3 * Player.CURRENT.missiles / Player.CURRENT.maxMissiles));
        String name;
		if(rand < probBigHealth)
			name = "O2_large";
		else if(rand < probSmallHealth)
			name = "O2_small";
		else if(rand < probAmmo)
			name = "ammo";
		else if(rand < probArmor)
			name = "armor";
        else return;

        PooledItem item = PooledItem.POOL.obtain();
        item.set("item_" + name, x, y);
        item.setDelete(false);
		((GameScreen) ((GdxGame) Gdx.app.getApplicationListener()).getScreen()).world.addEntity(item);
	}
	
	@Override
	public void collideWith(Entity entity) {
		if(entity instanceof Bullet || entity instanceof Bomb.Explosion)
			takeDamage(entity.power);
	}
}
