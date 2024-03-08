package com.shdwfghtr.asset;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.Sector;
import com.shdwfghtr.explore.Tile;
import com.shdwfghtr.explore.World;

public class TextureAtlasService implements Disposable {
    private final TextureAtlas environmentAtlas, bgAtlas, explosionAtlas, uiAtlas;
    public TextureAtlas entityAtlas, sectorAtlas;

    public TextureAtlasService(AssetManagerService manager){
        bgAtlas = manager.GetResource("atlas/BG.atlas", TextureAtlas.class);
        environmentAtlas = manager.GetResource("atlas/Environment.atlas", TextureAtlas.class);
        entityAtlas = manager.GetResource("atlas/Entity.atlas", TextureAtlas.class);
        explosionAtlas = manager.GetResource("atlas/Explosions.atlas", TextureAtlas.class);
        uiAtlas = manager.GetResource("atlas/UI.atlas", TextureAtlas.class);
    }

    public TextureRegion findEntityRegion(String name){
        return entityAtlas.findRegion(name);
    }

    public Array<TextureAtlas.AtlasRegion> findEntityRegions(String name) {
        return entityAtlas.findRegions(name);
    }

    public Array<TextureAtlas.AtlasRegion> findExplosionRegions(String name) {
        return explosionAtlas.findRegions(name);
    }

    public TextureRegion findBackgroundRegion(String name) {
        return bgAtlas.findRegion(name);
    }

    public TextureRegion findUIRegion(String name) {
        return uiAtlas.findRegion(name);
    }

    public Array<? extends TextureRegion> findEnvironmentRegions(String name) {
        return environmentAtlas.findRegions(name);
    }

    public TextureRegion findEnvironmentRegion(String name) {
        return environmentAtlas.findRegion(name);
    }

    public void generateSectorAtlas(World world) {
        Pixmap.Format format = Pixmap.Format.RGB565;
        int width = world.getWidth(), height = world.getHeight();
        PixmapPacker packer =
                new PixmapPacker(width * Sector.WIDTH, height * Sector.HEIGHT,
                        format, 2, true);


        for(int yi=0; yi < height; yi++)
            for(int xi=0; xi < width; xi++) {
                Sector sector = world.getSector(xi, yi);
                if(packer.getPageIndex(sector.name) < 0) {
                    Pixmap pixmap = new Pixmap(Sector.WIDTH, Sector.HEIGHT, format);

                    if (sector.name.contains("dead")) {
                        TextureRegion tr = GdxGame.textureAtlasService.findUIRegion("hud_empty_cell");
                        Texture tex = tr.getTexture();
                        TextureData texData = tex.getTextureData();
                        if (!texData.isPrepared()) texData.prepare();
                        Pixmap texMap = texData.consumePixmap();

                        for (int ty = 0; ty < Sector.HEIGHT; ty++)
                            for (int tx = 0; tx < Sector.WIDTH; tx++)
                                pixmap.drawPixel(tx, ty,
                                        texMap.getPixel(tr.getRegionX() + tx, tr.getRegionY() + ty));

                    } else {
                        pixmap.setColor(world.palette[1]);
                        pixmap.fill();
                        pixmap.setColor(Color.WHITE);
                        //color in each pixel is a tile
                        for (int ty = 0; ty < Sector.HEIGHT; ty++)
                            for (int tx = 0; tx < Sector.WIDTH; tx++)
                                if (Tile.isSolid(sector.getChar(tx, Sector.HEIGHT - ty - 1)))
                                    pixmap.drawPixel(tx, ty);
                    }
                    //pack the pixmap
                    packer.pack(sector.name, pixmap);
                }
            }

        TextureAtlas atlas = packer.generateTextureAtlas(
                Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, false);
        packer.dispose();
        sectorAtlas = atlas;
    }

    @Override
    public void dispose() {
        entityAtlas.dispose();
        environmentAtlas.dispose();
        bgAtlas.dispose();
        explosionAtlas.dispose();
        uiAtlas.dispose();
    }

    public Array<TextureAtlas.AtlasRegion> getEntityRegions() {
        return entityAtlas.getRegions();
    }

    public TextureRegion findSectorRegion(String name) {
        return sectorAtlas.findRegion(name);
    }
}
