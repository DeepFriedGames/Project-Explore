package com.shdwfghtr.entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import com.shdwfghtr.explore.World;

public class Spawner {

	private static final Vector2 VECTOR2 = new Vector2();
	public final Entity entity;
    private float x, y, width, height;
	
	public Spawner(Entity e) {
		this.entity = e;
		this.set(e.getX(), e.getY(), e.getWidth(), e.getHeight());
	}

	public void spawn() {
		try {
			entity.respawn();
			entity.setBounds(x, y, width, height);
			entity.left = MathUtils.randomBoolean();
			World.CURRENT.addEntity(entity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    private void set(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
    
    public Vector2 getCenter() {
        return VECTOR2.set(x + width/2, y + height/2);
    }
}
