package com.shdwfghtr.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Action;

public class GoToScreenAction extends Action {
    private final Screen screen;
    private final Game game;

    public GoToScreenAction(Game game, Screen screen){
        this.game = game;
        this.screen = screen;
    }
    @Override
    public boolean act(float delta) {
        if(game.getScreen() != screen)
            game.setScreen(screen);
        return true;
    }
}
