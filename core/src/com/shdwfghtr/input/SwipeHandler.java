package com.shdwfghtr.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.shdwfghtr.asset.ControllerService;
import com.shdwfghtr.asset.InventoryService;

import com.shdwfghtr.explore.GameState;

public class SwipeHandler extends InputHandler{
    private static final Vector2 VECTOR2 = new Vector2();
    private final Vector2[] touch = {new Vector2(), new Vector2()};
    private final GestureType[] type = {GestureType.NONE, GestureType.NONE};

    @Override
    public void act(float delta) {
        if(player != null && gameScreen.getState() == GameState.PLAY) {
            for (int pointer = 0; pointer < type.length; pointer++)
                switch (type[pointer]) {
                    case NONE:
                        continue;
                    case MOVEMENT:
                        VECTOR2.set(Gdx.input.getX(pointer), Gdx.input.getY(pointer));
                        //there are x and y gestures, to determine which this is we
                        //compare which has a larger change in value
                        if (Math.abs(VECTOR2.x - touch[pointer].x) > Math.abs(VECTOR2.y - touch[pointer].y))
                            if (VECTOR2.x < touch[pointer].x) {
                                getPlayer().left = true;
                                getPlayer().DOWN = false;
                                if (Math.abs(getPlayer().d.x) < getPlayer().speed)
                                    getPlayer().d.x = -getPlayer().speed;
                                else if (getPlayer().RUN) getPlayer().d.x -= 0.05f;
                            } else if (VECTOR2.x > touch[pointer].x) {
                                getPlayer().left = false;
                                getPlayer().DOWN = false;
                                if (Math.abs(getPlayer().d.x) < getPlayer().speed)
                                    getPlayer().d.x = getPlayer().speed;
                                else if (getPlayer().RUN) getPlayer().d.x += 0.05f;
                            } else if (VECTOR2.y < touch[pointer].y) {
                                super.inputDown(new InputEvent(), ControllerService.getInput("down"));
                            } else if (VECTOR2.y > touch[pointer].y) {
                                super.inputDown(new InputEvent(), ControllerService.getInput("up"));
                            }
                        break;
                    case ACTION:
                        VECTOR2.set(Gdx.input.getX(pointer), Gdx.input.getY(pointer));
                        //shoot gesture is not moving touch, allowing for some error
                        if (VECTOR2.dst2(touch[pointer]) < 1)
                            if (getPlayer().charge >= 3)
                                getPlayer().charge = 3;
                            else if ((getPlayer().MISSILE && InventoryService.isActive("charge_missile"))
                                    || (getPlayer().MORPH && InventoryService.isActive("charge_bomb"))
                                    || (!getPlayer().MORPH && !getPlayer().MISSILE && InventoryService.isActive("charge_shot")))
                                getPlayer().charge *= 1.01f;
                            else
                                super.inputDown(new InputEvent(), ControllerService.getInput("shoot"));

                            //there are x and y gestures, to determine which this is we
                            //compare which has a larger change in value
                        else if (Math.abs(VECTOR2.x - touch[pointer].x) > Math.abs(VECTOR2.y - touch[pointer].y))
                            //swapping weapons is swiping left/right
                            super.inputDown(new InputEvent(), ControllerService.getInput("switch"));

                            //jump gesture is swiping up
                        else if (VECTOR2.y > touch[pointer].y)
                            super.inputDown(new InputEvent(), ControllerService.getInput("jump"));
                        //running is swiping down
                        break;
                }
        }
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if(pointer > touch.length || pointer > type.length) return false;
        //store the touch position using pointer
        touch[pointer].set(x, y);
        //if it's on the left side of the screen, we prepare for movement
        if(x <= Gdx.graphics.getWidth()/2)
            type[pointer] = GestureType.MOVEMENT;
        //if it's on the right side of the screen, we prepare for action
        if(x > Gdx.graphics.getWidth()/2)
            type[pointer] = GestureType.ACTION;
        return true;
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if(pointer > touch.length || pointer > type.length) return;

        touch[pointer].setZero();
        type[pointer] = GestureType.NONE;
    }

    public enum GestureType {
        MOVEMENT, ACTION, NONE
    }
}
