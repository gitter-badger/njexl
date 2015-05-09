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

    /**
     * Given directory - trains the system
     * @param trainingImageDir the image directory
     */
    public static void train(String trainingImageDir)
    {

        if (!trainingImageDir.endsWith(File.separator))
        {
            trainingImageDir += File.separator;
        }
        try
        {
            scanner.clearTrainingImages();
            TrainingImageLoader loader = new TrainingImageLoader();
            HashMap<Character, ArrayList<TrainingImage>> trainingImageMap = new HashMap<Character, ArrayList<TrainingImage>>();

            loader.load(
                    trainingImageDir + "ascii.png",
                    new CharacterRange('!', '~'),
                    trainingImageMap);

            loader.load(
                    trainingImageDir + "hpljPica.jpg",
                    new CharacterRange('!', '~'),
                    trainingImageMap);


            loader.load(
                    trainingImageDir + "sublime.png",
                    new CharacterRange('!', '~'),
                    trainingImageMap);

            loader.load(
                    trainingImageDir + "digits.jpg",
                    new CharacterRange('0', '9'),
                    trainingImageMap);

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
