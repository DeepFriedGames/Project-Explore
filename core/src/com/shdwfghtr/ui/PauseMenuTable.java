package com.shdwfghtr.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.shdwfghtr.asset.ConversionService;
import com.shdwfghtr.asset.DataService;
import com.shdwfghtr.asset.InventoryService;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.GdxGame;

public class PauseMenuTable extends Window {
    private final TextureRegion playerTextureRegion = GdxGame.textureAtlasService.findEntityRegion("player_stand_front");

    public PauseMenuTable(String header, Player player) {
        super("Information", GdxGame.uiService.getSkin());

        setName("Pause Menu");
        setTouchable(Touchable.disabled);
        setFillParent(true);
        setResizable(false);
        setMovable(false);
        setVisible(false);
        setColor(1, 1, 1, 0f);

        Label.LabelStyle bodyStyle = new Label.LabelStyle(GdxGame.uiService.getBodyFont(), Color.WHITE);
        add(new Label("Player", bodyStyle)).pad(12).center();
        add(new Label(header, bodyStyle)).expandX().pad(12).center();
        row();
        add(this.new PlayerTable(player)).pad(6).top().expandY();
    }

    public PauseMenuTable(Player player, WorldUIGroup... worldUIGroups) {
        this("System " + ConversionService.toString(Math.abs(DataService.getSeed()))
            , player);
        add(this.new WorldsTable(worldUIGroups)).pad(6).top().expand();
    }

    public PauseMenuTable(Player player, WorldUIGroup.WorldMap worldMap) {
        this("World " + worldMap.getName(), player);
        add(worldMap).pad(6).top().expand();
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if(stage != null) {
            stage.setKeyboardFocus(this);
        }
    }

    public void toggle() {
        GdxGame.audioService.playSound("select");
        GdxGame.audioService.setVolume(1f);
        if (isVisible()) {
            this.addAction(Actions.sequence(
                    Actions.touchable(Touchable.disabled)
                    , Actions.fadeOut(0.6f)
                    , Actions.visible(false)
            ));
        } else {
            this.addAction(Actions.sequence(
                    Actions.touchable(Touchable.childrenOnly)
                    , Actions.visible(true)
                    , Actions.fadeIn(0.6f)
            ));
        }
    }

    private class InventoryGroup extends VerticalGroup {
        private InventoryGroup() {
            this.setName("Inventory");
            this.setTouchable(Touchable.disabled);

            for (Actor item : InventoryService.getInventoryActors()) {
                add(item);
            }
        }
    }

    private class PlayerTable extends Table {
        Label powerLabel, rangeLabel, speedLabel, jumpLabel;

        public PlayerTable(Player player) {
            super(PauseMenuTable.this.getSkin());

            Image playerImage = new Image(playerTextureRegion);
            powerLabel = new Label("Power:  k", getSkin());
            rangeLabel = new Label("Range:  m", getSkin());
            speedLabel = new Label("Speed:  m/s", getSkin());
            jumpLabel = new Label("Jump:  m", getSkin());

            add(playerImage).prefSize(48, 96).top().pad(12).row();
            add(new Label("Statistics", getSkin())).left().row();
            add(powerLabel).left().row();
            add(rangeLabel).left().row();
            add(speedLabel).left().row();
            add(jumpLabel).left().row();
            add(PauseMenuTable.this.new InventoryGroup()).expandY();
            if (player != null) setPlayer(player);
        }

        private void setPlayer(Player player) {
            float power = MathUtils.round(player.power * 100) / 100f;
            float range = Player.calculateBulletRange(player.bullet_life);
            float speed = Player.calculateSpeed(player.speed);
            float jumpHeight = Player.calculateJumpHeight(player.jump_speed);
            powerLabel.setText("Power: " + power + " k");
            rangeLabel.setText("Range: " + range + " m");
            speedLabel.setText("Speed: " + speed + " m/s");
            jumpLabel.setText("Vertical: " + jumpHeight + " m");
        }
    }

    private class WorldsTable extends Table {
        public WorldsTable(WorldUIGroup... worldUIGroups) {
            super(PauseMenuTable.this.getSkin());

            for (WorldUIGroup worldUIGroup : worldUIGroups) {
                Image worldImage = worldUIGroup.new WorldImage();
                worldImage.addListener(worldUIGroup.new WorldClickListener());

                Label worldLabel = worldUIGroup.new WorldInformationLabel();
                worldLabel.addListener(worldUIGroup.new WorldClickListener());

                add(worldImage).prefSize(32).pad(12);
                add(worldLabel).pad(12);
                row();
            }
        }
    }
}
