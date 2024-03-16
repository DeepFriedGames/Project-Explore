package com.shdwfghtr.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.Sector;
import com.shdwfghtr.explore.World;

public class HUDTable extends Table {
    public static final int HEIGHT = Sector.HEIGHT * 3;

    public HUDTable(Player player, Table miniMap, Skin skin){
        super();

        add(new HealthTable(Player.CURRENT, skin)).expandY().width(80).left().bottom().pad(2);
        add(new AmmoTable(player, skin)).expand().left().bottom().pad(2);
        add(miniMap).left().pad(2);
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        setBounds(0, stage.getHeight() - HEIGHT, stage.getWidth(), HEIGHT);
    }
}
