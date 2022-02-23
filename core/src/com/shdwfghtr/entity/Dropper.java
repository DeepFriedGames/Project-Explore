package com.shdwfghtr.entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.Tile;
import com.shdwfghtr.explore.World;

public class Dropper extends Enemy {
    private boolean FALL;
	
	public Dropper(float x, float y) {
        super("enemy_dropper", x, y);
		this.health = 10;
		this.power = 5;
		this.speed = 0.14f;
	}

    @Override
    public void update(float delta) {
        super.update(delta);
        if(FALL) {
            if(d.y > -9) d.y -= World.CURRENT.getGravity();
            d.x = Math.copySign(speed, Player.CURRENT.getCenterX() - getCenterX());
            
            if(World.CURRENT.isBlocked(getCenterX(), getY() + d.y)) {
                Rectangle box = World.getTileBox(getCenterX(), getY() + d.y);
                setY(box.y + box.height);
                FALL = false;
                d.set(left ? -speed : speed, 0);
                //spews rocks out in random directions
                    for(int deg=60; deg<=120; deg+=30) {
                        Rock rock = Rock.POOL.obtain();
                        rock.setSize(Asset.RANDOM.nextInt(2) + 6, Asset.RANDOM.nextInt(2) + 5);
                        rock.setPosition(getCenterX() - rock.getWidth() / 2, getCenterY() - rock.getHeight() / 2);
                        rock.d.setAngle(deg);
                        World.CURRENT.addEntity(rock);
                    }
            }
        } else if(World.CURRENT.isBlocked(getCenterX(), getTop() + World.CURRENT.getGravity()) &&
                Player.CURRENT.getTop() < getY() && Math.abs(Player.CURRENT.getCenterX() - getCenterX()) < 48)
            FALL = true;
    }

    @Override
	public void draw(Batch batch) {
        Array<TextureAtlas.AtlasRegion> regions = World.CURRENT.entityAtlas.findRegions(name);
        Animation.PlayMode playmode = Animation.PlayMode.NORMAL;
        float angle = d.angle();
        if(!FALL) {
            playmode = Animation.PlayMode.LOOP;
            regions.removeRange(2, 3);
        } else {
            if(left) angle = 0;
            else angle = 180;
        }
        
        setAnimation(new Animation<TextureRegion>(0.6f, regions, playmode));
        super.draw(batch);
    }
	
	@Override
	public void checkCollisions() {
        Enemy.geemerCollisionAI(this);
	}
    
    @Override
    public void takeDamage(float amount) {
        if(!hurt) Asset.getMusicHandler().playSound("enemy_damage");
        super.takeDamage(amount);
    }

    @Override
    public void respawn() {
        super.respawn();
        this.health = 10;
        this.setSize(14, 11);
        this.d.setAngle(0);
    }
    
    private static class Rock extends Enemy implements Pool.Poolable {
        private static final Pool<Rock> POOL = new Pool<Rock>() {
            @Override
            protected Rock newObject() {
                return new Rock();
            }
        };
        
        private Rock() {
            super("enemy_rock", 0, 0);
            this.power = 6;
            this.health = 1;
            this.speed = 3.2f;
            this.d.set(0, -speed);
        }

        @Override
        public void update(float delta) {
            super.update(delta);
            d.y -= World.CURRENT.getGravity();
            if(!Asset.CAMERA.getBox().overlaps(this.getBox()))
                setDelete(true);
            setRotation(d.angle());
        }

        @Override
        public void reset() {
            super.respawn();
            this.health = 1;
            this.setPosition(0, 0);
            this.d.setAngle(270);
            this.setSize(7, 6);
            World.CURRENT.removeEntity(this);
        }
    }
}
