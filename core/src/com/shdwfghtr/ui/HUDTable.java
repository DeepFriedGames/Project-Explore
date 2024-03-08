package com.shdwfghtr.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.Sector;
import com.shdwfghtr.explore.World;

public class HUDTable extends Table {
    public static final int HEIGHT = Sector.HEIGHT * 3;

    public HUDTable(Player player, World world, Skin skin){
        super();
        WorldMap worldMap = new WorldMap(world);

        pad(2.0f);
        add(new HealthTable(Player.CURRENT, skin))
                .expandY().width(80).left().bottom();
        add(new AmmoTable(player, skin)).expand().left().bottom();
        add(worldMap.new WorldMiniMap()).width(Sector.WIDTH * 5).height(Sector.HEIGHT * 3).left();

    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        setBounds(0, stage.getHeight() - HEIGHT, stage.getWidth(), HEIGHT);
    }
}
