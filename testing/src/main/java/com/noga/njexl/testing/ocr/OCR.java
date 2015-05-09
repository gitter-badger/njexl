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

    public static String text(String file) throws Exception {
        Image image = ImageIO.read(new File(file));
        return text(image);
    }

    public static String text(Image image) {
        PixelImage pixelImage = new PixelImage(image);
        pixelImage.toGrayScale(true);
        pixelImage.filter();
        String text = scanner.scan(image, 0, 0, 0, 0, null);
        return text;
    }

    public static String screen() throws Exception {
        // capture the whole screen
        BufferedImage capture = new Robot().createScreenCapture(
                new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        return text(capture);
    }

}
