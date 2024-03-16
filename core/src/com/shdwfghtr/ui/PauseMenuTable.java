package com.shdwfghtr.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
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
    final float menuBorder =  15f;
    final float menuBreak = getWidth() /4f;

    public WorldUIGroup.WorldMap worldMap;

    public PauseMenuTable() {
        super("Information", GdxGame.uiService.getSkin());
        setName("Pause Menu");
        setTouchable(Touchable.disabled);
    }

    public PauseMenuTable(Player player, WorldUIGroup... worldUIGroups) {
        this();

        String header = "System " + ConversionService.toString(Math.abs(DataService.getSeed()));
        Label.LabelStyle bodyStyle = new Label.LabelStyle(GdxGame.uiService.getBodyFont(), Color.WHITE);

        Table playerTable = new Table();
        Image playerImage = this.new PlayerImage();
        playerTable.add(playerImage).prefSize(48, 96).top().pad(12);
        if (player != null) playerTable.add(this.new PlayerStatisticsActor(player));
        playerTable.add(this.new InventoryGroup()).expandY();

        Table worldsTable = new Table();
        for(WorldUIGroup worldUIGroup : worldUIGroups) {
            Image worldImage = worldUIGroup.new WorldImage();
            worldImage.addListener(worldUIGroup.new WorldClickListener());

            Label worldLabel = worldUIGroup.new WorldInformationLabel();
            worldLabel.addListener(worldUIGroup.new WorldClickListener());

            worldsTable.add(worldImage).prefSize(32).pad(12);
            worldsTable.add(worldLabel).pad(12);
            worldsTable.row();
        }

        add(new Label("Player", bodyStyle)).pad(12).center();
        add(new Label(header, bodyStyle)).expandX().pad(12).center();
        row();
        add(playerTable).pad(6).top().expandY();
        add(worldsTable).pad(6).top().expand();
    }

    public PauseMenuTable(Player player, WorldUIGroup worldUIGroup) {
        this();
        this.worldMap = worldUIGroup.new WorldMap();

        if(player != null) {
            add(this.new PlayerImage()).pad(12);
            add(this.new PlayerStatisticsActor(player)).pad(12);
            add(worldUIGroup.new WorldImage()).pad(12);
            add(worldUIGroup.new WorldInformationLabel()).pad(12);
            row();
            add(this.new InventoryGroup()).top().pad(12);
            add(worldMap).pad(6).expand();
        }
    }

   private class PlayerImage extends Image {
       PlayerImage(){
           super(playerTextureRegion);
           this.setSize(playerTextureRegion.getRegionWidth() * 3
                   , playerTextureRegion.getRegionHeight() * 3);
           this.setPosition(menuBreak/2 - this.getWidth()/2
                   , getHeight() - this.getHeight() - menuBorder);
           this.setTouchable(Touchable.disabled);
       }
   }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);

        if(stage != null)
            setBounds(0, 0,stage.getWidth(), stage.getHeight() - HUDTable.HEIGHT - 1);
    }

    private class PlayerStatisticsActor extends Actor {
        private final Player player;
       private PlayerStatisticsActor(Player player) {
           this.player = player;
           this.setTouchable(Touchable.disabled);
       }

       @Override
       public void draw(Batch batch, float parentAlpha) {
           GdxGame.uiService.getBodyFont().setColor(1, 1, 1, parentAlpha);
           GdxGame.uiService.getBodyFont().draw(batch, "Statistics", getX(), getY());
           float power = player.power;
           float range = Player.calculateBulletRange(player.bullet_life);
           float speed = Player.calculateSpeed(player.speed);
           float jumpHeight = Player.calculateJumpHeight(player.jump_speed);
           power *= 100; //round power to the nearest 100th
           power = Math.round(power);
           power /= 100;
           GdxGame.uiService.getBodyFont().draw(batch, "Power: " + power + " k", getX() + menuBorder, getY() - GdxGame.uiService.getBodyFont().getLineHeight());
           GdxGame.uiService.getBodyFont().draw(batch, "Range: " + range + " m", getX() + menuBorder, getY() - GdxGame.uiService.getBodyFont().getLineHeight() * 2);
           GdxGame.uiService.getBodyFont().draw(batch, "Speed: " + speed + " m/s", getX() + menuBorder, getY() - GdxGame.uiService.getBodyFont().getLineHeight() * 3);
           GdxGame.uiService.getBodyFont().draw(batch, "Jump:  " + jumpHeight + " m", getX() + menuBorder, getY() - GdxGame.uiService.getBodyFont().getLineHeight() * 4);
           GdxGame.uiService.getBodyFont().setColor(Color.WHITE);
       }
   }

   private class InventoryGroup extends VerticalGroup {
       private InventoryGroup() {
           this.setName("Inventory");
           this.setTouchable(Touchable.disabled);

           for(Actor item : InventoryService.getInventoryActors()){
               add(item);
           }
       }
   }
}
