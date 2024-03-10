package com.shdwfghtr.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.shdwfghtr.asset.ConversionService;
import com.shdwfghtr.asset.DataService;
import com.shdwfghtr.asset.InventoryService;
import com.shdwfghtr.asset.PaletteService;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.GdxGame;

public class PauseMenuTable extends Table {
    private final TextureRegion playerTextureRegion = GdxGame.textureAtlasService.findEntityRegion("player_stand_front");
    private final TextureRegion backgroundTextureRegion = GdxGame.textureAtlasService.findUIRegion("pauseMenu");
    final float menuBorder =  15f;
    final float menuBreak = getWidth() /4f;
    public final Array<Actor> items = new Array<>();

    public PauseMenuTable(Player player) {
        setBackground(new TextureRegionDrawable(backgroundTextureRegion));
        setName("Pause Menu");

        PlayerImage playerImage = this.new PlayerImage();
        addActor(playerImage);

        //also adds the player's stats to the pause menu
        if(player != null) {
            Actor stats = this.new PlayerStatisticsActor(player);
            stats.setPosition(menuBorder, playerImage.getY() - menuBorder);
            addActor(stats);

            Actor inventoryActor = new InventoryActor();
            inventoryActor.setPosition(menuBorder, stats.getY() - GdxGame.uiService.getBodyFont().getLineHeight() * 5);
            addActor(inventoryActor);
        }


        items.addAll(InventoryService.getInventoryActors());
        for(Actor item : items){
            item.setSize(getWidth() / 4, GdxGame.uiService.getBodyFont().getLineHeight());
            item.setTouchable(Touchable.enabled);
        }
    }

    public PauseMenuTable(Player player, WorldUIGroup... worldUIGroups){
        this(player);
        //setBounds(BUTTON_SIZE, BUTTON_SIZE, GdxGame.uiService.getStage().getWidth() - 2*BUTTON_SIZE, GdxGame.uiService.getStage().getHeight() - 2*BUTTON_SIZE);
        setLayoutEnabled(false);
        setTouchable(Touchable.disabled);
        addActor(this.new SystemInfoGroup(worldUIGroups));
    }
    public PauseMenuTable(Player player, WorldUIGroup worldUIGroup) {
        this(player);
        addActor(worldUIGroup.new WorldInformationLabel());
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

   private class InventoryActor extends Actor {
       private InventoryActor() {
           this.setName("Inventory Label");
           this.setTouchable(Touchable.disabled);
       }

       @Override
       public void draw(Batch batch, float parentAlpha) {
           GdxGame.uiService.getBodyFont().setColor(1, 1, 1, parentAlpha);
           GdxGame.uiService.getBodyFont().draw(batch, "Inventory", getX(), getY());
           GdxGame.uiService.getBodyFont().setColor(Color.WHITE);
       }
   }

   private class SystemInfoGroup extends Group {
       private SystemInfoGroup(WorldUIGroup... worldUIGroups) {
            GlyphLayout glyphLayout = new GlyphLayout();
           String header = "System " + ConversionService.toString(Math.abs(DataService.getSeed()));
            Label menuHeader = new Label(header, GdxGame.uiService.getSkin(), "title", PaletteService.getPalette("ui")[1]);
            glyphLayout.setText(GdxGame.uiService.getBodyFont(), header);
            menuHeader.setSize(glyphLayout.width, glyphLayout.height);
            menuHeader.setPosition(menuBreak + menuBorder*2, getHeight() - menuHeader.getHeight() - menuBorder);
            menuHeader.setTouchable(Touchable.disabled);
            addActor(menuHeader);

            for(int i = 0; i< worldUIGroups.length; i++) {
                Label label = worldUIGroups[i].new WorldInformationLabel();
                float x = menuHeader.getX(),
                        y = menuHeader.getY() - label.getHeight() * (i + 1);
                if(y <= 0) {
                    x += menuBreak + 2 * menuBorder;
                    y += label.getHeight() * 5;
                }
                label.addListener(worldUIGroups[i].new WorldClickListener());
                label.setPosition(x, y);
                addActor(label);
            }
        }
   }
}
