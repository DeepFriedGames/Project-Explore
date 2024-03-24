package com.shdwfghtr.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.GdxGame;

public class HUDTable extends Table {
    private static final TextureRegionDrawable armor = new TextureRegionDrawable(GdxGame.textureAtlasService.findUIRegion("hud_armor"));
    private static final TextureRegionDrawable fullTank = new TextureRegionDrawable(GdxGame.textureAtlasService.findUIRegion("hud_O2_full"));
    private static final TextureRegionDrawable emptyTank = new TextureRegionDrawable(GdxGame.textureAtlasService.findUIRegion("hud_O2_empty"));
    private static final Image missileImage = new Image(GdxGame.textureAtlasService.findUIRegion("hud_missile"));

    private final Player player;

    public HUDTable(Player player, Group miniMap, Skin skin) {
        super(skin);
        this.player = player;

        Table healthTable = new Table(skin);
        healthTable.add(this.new TanksTable()).expand().bottom().left();
        healthTable.row();
        healthTable.add(this.new EnergyLabel()).left();
        healthTable.row();
        healthTable.add(this.new ArmorTable()).expandX().left().bottom();

        Table ammoTable = new Table();
        add (missileImage).left();
        add(this.new AmmoLabel()).left().expandX();

        add(healthTable).expandY().width(80).left().bottom().pad(2);
        add(ammoTable).expand().left().bottom().pad(2);
        add(miniMap).right().pad(2);
    }

    private class TanksTable extends Table {
        int nTanks = 0;

        @Override
        public void act(float delta) {
            int tanks = player.maxHealth / 100;
            while (nTanks < tanks) {
                add(new Image(fullTank));
                nTanks++;
            }
            Array<Cell> cells = getCells();
            for (int i = 0; i < cells.size; i++) {
                Image image = (Image) cells.get(i).getActor();
                if (image.getDrawable() == fullTank && player.health < (i + 1) * 100)
                    image.setDrawable(emptyTank);
                if (image.getDrawable() == emptyTank && player.health >= (i + 1) * 100)
                    image.setDrawable(fullTank);

            }
        }
    }

    private class EnergyLabel extends Label {
        private static final String prefix = "Energy: ";

        private EnergyLabel() {
            super(prefix, HUDTable.this.getSkin());
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            setText(prefix + (int) Math.floor(Player.CURRENT.health % 100));
        }
    }

    private class ArmorTable extends Table {
        int nArmor = 0;

        @Override
        public void act(float delta) {
            int number = Player.CURRENT.maxArmor / 2;
            while (nArmor < number) {
                add(new Image(armor));
                nArmor++;
            }
            Cell<Image>[] cells = getCells().toArray(Cell.class);
            for (int i = 0; i < cells.length; i++) {
                Image image = cells[i].getActor();
                if (image.getScaleX() != 1.0f && Player.CURRENT.armor >= (i + 1) * 2)
                    image.setScale(1.0f);
                if (image.getScaleX() != 0.75f && Player.CURRENT.armor - i * 2 == 1)
                    image.setScale(0.75f);
                if (image.getScaleX() != 0.0f && Player.CURRENT.armor < (i + 1) * 2)
                    image.setScale(0.0f);

            }
        }
    }

    private class AmmoLabel extends Label {
        private static final String prefix = ": ";

        public AmmoLabel() {
            super(prefix, HUDTable.this.getSkin());
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            setText(prefix + player.missiles);
        }
    }

}