package com.shdwfghtr.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.shdwfghtr.input.GamepadHandler;
import com.shdwfghtr.input.InputHandler;
import com.shdwfghtr.input.KeyboardHandler;
import com.shdwfghtr.input.TouchHandler;

public class ControllerService {
    public static final String[] INPUT_LIST = {"up", "down", "left", "right"
            , "jump", "shoot", "dash", "switch"
            , "start", "back", "x-axis", "y-axis"};
    private static final Preferences controls = Gdx.app.getPreferences("controls");

    public static void initializeController() {
        if(controls.contains("controller"))
            if((isGamepad() && Controllers.getControllers().size < 1)
                    || (isKeyboard() && !Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard))
                    || (isTouch() && !Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen))) {
                controls.remove("controller");
            }
        if(!controls.contains("controller")) resetControls();
        controls.flush();
    }

    public static int getInput(String input){
        return controls.getInteger(input);
    }

    public static float getTouchSize(){
        return controls.getFloat("touch_size", 64f);
    }
    public static float getTouchPadding(){
        return controls.getFloat("touch_padding", 1f);
    }
    public static float getTouchBorder(){
        return controls.getFloat("touch_border", 5f);
    }
    public static float getTouchPosition(){
        return controls.getFloat("touch_position", 5f);
    }
    public static float getTouchSensitivity() {
        return controls.getFloat("touch_sensitivity", 0.5f);
    }

    public static void setTouchSize(float value){
        controls.putFloat("touch_size", value);
        controls.flush();
    }
    public static void setTouchPadding(float value){
        controls.putFloat("touch_padding", value);
        controls.flush();
    }
    public static void setTouchBorder(float value){
        controls.putFloat("touch_border", value);
        controls.flush();
    }
    public static void setTouchPosition(float value){
        controls.putFloat("touch_position", value);
        controls.flush();
    }
    public static void setTouchSensitivity(float value){
        controls.putFloat("touch_sensitivity", value);
        controls.flush();
    }

    public static void resetControls() {
        System.out.println("resetting controls");
        if(Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen)) {
            controls.putString("controller", "touch");
            //The touchscreen controls are in an ordered list called buttons
            //each button gets it's name from the corresponding items in the INPUT_LIST
            //these names are then used to pass on commands
            controls.putInteger("up", 0);
            controls.putInteger("down", 1);
            controls.putInteger("left", 2);
            controls.putInteger("right", 3);
            controls.putInteger("jump", 4);
            controls.putInteger("shoot", 5);
            controls.putInteger("dash", 6);
            controls.putInteger("switch", 7);
            controls.putInteger("start", 8);
            controls.putInteger("back", 9);
        }
        if(Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard)) {
            controls.putString("controller", "keyboard");
            controls.putInteger("up", Input.Keys.UP);
            controls.putInteger("down", Input.Keys.DOWN);
            controls.putInteger("left", Input.Keys.LEFT);
            controls.putInteger("right", Input.Keys.RIGHT);
            controls.putInteger("jump", Input.Keys.Z);
            controls.putInteger("shoot", Input.Keys.X);
            controls.putInteger("dash", Input.Keys.CONTROL_LEFT);
            controls.putInteger("switch", Input.Keys.ALT_LEFT);
            controls.putInteger("start", Input.Keys.ENTER);
            controls.putInteger("back", Input.Keys.ESCAPE);
        }
        if(Controllers.getControllers().size > 0) {
            controls.putString("controller", "gamepad");
            GamepadHandler.GAMEPAD = Controllers.getControllers().first();
            ControllerMapping mapping = GamepadHandler.GAMEPAD.getMapping();
            controls.putInteger("up", mapping.buttonDpadUp);
            controls.putInteger("down", mapping.buttonDpadDown);
            controls.putInteger("left", mapping.buttonDpadLeft);
            controls.putInteger("right", mapping.buttonDpadRight);
            controls.putInteger("jump", mapping.buttonA);
            controls.putInteger("shoot", mapping.buttonB);
            controls.putInteger("dash", mapping.buttonX);
            controls.putInteger("switch", mapping.buttonY);
            controls.putInteger("start", mapping.buttonStart);
            controls.putInteger("back", mapping.buttonBack);
        }
        controls.putBoolean("use_axis", false);
        controls.flush();
    }

    public static InputHandler GetInputHandler() {
        if(isGamepad())
            return new GamepadHandler();
        else if(isTouch())
            return new TouchHandler();
        else if(isKeyboard())
            return new KeyboardHandler();

        return null;
    }

    public static boolean isKeyboard() {
        return controls.getString("controller").matches("keyboard");
    }

    public static boolean isTouch() {
        return controls.getString("controller").matches("touch");
    }

    public static boolean isGamepad() {
        return controls.getString("controller").matches("gamepad");
    }

    public static boolean isUsingAxis() {
        return controls.getBoolean("use_axis");
    }

    public static void setController(String value) {
        controls.putString("controller", value);
        controls.flush();
    }

    public static void setUsingAxis(boolean b) {
        controls.putBoolean("use_axis", b);
        controls.flush();
    }

    public static int getXAxis() {
        return controls.getInteger("x-axis");
    }
    public static int getYAxis() {
        return controls.getInteger("y-axis");
    }

    public static void setInput(String input, int key) {
        controls.putInteger(input, key);
        controls.flush();
    }

    public static String getCurrentController() {
        return controls.getString("controller").toUpperCase();
    }

    public static boolean hasInput(String input) {
        return controls.contains(input);
    }
}
