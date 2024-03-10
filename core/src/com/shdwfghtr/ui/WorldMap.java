package com.shdwfghtr.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.Sector;
import com.shdwfghtr.explore.World;

public class WorldMap extends Actor {
    private final World world;
    private final TextureRegion[][] regions;
    private int xCurrent, yCurrent;

    public WorldMap(World world){
        this.world = world;
        this.regions = new TextureRegion[world.getHeight()][world.getWidth()];
        for(int yi=0; yi < world.getHeight(); yi++){
            for(int xi=0; xi < world.getWidth(); xi++){
                Sector sector = world.sectorMap[yi][xi];
                regions[yi][xi] = GdxGame.textureAtlasService.findSectorRegion(sector.name);
            }
        }
    }

    @Override
    public void act(float delta) {
        //set the current sector
        xCurrent = (int) Math.floor(Player.CURRENT.getCenterX() / Sector.pWIDTH);
        yCurrent = (int) Math.floor(Player.CURRENT.getCenterY() / Sector.pHEIGHT);

        //make the current sector flash
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        for(int y = 0; y < world.getHeight(); y++) {
            for(int x = 0; x < world.getWidth(); x++) {
                float drawX = this.getX() + x * Sector.WIDTH;
                float drawY = this.getY() + y * Sector.HEIGHT;
                    batch.draw(regions[y][x]
                            , drawX, drawY
                            , Sector.WIDTH, Sector.HEIGHT);
            }
        }
    }

    public class WorldMiniMap extends Actor {
        @Override
        public void draw(Batch batch, float parentAlpha) {
            for(int y= yCurrent - 1; y <= yCurrent + 1; y++) {
                for(int x= xCurrent - 2; x <= xCurrent + 2; x++) {
                    float drawX = this.getX() + x * Sector.WIDTH;
                    float drawY = this.getY() + y * Sector.HEIGHT;
                    if(x >= 0 && y >= 0 && x < world.getWidth() && y < world.getHeight()) {
                        batch.draw(regions[y][x]
                                , drawX, drawY
                                , Sector.WIDTH, Sector.HEIGHT);
                    }
                }
            }
        }
    }
}
