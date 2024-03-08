package com.shdwfghtr.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.shdwfghtr.asset.ControllerService;
import com.shdwfghtr.asset.DataService;
import com.shdwfghtr.asset.InventoryService;
import com.shdwfghtr.entity.Entity;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.entity.Turret;
import com.shdwfghtr.explore.GameState;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.World;
import com.shdwfghtr.screens.GameScreen;
import com.shdwfghtr.ui.LeavePlanetDialog;

public class InputHandler extends InputListener {
    Player player;
    GameScreen gameScreen;

    public void act(float delta) {
        if(player == null) return;
        if(gameScreen.getState() == GameState.PLAY) {
            if(isInputDown("left")) {
                getPlayer().left = true;
                getPlayer().DOWN = false;
                if(Math.abs(getPlayer().d.x) < getPlayer().speed) getPlayer().d.x = -getPlayer().speed;
                else if(getPlayer().RUN) getPlayer().d.x -= 0.05f;

            } else if(isInputDown("right")) {
                getPlayer().left = false;
                getPlayer().DOWN = false;
                if(Math.abs(getPlayer().d.x) < getPlayer().speed) getPlayer().d.x = getPlayer().speed;
                else if(getPlayer().RUN) getPlayer().d.x += 0.05f;

            } if(isInputDown("shoot"))  {
                if(getPlayer().charge >= Player.MAX_CHARGE)
                    getPlayer().charge = Player.MAX_CHARGE;
                else if((getPlayer().MISSILE && !getPlayer().MORPH && InventoryService.isActive("charge_missile"))
                        || (getPlayer().MORPH && !getPlayer().MISSILE && InventoryService.isActive("charge_bomb"))
                        || (!getPlayer().MORPH && !getPlayer().MISSILE && InventoryService.isActive("charge_shot")))
                    getPlayer().charge *= Player.CHARGE_RATE;
            }
        }}

    boolean isInputDown(String input) {
        return false;
    }

    boolean inputUp(InputEvent event, int inputCode) {
        if(inputCode == ControllerService.getInput("start")) {
            if(gameScreen != null) {
                if (gameScreen.getState() == GameState.PLAY)
                    gameScreen.setState(GameState.PAUSE);
                else if (gameScreen.getState() == GameState.PAUSE)
                    gameScreen.setState(GameState.PLAY);
            }
            return true;
        } else if(inputCode == ControllerService.getInput("back")
                || inputCode == Input.Keys.BACK || inputCode == Input.Keys.ESCAPE) {
            if (gameScreen != null && gameScreen.getState() == GameState.PAUSE) {
                gameScreen.setState(GameState.PLAY);
                return true;
            } else {
                DataService.save(player);
                LeavePlanetDialog dialog = new LeavePlanetDialog(GdxGame.uiService);
                dialog.show(GdxGame.uiService.getStage());
                dialog.toFront();
                return true;
            }
        } else if(player != null && gameScreen.getState() == GameState.PLAY) {
            if(inputCode == ControllerService.getInput("left") || inputCode == ControllerService.getInput("right"))
                player.d.x = 0;
            else if (inputCode == ControllerService.getInput("up")) player.UP = false;
            else if(inputCode == ControllerService.getInput("dash"))	player.RUN = false;
            else if(inputCode == ControllerService.getInput("jump")) {
                if(player.d.y > 0)
                    player.d.y = 0;
            } else if(inputCode == ControllerService.getInput("shoot")) {
                if(player.charge > 1.2f)
                    player.fire();
                player.charge = 1;
            }
            return true;
        } else return super.keyUp(event, inputCode);
    }

    boolean inputDown(InputEvent event, int inputCode) {
        if(player == null) return false;
        if (gameScreen.getState() == GameState.PLAY) {
            if (inputCode == ControllerService.getInput("up")) {
                if (player.MORPH && !gameScreen.world.isBlocked(player.getX(), player.getTop() + 3)
                        && !gameScreen.world.isBlocked(player.getRight(), player.getTop() + 3)) {
                    player.MORPH = false;
                    player.DOWN = true;
                    player.charge = 1;
                } else {
                    player.DOWN = false;
                    player.UP = true;
                }
            }
            if (inputCode == ControllerService.getInput("down")) {
                if (player.DOWN && InventoryService.isActive("compression_orb")) {
                    player.MORPH = true;
                    player.charge = 1;
                } else {
                    player.DOWN = true;
                    player.SPIN = false;
                }

            }
            if (inputCode == ControllerService.getInput("dash")) {
                if (!player.MORPH && InventoryService.isActive("swift_boots"))
                    player.RUN = true;
            }
            if (inputCode == ControllerService.getInput("switch")) {
                if (player.missiles > 0) {
                    player.MISSILE = !player.MISSILE;
                    player.charge = 1;
                }
            }
            if (inputCode == ControllerService.getInput("jump")) {
                if (player.canJump() && !player.MORPH) {
                    GdxGame.audioService.playSound("jump");
                    if (player.d.x != 0) {
                        player.SPIN = true;
                        player.d.y = player.jump_speed * 0.94f;
                    } else player.d.y = player.jump_speed;
                    player.DOWN = false;
                } else if (player.MORPH && !gameScreen.world.isBlocked(player.getX(), player.getTop() + 3)
                        && !gameScreen.world.isBlocked(player.getRight(), player.getTop() + 3)) {
                    player.MORPH = false;
                    player.DOWN = true;
                    player.charge = 1;
                }
            }
            if (inputCode == ControllerService.getInput("shoot")) {
                if (player.SPIN) {
                    player.SPIN = false;
                        for (float y = player.getTop(); y <= player.getTop() + 13; y++)
                            if (gameScreen.world.isBlocked(player.getCenterX(), y))
                                player.setY(World.getTileY(y) - player.getHeight());
                }
                player.fire();
                player.charge = 1;

                //quick check to make turrets fire when the player does
                for (Entity entity : gameScreen.world.getActiveEntities()) {
                    if (entity instanceof Turret && gameScreen.camera.getBox().overlaps(entity.getBox()))
                        ((Turret) entity).fire();
                }
            }
            return true;
        } else
            return gameScreen.getState() == GameState.PAUSE || super.keyDown(event, inputCode);
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    Player getPlayer() {
        return player;
    }

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    public void toStage(Stage stage) {
        stage.addListener(this);
    }

}
