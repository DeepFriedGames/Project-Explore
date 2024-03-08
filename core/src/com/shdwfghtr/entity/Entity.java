package com.shdwfghtr.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.World;

public class Entity extends Sprite {
    static final Pool<HurtTimer> HURT_TIMER_POOL = new Pool<HurtTimer>() {
        @Override
        protected HurtTimer newObject() {
            return new HurtTimer();
        }
    };
    private static final Vector2 VECTOR2 = new Vector2();

    private Animation<TextureRegion> animation;
    private boolean delete;

    public final Vector2 d = new Vector2(); //vector for storing speed
	public boolean left;
    boolean hurt;
    public DrawLayer drawLayer = DrawLayer.BACKGROUND;
	public float health, speed, power;  //speed is in meters per second
    public String name = "";
    public boolean persistent = false;

    float stateTime = 0, importance;

    Entity() {
		super();
	}

	public Entity(String name) {
	    super(GdxGame.textureAtlasService.findEntityRegion(name));
	    this.name = name;
    }

    Entity(String name, float x, float y, float width, float height) {
	    this(name);
		setBounds(x, y, width, height);
	}

    public Entity(String name, float x, float y) {
        this(name);
        setBounds(x, y, getRegionWidth(), getRegionHeight());
    }

    private boolean hasAnimation() { return animation != null; }

    Animation<TextureRegion> getAnimation() {
        return animation;
    }

    void setAnimation(Animation<TextureRegion> animation) {
        this.animation = animation;
    }

    public void loadAnimation(World world) {
        Array<TextureRegion> frames = new Array<>();
        Array<TextureAtlas.AtlasRegion> regions = GdxGame.textureAtlasService.findEntityRegions(name);
        if(regions != null)
            frames.addAll(regions);

        setAnimation(new Animation<>(0.2f, frames, Animation.PlayMode.LOOP_PINGPONG));
    }

	public void draw(Batch batch) {
        try {
            if (hasAnimation())
                setRegion(animation.getKeyFrame(stateTime));

            setFlip(left, false);

            if (!hurt || MathUtils.randomBoolean())
                super.draw(batch);
        } catch (Exception e) {
            System.out.println(name + " cannot be drawn");
            e.printStackTrace();
        }
    }

    public void update(float delta) {
		checkCollisions();
		if(Math.abs(d.x) < 0.05f) d.x = 0;
        translate(d.x, d.y);
        stateTime += delta;
	}

	public void collideWith(Entity e) {}

	void checkCollisions() {}

	void takeDamage(float amount) {
		if(!hurt) {
			hurt = true;
			health -= amount;
			HurtTimer hurtTimer = HURT_TIMER_POOL.obtain();
            hurtTimer.entity = this;
            hurtTimer.duration = 0.2f;
            hurtTimer.reset();
		}
		if(!delete && health <= 0) destroy();
	}

	public float getRight() {
		return getX() + getWidth();
	}

	public float getTop() {	
		return getY() + getHeight(); 
	}

	public Vector2 getCenter() {
        return getBoundingRectangle().getCenter(VECTOR2);
    }

	public float getCenterX() {
		return getX() + getWidth() / 2;
	}

	public float getCenterY() {
		return getY() + getHeight() / 2;
	}

	public Rectangle getBox() {
        return getBoundingRectangle();
	}

    public float getImportance() {
        return importance;
    }

    public void setImportance(float importance) {
        this.importance = importance;
    }

	public boolean isDead() {
		return delete;
	}

	public void destroy() {
		setDelete(true);
	}

	void setDelete(boolean b) {
		delete = b;
	}

    public void respawn() {
        setDelete(false);
        hurt = false;
        d.setZero();
        stateTime = 0;
    }

	boolean onGround() {
		return World.CURRENT.isBlocked(getX(), getY() - World.CURRENT.getGravity() * Gdx.graphics.getRawDeltaTime())
				|| World.CURRENT.isBlocked(getCenterX(), getY() - World.CURRENT.getGravity() * Gdx.graphics.getRawDeltaTime())
				|| World.CURRENT.isBlocked(getRight(), getY() - World.CURRENT.getGravity() * Gdx.graphics.getRawDeltaTime());
    }
    
    public String getName() {
        return name;
    }

    protected static class HurtTimer extends TimeService.Timer implements Pool.Poolable {
        public Entity entity;

        HurtTimer() {
            super(0.2f);
        }

        @Override
        public boolean onCompletion() {
            entity.hurt = false;
            HURT_TIMER_POOL.free(this);
            return true;
        }
    }

    @SuppressWarnings("unused")
    public enum DrawLayer {
        BACKGROUND, NORMAL, FOREGROUND
    }
}

