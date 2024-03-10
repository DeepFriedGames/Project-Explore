package com.shdwfghtr.explore;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.entity.Entity;
import com.shdwfghtr.entity.Player;

public class GameCamera extends OrthographicCamera {
	public static final int WIDTH = Sector.pWIDTH;
	public static final int HEIGHT = Sector.WIDTH * 10;
	private static final Rectangle RECTANGLE = new Rectangle();
	private static final Vector3 POSITION = new Vector3();  //target camera position
	private static final Vector3 SHAKE = new Vector3();  //amount of camera shake to be applied
	private static final Perlin PERLIN = new Perlin();	//Noise used for shaking the camera
	private static final float H_WEIGHT = 0.5f, UP_WEIGHT = 0.1f, DN_WEIGHT = 0.2f;	//% distance to target each frame by direction

	private float padding = Tile.WIDTH, trauma = 0, maxAngle = 4, maxOffsetX = 12, maxOffsetY = 9;

	public GameCamera() {
		super(WIDTH, HEIGHT);
	}

	@Override
	public void update() {
		super.update();
		if(Player.CURRENT != null) {
			//determine the target camera position as a weighted average of points of interest
			float maxDx = (viewportWidth - 2 * padding) * zoom,
					maxDy = (viewportHeight - 2 * padding) * zoom;
					
			float sumX = 0, sumY = 0, sumXWeight = 0, sumYWeight = 0;
			
			Entity[] interests = World.CURRENT.getInterests();
			for(Entity entity : interests) {
				float x = entity.getCenterX(),
						y = entity.getCenterY();

				float dx = Math.abs(x - Player.CURRENT.getCenterX()),
						dy = Math.abs(y - Player.CURRENT.getCenterY());
					
				if(dx < maxDx) {
					//assign each a weight of proximity * importance
					float wX = (1 - dx / maxDx) * entity.getImportance();
							
					sumX += x * wX;
					sumXWeight += wX;
				}
				if(dy < maxDy) {
					float wY = (1 - dy / maxDy) * entity.getImportance();

					sumY += y * wY;
					sumYWeight += wY;
				}
			}
			//calculate weighted average of entity position * weight
			POSITION.x = sumX / sumXWeight;
			POSITION.y = sumY / sumYWeight;
			
			//constrain target position to edges of sectors
			Sector sector = World.CURRENT.getSector(Player.CURRENT.getCenterX(), Player.CURRENT.getCenterY());
			if(!sector.UP && POSITION.y + viewportHeight * zoom / 2 > sector.getTop())
				POSITION.y = sector.getTop() - viewportHeight * zoom / 2;
			if(!sector.DOWN && POSITION.y - viewportHeight * zoom / 2 < sector.y)
				POSITION.y = sector.y + viewportHeight * zoom / 2;
			if(!sector.RIGHT && POSITION.x + viewportWidth * zoom / 2 > sector.getRight())
				POSITION.x = sector.getRight() - viewportWidth * zoom / 2;
			if(!sector.LEFT && POSITION.x - viewportWidth * zoom / 2 < sector.x)
				POSITION.x = sector.x + viewportWidth * zoom / 2;

			//smoothly move camera to target position using asymptotic averaging
			float dx = POSITION.x - position.x, dy = POSITION.y - position.y;
			position.x += dx * H_WEIGHT;
			position.y += dy * (dy > 0 ? UP_WEIGHT : DN_WEIGHT);

			//camera shake
			float time = TimeService.GetTime();
			float shake = trauma * trauma;  //trauma cubed "feels" best
			float angle = maxAngle * shake * PERLIN.getFloat(time, -1, 1);
			SHAKE.x = maxOffsetX * shake * PERLIN.getFloat(time + 1, -1, 1);
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
