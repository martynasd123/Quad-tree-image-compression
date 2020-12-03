package main.util;

import main.main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    /**
     * Loads an image from a file, and stores it in a color array
     *
     * @param file The source file to an image
     * @return 2D array of colors that represents an image
     */
    public static Color[][] LoadImageAsArray(File file) throws IOException {

        BufferedImage buffer = ImageIO.read(file);

        int height = buffer.getHeight();
        int width = buffer.getWidth();

        Color[][] pixels = new Color[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                pixels[i][j] = new Color(buffer.getRGB(j, i), true);
            }
        }
        return pixels;
    }

}
