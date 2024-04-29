import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

class Main {
    public static int[][] dimensions = { { 360, 640 }, { 360, 800 }, { 375, 667 }, { 414, 896 }, { 1080, 2400 },
            { 1179, 2556 }, { 1290, 2796 }, { 768, 1024 }, { 800, 1280 }, { 1200, 1920 }, { 1280, 800 }, { 1536, 2048 },
            { 1280, 720 }, { 1366, 768 }, { 1440, 900 }, { 1920, 1080 }, { 2560, 1440 } };

    public static void main(String[] args) throws IOException {
        int wallpaperWidth = 0;
        int wallpaperHeight = 0;
        System.out.println("Select screen dimensions");
        System.out.println(
                "(1) 360×640: Used by devices like the Samsung Galaxy J3 and J5\n(2) 360×800: Commonly found on the Huawei P30 and P40\n(3) 375×667: The size of the iPhone 6, 6s, 7, and 8\n(4) 414×896: Found on devices like the iPhone XR, XS Max, and iPhone 11 Pro Max\n(5) 1080×2400: A popular resolution for phones like the Samsung Galaxy S20 and OnePlus 8\n(6) 1179×2556: iPhone 14 and iPhone 14 Pro\n(7) 1290x2796: iPhone 14 Pro Max\n\n(8) 768×1024: Used by the iPad 2, iPad Mini, and iPad Air\n(9) 800×1280: Common screen size for Samsung Galaxy Tab A and Amazon Fire HD 8\n(10) 1200×1920: Found on tablets like the Samsung Galaxy Tab S2 and Google Nexus 7\n(11) 1280×800: Used by devices like the Amazon Fire HD 10 and Lenovo Tab 4\n(12) 1536×2048: The resolution of the iPad Pro 9.7 and 10.5\n\n(13) 1280×720: Used by smaller monitors and netbooks like the ASUS Eee PC\n(14) 1366×768: A popular size for laptops like the Dell Inspiron and Lenovo ThinkPad\n(15) 1440×900: Found on MacBook Air 13″ and some Dell UltraSharp monitors\n(16) 1920×1080: A standard size for various monitors, including the HP EliteDisplay and Acer Predator\n(17) 2560×1440: A high-resolution size commonly found on devices like the Apple Thunderbolt Display and ASUS ROG Swift monitor\n\n(18) Custom dimensions");
        Scanner screenSizeSel = new Scanner(System.in);
        int screenSizeNum = screenSizeSel.nextInt();
        if (screenSizeNum < 18 && screenSizeNum > 0) {
            wallpaperWidth = dimensions[screenSizeNum - 1][0];
            wallpaperHeight = dimensions[screenSizeNum - 1][1];
        } else if (screenSizeNum == 18) {
            System.out.println("Enter screen width");
            wallpaperWidth = screenSizeSel.nextInt();
            System.out.println("Enter screen height");
            wallpaperHeight = screenSizeSel.nextInt();
        } else {
            System.out.println("Invalid entry (must be an integer value from 1 to 18)");
        }
        screenSizeSel.close();
        BufferedImage wallpaperFinal = new BufferedImage(wallpaperWidth, wallpaperHeight, BufferedImage.TYPE_INT_ARGB);

        File[] images = new File("images").listFiles();
        BufferedImage[] squares = new BufferedImage[images.length];
        int trueLength = 0;
        for (int i = 0; i < images.length; i++) {
            try {
                BufferedImage workingSquare = ImageIO.read(images[i]);
                int workingWidth = workingSquare.getWidth();
                int workingHeight = workingSquare.getHeight();
                if (workingWidth > workingHeight) { // landscape
                    squares[i] = workingSquare.getSubimage((workingWidth / 2) - (workingHeight / 2), 0, workingHeight,
                            workingHeight);
                    trueLength++;
                } else if (workingHeight > workingWidth) { // portrait
                    squares[i] = workingSquare.getSubimage(0, (workingHeight / 2) - (workingWidth / 2), workingWidth,
                            workingWidth);
                    trueLength++;
                } else {
                    trueLength++;
                    continue; // just for my sanity - the picture would be square to meet this condition
                }
            } catch (OutOfMemoryError e) { // this error is stinky... find a solution to this sometime over the rainbow
                System.out.println("Too much memory!");
            }

        }

        // Find the grid configuration that matches the screen size the best
        // Find the width-height ratio of the screen, then find every 2 numbers that
        // will multiply to the number of pictures and see which proportion is closest
        // Scale the pictures then accordingly to fill all spaces, leave nothing out
        float resRatio = (float) wallpaperHeight / wallpaperWidth;
        float minDiff = resRatio;
        int[] minFactors = new int[2];

        for (int i = 1; i < images.length; i++) {
            if (images.length % i == 0) {
                float workingRatio = (float) i / (images.length / i);
                if (Math.abs(resRatio - workingRatio) < minDiff) {
                    minDiff = Math.abs(resRatio - workingRatio);
                    minFactors[0] = i;
                    minFactors[1] = images.length / i;
                }
            }
        }

        if (minDiff > 0.1) { // the maximum compatible ratio diff is too large to be reasonable for the
                             // wallpaper
            for (float i = 0; true; i += 0.00001) {
                float length = i;
                float width = (float) trueLength / i;

                // System.out.println(Math.abs((width / length) - resRatio));
                if (Math.abs((width / length) - resRatio) < 0.00001) {
                    minFactors[0] = (int) length;
                    minFactors[1] = (int) width;
                    break;
                }
            }
        }

        // let's get shuffled up
        int[] range = IntStream.rangeClosed(0, trueLength - 1).toArray();
        Random rnd = new Random();
        for (int i = range.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = range[index];
            range[index] = range[i];
            range[i] = a;
        }

        // spin, grow, draw
        Image[] squaresFinal = new Image[trueLength];
        int squareWidth = wallpaperWidth / minFactors[0];
        if (squareWidth * minFactors[0] < wallpaperWidth)
            minFactors[0]++;
        int squareHeight = wallpaperHeight / minFactors[1];
        if (squareHeight * minFactors[1] < wallpaperHeight)
            minFactors[1]++;

        // copy the array out - hopefully ease up on memory usage
        for (int i = 0; i < squaresFinal.length; i++) {
            Random rand = new Random();
            double scale = rand.nextDouble(0.5) + 1;
            System.out.print("Transform #" + (i + 1) + ": " + scale + ", ");
            squaresFinal[i] = squares[i].getScaledInstance((int) (squareHeight * scale), (int) (squareHeight * scale),
                    Image.SCALE_SMOOTH);
        }

        for (int i = 0; i < squaresFinal.length; i++) {
            Random rand = new Random();
            double rotate = rand.nextDouble(Math.PI / 3) - Math.PI / 6;
            System.out.println(rotate);
            try {
                AffineTransform move = new AffineTransform();
                move.translate((range[i] % 7) * squareHeight, (range[i] / 7) * squareHeight);
                move.rotate(rotate, squareHeight / 2, squareHeight / 2);
                wallpaperFinal.createGraphics().drawImage(squaresFinal[i], move, null);
            } catch (OutOfMemoryError e) {
                System.out.println("WAAAY too much memory!");
            }
        }
        ImageIO.write(wallpaperFinal, "png", new File("wallpaper-final.png"));
    }
}