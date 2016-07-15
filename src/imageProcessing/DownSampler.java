/*
 * Copyright (C) 2016 Travis Shao
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package imageProcessing;

import static dto.Properties.DS_IMAGE_HEIGHT;
import static dto.Properties.DS_IMAGE_WIDTH;
import static dto.Properties.IMAGE_HEIGHT;
import static dto.Properties.IMAGE_WIDTH;
import static dto.Properties.SEGMENTATION_DELAY;
import imageAcquisition.ImageProducer;
import java.nio.ByteBuffer;

/**
 *
 * @author Travis Shao
 */
public class DownSampler implements Runnable {

    private final ImageProducer input;
    private final long referenceTime = 0;
    private int[][] resizedImage;
    private final Thread thread;
    public boolean run = true;

    public DownSampler(ImageProducer in) {
        input = in;
        thread = new Thread(this);
    }

    public void start() {
        thread.start();
        run = true;
    }

    public void stop() {
        run = false;
    }

    private static int[][] getImage(byte[] src) {

        int[] gray = new int[src.length / 3];
        for (int i = 0; i < src.length; i += 3) {
            int r = src[i];
            int g = src[i + 1];
            int b = src[i + 2];
            gray[i / 3] = (int) ((r + g + b) / 3);
        }

        int[][] image = new int[IMAGE_WIDTH][IMAGE_HEIGHT];
        int k = 0;
        for (int j = 0; j < IMAGE_HEIGHT; j++) {
            for (int i = 0; i < IMAGE_WIDTH; i++) {
                image[i][j] = src[k];
                k++;
            }
        }
        return image;
    }

    public int[][] resizePixels(int[][] pixels, int w1, int h1, int w2, int h2) {
        int[][] temp = new int[w2][h2];
        double x_ratio = w1 / (double) w2;
        double y_ratio = h1 / (double) h2;
        double px, py;
        for (int i = 0; i < h2; i++) {
            for (int j = 0; j < w2; j++) {
                px = Math.floor(j * x_ratio);
                py = Math.floor(i * y_ratio);
                temp[j][i] = pixels[(int) px][(int) py];
            }
        }
        return temp;
    }

    @Override
    public void run() {
        while (run) {
            try {
                if (System.currentTimeMillis() - referenceTime > SEGMENTATION_DELAY) {
                    ImageTools.ImageEntry entry = input.peek();
                    if (entry == null) {
                        // Thread just started, no images to peek! Wait a bit.
                        Thread.sleep(500);
                        continue;
                    }
                    byte[] wrap;
                    synchronized (entry) {
                        ByteBuffer img = entry.img;
                        wrap = new byte[img.remaining()];
                        img.get(wrap);
                        img.rewind();
                    }
                    int[][] referenceImage = getImage(wrap);
                    resizedImage = resizePixels(referenceImage, IMAGE_WIDTH, IMAGE_HEIGHT, DS_IMAGE_WIDTH, DS_IMAGE_HEIGHT);
                } else {
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
