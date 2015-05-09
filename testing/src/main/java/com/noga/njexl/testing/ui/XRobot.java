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
 */
public class XRobot extends Robot {

    public static HashMap<String,Object> keyEvents ;

    static {
        keyEvents = new HashMap<>();
        Field[] fields = KeyEvent.class.getDeclaredFields();
        for ( int i = 0 ; i < fields.length ;i++ ){
            if (!java.lang.reflect.Modifier.isStatic(fields[i].getModifiers())) {
                continue;
            }
            String name = fields[i].getName() ;
            if ( name.startsWith("VK_")){
                name = name.substring(3);
            }
            try {
                Object v = fields[i].get(null);
                keyEvents.put(name, v);
            }catch (IllegalAccessException e){
                // eat it up
            }
        }
    }

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

    public void typeKey(String key){
        if ( keyEvents.containsKey(key)){
            Integer k = (Integer)keyEvents.get("key");
            keyPress(k);
            keyRelease(k);
        }
    }

    public void pressKey(String key){
        if ( keyEvents.containsKey(key)){
            Integer k = (Integer)keyEvents.get("key");
            keyPress(k);
        }
    }

    public void releaseKey(String key){
        if ( keyEvents.containsKey(key)){
            Integer k = (Integer)keyEvents.get("key");
            keyRelease(k);
        }
    }

}
