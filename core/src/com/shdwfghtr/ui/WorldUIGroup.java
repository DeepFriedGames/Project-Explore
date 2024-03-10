package com.shdwfghtr.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.shdwfghtr.asset.PaletteService;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.explore.GdxGame;
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
        String worldType = World.getType(worldLoader.world.index);
        TextureRegion textureRegion = GdxGame.textureAtlasService.findEnvironmentRegion(worldType);
        PaletteService.recolorTextureRegion(textureRegion
                , PaletteService.getPalette("environment"), worldLoader.world.palette);

        this.worldImage = this.new WorldImage(textureRegion);
        this.dialog = this.new WorldClickDialog();

        this.worldImage.addListener(this.new WorldClickListener());
    }

    public class WorldImage extends Image {
        private WorldImage(TextureRegion textureRegion) {
            super(textureRegion);

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
}
