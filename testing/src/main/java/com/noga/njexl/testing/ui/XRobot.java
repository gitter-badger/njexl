/**
 * Copyright 2015 Nabarun Mondal
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.noga.njexl.testing.ui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by noga on 08/05/15.
 * Shamelessly copies basic idea from :
 * http://stackoverflow.com/questions/1248510/convert-string-to-keyevents
 */
public class XRobot extends Robot {


    public int mouseDelay = 100 ;

    public XRobot() throws AWTException {
    }

    public XRobot(GraphicsDevice screen) throws AWTException {
        super(screen);
    }

    public void screenShot(String fileName, String fileType) throws Exception {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        BufferedImage image = createScreenCapture(screenRectangle);
        ImageIO.write(image, fileType, new File(fileName));
    }

    public void screenShot(String fileName) throws Exception {
        screenShot(fileName, "png");
    }

    public void leftClick(int x, int y){
        mouseMove(x,y);
        mousePress(InputEvent.BUTTON1_MASK);
        delay(mouseDelay);
        mouseRelease(InputEvent.BUTTON1_MASK);
    }

    public void leftClick(){
        mousePress(InputEvent.BUTTON1_MASK);
        delay(mouseDelay);
        mouseRelease(InputEvent.BUTTON1_MASK);
    }

    public void rightClick(int x, int y){
        mouseMove(x,y);
        mousePress(InputEvent.BUTTON2_MASK);
        delay(mouseDelay);
        mouseRelease(InputEvent.BUTTON2_MASK);
    }

    public void rightClick(){
        mousePress(InputEvent.BUTTON2_MASK);
        delay(mouseDelay);
        mouseRelease(InputEvent.BUTTON2_MASK);
    }

    public void scroll(int amount){
        mousePress(InputEvent.BUTTON3_DOWN_MASK);
        mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        mouseWheel(amount);
    }

    public void type(char character) {
        switch (character) {
            case 'a': doType(KeyEvent.VK_A); break;
            case 'b': doType(KeyEvent.VK_B); break;
            case 'c': doType(KeyEvent.VK_C); break;
            case 'd': doType(KeyEvent.VK_D); break;
            case 'e': doType(KeyEvent.VK_E); break;
            case 'f': doType(KeyEvent.VK_F); break;
            case 'g': doType(KeyEvent.VK_G); break;
            case 'h': doType(KeyEvent.VK_H); break;
            case 'i': doType(KeyEvent.VK_I); break;
            case 'j': doType(KeyEvent.VK_J); break;
            case 'k': doType(KeyEvent.VK_K); break;
            case 'l': doType(KeyEvent.VK_L); break;
            case 'm': doType(KeyEvent.VK_M); break;
            case 'n': doType(KeyEvent.VK_N); break;
            case 'o': doType(KeyEvent.VK_O); break;
            case 'p': doType(KeyEvent.VK_P); break;
            case 'q': doType(KeyEvent.VK_Q); break;
            case 'r': doType(KeyEvent.VK_R); break;
            case 's': doType(KeyEvent.VK_S); break;
            case 't': doType(KeyEvent.VK_T); break;
            case 'u': doType(KeyEvent.VK_U); break;
            case 'v': doType(KeyEvent.VK_V); break;
            case 'w': doType(KeyEvent.VK_W); break;
            case 'x': doType(KeyEvent.VK_X); break;
            case 'y': doType(KeyEvent.VK_Y); break;
            case 'z': doType(KeyEvent.VK_Z); break;
            case 'A': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_A); break;
            case 'B': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_B); break;
            case 'C': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_C); break;
            case 'D': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_D); break;
            case 'E': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_E); break;
            case 'F': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_F); break;
            case 'G': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_G); break;
            case 'H': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_H); break;
            case 'I': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_I); break;
            case 'J': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_J); break;
            case 'K': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_K); break;
            case 'L': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_L); break;
            case 'M': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_M); break;
            case 'N': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_N); break;
            case 'O': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_O); break;
            case 'P': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_P); break;
            case 'Q': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Q); break;
            case 'R': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_R); break;
            case 'S': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_S); break;
            case 'T': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_T); break;
            case 'U': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_U); break;
            case 'V': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_V); break;
            case 'W': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_W); break;
            case 'X': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_X); break;
            case 'Y': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Y); break;
            case 'Z': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Z); break;
            case '`': doType(KeyEvent.VK_BACK_QUOTE); break;
            case '0': doType(KeyEvent.VK_0); break;
            case '1': doType(KeyEvent.VK_1); break;
            case '2': doType(KeyEvent.VK_2); break;
            case '3': doType(KeyEvent.VK_3); break;
            case '4': doType(KeyEvent.VK_4); break;
            case '5': doType(KeyEvent.VK_5); break;
            case '6': doType(KeyEvent.VK_6); break;
            case '7': doType(KeyEvent.VK_7); break;
            case '8': doType(KeyEvent.VK_8); break;
            case '9': doType(KeyEvent.VK_9); break;
            case '-': doType(KeyEvent.VK_MINUS); break;
            case '=': doType(KeyEvent.VK_EQUALS); break;
            case '~': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE); break;
            case '!': doType(KeyEvent.VK_EXCLAMATION_MARK); break;
            case '@': doType(KeyEvent.VK_AT); break;
            case '#': doType(KeyEvent.VK_NUMBER_SIGN); break;
            case '$': doType(KeyEvent.VK_DOLLAR); break;
            case '%': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_5); break;
            case '^': doType(KeyEvent.VK_CIRCUMFLEX); break;
            case '&': doType(KeyEvent.VK_AMPERSAND); break;
            case '*': doType(KeyEvent.VK_ASTERISK); break;
            case '(': doType(KeyEvent.VK_LEFT_PARENTHESIS); break;
            case ')': doType(KeyEvent.VK_RIGHT_PARENTHESIS); break;
            case '_': doType(KeyEvent.VK_UNDERSCORE); break;
            case '+': doType(KeyEvent.VK_PLUS); break;
            case '\t': doType(KeyEvent.VK_TAB); break;
            case '\n': doType(KeyEvent.VK_ENTER); break;
            case '[': doType(KeyEvent.VK_OPEN_BRACKET); break;
            case ']': doType(KeyEvent.VK_CLOSE_BRACKET); break;
            case '\\': doType(KeyEvent.VK_BACK_SLASH); break;
            case '{': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET); break;
            case '}': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET); break;
            case '|': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH); break;
            case ';': doType(KeyEvent.VK_SEMICOLON); break;
            case ':': doType(KeyEvent.VK_COLON); break;
            case '\'': doType(KeyEvent.VK_QUOTE); break;
            case '"': doType(KeyEvent.VK_QUOTEDBL); break;
            case ',': doType(KeyEvent.VK_COMMA); break;
            case '<': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA); break;
            case '.': doType(KeyEvent.VK_PERIOD); break;
            case '>': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD); break;
            case '/': doType(KeyEvent.VK_SLASH); break;
            case '?': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH); break;
            case ' ': doType(KeyEvent.VK_SPACE); break;
            default:
                throw new IllegalArgumentException("Cannot type character " + character);
        }
    }

    public void press(String s){
        switch (s){
            case "F1" : keyPress(KeyEvent.VK_F1); break;
            case "F2" : keyPress(KeyEvent.VK_F2); break;
            case "F3" : keyPress(KeyEvent.VK_F3); break;
            case "F4" : keyPress(KeyEvent.VK_F4); break;
            case "F5" : keyPress(KeyEvent.VK_F5); break;
            case "F6" : keyPress(KeyEvent.VK_F6); break;
            case "F7" : keyPress(KeyEvent.VK_F7); break;
            case "F8" : keyPress(KeyEvent.VK_F8); break;
            case "F9" : keyPress(KeyEvent.VK_F9); break;
            case "F10" : keyPress(KeyEvent.VK_F10); break;
            case "F11" : keyPress(KeyEvent.VK_F11); break;
            case "F12" : keyPress(KeyEvent.VK_F12); break;
            case "ALT" : keyPress(KeyEvent.VK_ALT); break;
            case "CTRL" : keyPress(KeyEvent.VK_CONTROL); break;
            case "FN" : keyPress(KeyEvent.VK_F); break;
            case "SHIFT" : keyPress(KeyEvent.VK_SHIFT); break;

            default:
                throw new IllegalArgumentException("Cannot press Symbol " + s);
        }
    }

    public void release(String s){
        switch (s){
            case "F1" : keyRelease(KeyEvent.VK_F1); break;
            case "F2" : keyRelease(KeyEvent.VK_F2); break;
            case "F3" : keyRelease(KeyEvent.VK_F3); break;
            case "F4" : keyRelease(KeyEvent.VK_F4); break;
            case "F5" : keyRelease(KeyEvent.VK_F5); break;
            case "F6" : keyRelease(KeyEvent.VK_F6); break;
            case "F7" : keyRelease(KeyEvent.VK_F7); break;
            case "F8" : keyRelease(KeyEvent.VK_F8); break;
            case "F9" : keyRelease(KeyEvent.VK_F9); break;
            case "F10" : keyRelease(KeyEvent.VK_F10); break;
            case "F11" : keyRelease(KeyEvent.VK_F11); break;
            case "F12" : keyRelease(KeyEvent.VK_F12); break;
            case "ALT" : keyRelease(KeyEvent.VK_ALT); break;
            case "CTRL" : keyRelease(KeyEvent.VK_CONTROL); break;
            case "FN" : keyRelease(KeyEvent.VK_F); break;
            case "SHIFT" : keyRelease(KeyEvent.VK_SHIFT); break;

            default:
                throw new IllegalArgumentException("Cannot release Symbol " + s);
        }
    }

    public void doType(int... keyCodes) {
        doType(keyCodes, 0, keyCodes.length);
    }

    private void doType(int[] keyCodes, int offset, int length) {
        if (length == 0) {
            return;
        }

        keyPress(keyCodes[offset]);
        doType(keyCodes, offset + 1, length - 1);
        keyRelease(keyCodes[offset]);
    }
    
    public void type(CharSequence characters) {
        int length = characters.length();
        for (int i = 0; i < length; i++) {
            char character = characters.charAt(i);
            type(character);
        }
    }

    public void typeControls( String...args){
        if ( args.length == 0 ){
            return;
        }
        if ( args.length == 1 ){
            type(args[0]);
        }
        if ( args.length == 2 ){
            int i = 0 ;
            String[] controls = args[0].split(",");
            for(String c : controls ){
                press(c);
            }
            type(args[1]);
            for(String c : controls ){
                release(c);
            }
        }
    }

}
