package com.shdwfghtr.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.GdxGame;

public class AmmoTable extends Table {
    private final Player player;
    private static final Image missileImage = new Image(GdxGame.textureAtlasService.findUIRegion("hud_missile"));

    public AmmoTable(Player player, Skin skin) {
        super(skin);
        this.player = player;
        add(missileImage).left();
        add(new AmmoLabel(skin)).left().expandX();
    }

    private class AmmoLabel extends Label {
        private static final String prefix = ": ";

        public AmmoLabel(Skin skin) {
            super(prefix, skin);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            setText(prefix + player.missiles);
        }
    }
}
