package com.shdwfghtr.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.shdwfghtr.asset.PaletteService;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.Sector;
import com.shdwfghtr.explore.World;
import com.shdwfghtr.explore.WorldLoader;

public class WorldUIGroup {
    public final String worldName;
    public final WorldLoader worldLoader;
    public final WorldImage worldImage;
    public final WorldClickDialog dialog;

    public WorldUIGroup(WorldLoader worldLoader) {
        this.worldLoader = worldLoader;
        this.worldName = "World " + worldLoader.world.name;

        this.worldImage = this.new WorldImage();
        this.dialog = this.new WorldClickDialog();

        this.worldImage.addListener(this.new WorldClickListener());
    }

    public TextureRegion getWorldColorTextureRegion() {
        String worldType = World.getType(worldLoader.world.index);
        TextureRegion textureRegion = GdxGame.textureAtlasService.findEnvironmentRegion(worldType);
        PaletteService.recolorTextureRegion(textureRegion
                , PaletteService.getPalette("environment"), worldLoader.world.palette);
        return textureRegion;
    }

    public class WorldImage extends Image {
        WorldImage() {
            super(WorldUIGroup.this.getWorldColorTextureRegion());

            float variance = 1 - WorldUIGroup.this.worldLoader.area / World.AVG_AREA;
            variance *= 32;
            float worldSize = getDrawable().getMinWidth() * (2.25f + variance);
            setSize(worldSize, worldSize);
        }
    }

    public class WorldClickListener extends ClickListener {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            WorldUIGroup.this.dialog.addAction(Actions.moveTo(x, y));
            WorldUIGroup.this.dialog.show(event.getStage());
        }
    }

    public class WorldInformationLabel extends Label {
        public WorldInformationLabel() {
            super("Type:  " + World.getType(worldLoader.world.index).replace("planet_", "").toUpperCase() + '\n' +
                    "Gravity:  " + Math.round(worldLoader.world.gravity * 100) + "%\n" +
                    "Atmosphere:  " + Math.round(worldLoader.world.atmosphere * 100) + '%'
                    , GdxGame.uiService.getSkin());
        }
    }

    private class WorldClickDialog extends Dialog {
        private WorldClickDialog() {
            super(WorldUIGroup.this.worldName, GdxGame.uiService.getSkin());
            this.text(WorldUIGroup.this.new WorldInformationLabel())
                    .button("Travel", 0)
                    .button("Return", 1);
        }

        @Override
        protected void result(Object object) {
            if(object.equals(0)){
                //Travel to the destination
                GdxGame.uiService.getCurtain().setBounds(0, 0, GdxGame.uiService.getStage().getWidth(), GdxGame.uiService.getStage().getHeight());
                GdxGame.uiService.getStage().addActor(GdxGame.uiService.getCurtain());
                GdxGame.uiService.getCurtain().addAction(Actions.fadeIn(1.0f));

                World.CURRENT = worldLoader.world;
                Thread thread = new Thread(worldLoader);
                thread.start();
                getStage().addActor(new Label("Please wait...", GdxGame.uiService.getSkin()));
            }
            //remove dialog
            remove();
        }
    }
    public class WorldMap extends Table {
        private static final int MINIMAP_WIDTH = 5, MINIMAP_HEIGHT = 3;
        private final Action currentSectorAction = Actions.forever(Actions.sequence(
                Actions.color(Color.BLACK, 0.5f, Interpolation.smooth)
                , Actions.color(Color.WHITE, 0.5f, Interpolation.smooth)));
        public final Table miniMap;

        private Actor current;

        public WorldMap(){
            World world = WorldUIGroup.this.worldLoader.world;

            TextureAtlas sectorAtlas = GdxGame.textureAtlasService.generateSectorAtlas(world);
            for(int yi=0; yi < world.getHeight(); yi++){
                for(int xi=0; xi < world.getWidth(); xi++){
                    Sector sector = world.sectorMap[yi][xi];
                    TextureRegion region = sectorAtlas.findRegion(sector.name);
                    Image image = new Image(region);
                    add(image);
                }
                row();
            }
            sectorAtlas.dispose();  //THIS MIGHT NOT BE CORRECT

            this.miniMap = new Table();
            for(int yi = 0; yi < MINIMAP_HEIGHT; yi ++) {
                for(int xi = 0; xi < MINIMAP_WIDTH; xi ++) {
                    miniMap.add().size(Sector.WIDTH, Sector.HEIGHT);
                }
                miniMap.row();
            }
        }

        @Override
        public void act(float delta) {
            //set the current sector
            int x = MathUtils.floor(Player.CURRENT.getCenterX() / Sector.pWIDTH);
            int y = MathUtils.floor(Player.CURRENT.getCenterY() / Sector.pHEIGHT);
            Actor newCurrent = getCells().get(y * x).getActor();

            if(newCurrent != current) {
                if(current != null) {
                    current.setColor(Color.WHITE);
                    current.clearActions();
                }

                newCurrent.addAction(currentSectorAction);
                current = newCurrent;

                //update the minimap
            }
        }
    }

}
