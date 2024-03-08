package com.shdwfghtr.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.shdwfghtr.asset.DataService;
import com.shdwfghtr.asset.TimeService;

import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.Tile;
import com.shdwfghtr.explore.World;
import com.shdwfghtr.screens.GameScreen;

public class Door extends Entity {
	private boolean open = false;
	private float animationTime;
	final TimeService.Timer openTimer = new TimeService.Timer(5) {
        @Override
        public boolean onCompletion() {
            if (getBox().overlaps(Player.CURRENT.getBox())) {
                if(name.contains("horizontal")) {
                    if (Player.CURRENT.getCenterX() > getCenterX()) Player.CURRENT.setX(getRight());
                    else Player.CURRENT.setX(getX() - Player.CURRENT.getWidth() - 1);
                } else {
                    if (Player.CURRENT.getCenterY() > getCenterY()) Player.CURRENT.setY(getTop());
                    else Player.CURRENT.setY(getY() - Player.CURRENT.getHeight() - 1);
                    
                }
            }
            setOpen(false);
            return true;
        }
    };
	private Animation<TextureRegion> unlockedAnimation;
    private Animation<TextureRegion> lockedAnimation;

    public Door(String name, float x, float y) {
		super(name, x, y, GdxGame.textureAtlasService.findEntityRegion(name).getRegionWidth(), GdxGame.textureAtlasService.findEntityRegion(name).getRegionHeight());
        drawLayer = DrawLayer.FOREGROUND;
	}

	@Override
    public void loadAnimation(World world) {
        String unlockedName = this.name.replace("_locked", "");
        String lockedName = unlockedName.concat("_locked");

        this.unlockedAnimation = new Animation<TextureRegion>(0.1f,
                GdxGame.textureAtlasService.findEntityRegions(unlockedName), Animation.PlayMode.NORMAL);
        this.lockedAnimation = new Animation<TextureRegion>(0.1f,
                GdxGame.textureAtlasService.findEntityRegions(lockedName), Animation.PlayMode.NORMAL);

        if(this.name.contains("locked"))
            setAnimation(this.lockedAnimation);
        else
            setAnimation(this.unlockedAnimation);

    }

    @Override
	public void update(float delta) {
		if(open) animationTime += delta;
		else animationTime -= delta;
		if(animationTime < 0) animationTime = 0;
		if(isLocked() && !name.contains("locked")) {
		    name = name.concat("_locked");
		    setAnimation(unlockedAnimation);
        }
        else if(!isLocked() && name.contains("locked")) {
		    name = name.replace("_locked", "");
            setAnimation(lockedAnimation);
        }

        for(float x = getX(); x < getRight(); x += Tile.WIDTH)
            for(float y = getY(); y < getTop(); y += Tile.HEIGHT)
                if(open) World.CURRENT.setChar(' ', x, y);
                else World.CURRENT.setChar('=', x, y);
	}
	
	@Override
	public void draw(Batch batch) {
            batch.draw(getAnimation().getKeyFrame(animationTime), getX(), getY());
    }
	
	@Override
	public void collideWith(Entity e) {
        if ((e instanceof Bullet || e instanceof Bomb.Explosion) && !open) {
            e.destroy();
            setOpen(true);
            openTimer.reset();
        }
        else if(!(e instanceof Player)) {
            if(!GdxGame.getCamera().getBox().contains(e.getBox())) e.setDelete(true);
        }
    }
    
    public boolean isLocked() {	return DataService.load(World.CURRENT, this);
    }

	private void setOpen(boolean b) {
		if(!isLocked()) {
            if(GdxGame.getCamera().getBox().overlaps(getBox())) {
                if (b) GdxGame.audioService.playSound("door_open", 1, 1, (getCenterX() - Player.CURRENT.getCenterY()) / 16f);
                else GdxGame.audioService.playSound("door_close", 1, 1, (getCenterX() - Player.CURRENT.getCenterY()) / 16f);
            }
			if(b) animationTime = 0;
			else animationTime = getAnimation().getFrameDuration() * 3;
			open = b;
		}
	}

	public void setLocked(boolean locked) {
		if(name.contains("_locked") && !locked) this.name = name.replace("_locked", "");
        else if(!name.contains("_locked") && locked) this.name = name.concat("_locked");
        DataService.save(this);
	}
}
