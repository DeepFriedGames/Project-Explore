package com.shdwfghtr.explore;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.entity.Player;

public class GameCamera extends OrthographicCamera {
	private static final Rectangle RECTANGLE = new Rectangle();
	private static final Vector3 SHAKE = new Vector3();  //amount of camera shake to be applied
	private static final Perlin PERLIN = new Perlin();	//Noise used for shaking the camera
	private static final Vector3 target = new Vector3();  //target camera position

	private float trauma = 0;

	public GameCamera(float viewportWidth, float viewportHeight) {
		super(viewportWidth, viewportHeight);
	}

	@Override
	public void update() {
		super.update();
		if(Player.CURRENT != null) {
			target.set(Player.CURRENT.getCenter(), 0);

			//constrain target position to edges of sectors
			Sector sector = World.CURRENT.getSector(target.x, target.y);
			if(!sector.UP && target.y + viewportHeight * zoom / 2 > sector.getTop())
				target.y = sector.getTop() - viewportHeight * zoom / 2;
			if(!sector.DOWN && target.y - viewportHeight * zoom / 2 < sector.y)
				target.y = sector.y + viewportHeight * zoom / 2;
			if(!sector.RIGHT && target.x + viewportWidth * zoom / 2 > sector.getRight())
				target.x = sector.getRight() - viewportWidth * zoom / 2;
			if(!sector.LEFT && target.x - viewportWidth * zoom / 2 < sector.x)
				target.x = sector.x + viewportWidth * zoom / 2;

			//smoothly move camera to target position using asymptotic averaging
			float dx = target.x - position.x, dy = target.y - position.y;
			position.add(dx, dy, 0);

			//camera shake
			float time = TimeService.GetTime();
			float shake = trauma * trauma;  //trauma cubed "feels" best
			float maxAngle = 4;
			float angle = maxAngle * shake * PERLIN.getFloat(time, -1, 1);
			float maxOffsetX = 12;
			SHAKE.x = maxOffsetX * shake * PERLIN.getFloat(time + 1, -1, 1);
			float maxOffsetY = 9;
			SHAKE.y = maxOffsetY * shake * PERLIN.getFloat(time + 2, -1, 1);

			setAngle(angle);
			position.add(SHAKE);			
			addTrauma(-0.02f);  //trauma decreases linearly over time
		}
	}

	public void addTrauma(float amount) {
		this.trauma += amount;
		this.trauma = MathUtils.clamp(trauma, 0, 1);  //maintain trauma in [0,1]
	}

	public Rectangle getBox() {
		return RECTANGLE.set(getX(), getY(), viewportWidth * zoom, viewportHeight * zoom);
	}

	public float getX() {
		return position.x - viewportWidth * zoom / 2;
	}

	public float getY() {
		return position.y - viewportHeight * zoom / 2;
	}

	public float getRight() {
		return position.x + viewportWidth * zoom / 2;
	}

	public float getTop() {
		return position.y + viewportHeight * zoom / 2;
	}
	
	private void setAngle(float angle) {
		up.set(0, 1, 0);
		direction.set(0, 0, -1);
		rotate(angle);
		
	}
}
