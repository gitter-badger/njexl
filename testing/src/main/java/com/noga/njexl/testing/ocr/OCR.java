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

package com.noga.njexl.testing.ocr;

import net.sourceforge.javaocr.scanner.PixelImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by noga on 09/05/15.
 */
public final class OCR {

    public static final OCRScanner scanner = new OCRScanner();

    public static boolean image(String fileName){
        String name = fileName.toLowerCase();
        int li = name.lastIndexOf(".") + 1;
        if ( li <= 0 ) return false ;
        String ext = name.substring(li );
        switch (ext){
            case "png":
            case "jpg":
                return true;
            default:
                return false;
        }
    }

    public static CharacterRange characterRange(String fileName){
        String name = fileName.toLowerCase();
        if ( name.contains("digits")){
            return new CharacterRange('0','9');
        }
        return new CharacterRange('!','~');
    }

    /**
     * Given directory - trains the system
     * @param trainingImageDir the image directory
     */
    public static void train(String trainingImageDir)
    {

        try
        {
            scanner.clearTrainingImages();
            TrainingImageLoader loader = new TrainingImageLoader();
            HashMap<Character, ArrayList<TrainingImage>> trainingImageMap = new HashMap<>();

            File dir = new File(trainingImageDir);

            for ( File file : dir.listFiles() ){
                String fullName = file.getCanonicalPath();
                boolean img = image(fullName);
                if ( !img ){ continue; }
                CharacterRange cr = characterRange( fullName);
                loader.load( fullName, cr, trainingImageMap);

            }

            scanner.addTrainingImages(trainingImageMap);

        }
        catch (IOException ex)
        {
            System.err.println("OCR would go boom boom --> training is not complete!");
        }
    }

    /**
     * OCRs the text from the image file
     * @param file image file
     * @return text from the file
     * @throws Exception if error occurred while opening file
     */
    public static String text(String file) throws Exception {
        Image image = ImageIO.read(new File(file));
        return text(image);
    }

    /**
     * OCRs the text from image
     * @param image image object
     * @return text from the image
     */
    public static String text(Image image) {
        PixelImage pixelImage = new PixelImage(image);
        pixelImage.toGrayScale(true);
        pixelImage.filter();
        String text = scanner.scan(image, 0, 0, 0, 0, null);
        return text;
    }

    /**
     * OCRs current screen
     * @return text from the screen
     * @throws Exception
     */
    public static String screen() throws Exception {
        // capture the whole screen
        BufferedImage capture = new Robot().createScreenCapture(
                new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        return text(capture);
    }

}
