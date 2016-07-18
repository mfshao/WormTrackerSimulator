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
import static dto.Properties.SEGMENTATION_DELAY;
import imageAcquisition.ImageProducer;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.nio.ByteBuffer;

/**
 *
 * @author Travis Shao
 */
public class DownSampler implements Runnable {

    private final ImageProducer input;
    private final long referenceTime = 0;
    private BufferedImage resizedImage;
    private final Thread thread;
    public boolean run = true;
    private int i = 0;

    public DownSampler(ImageProducer in) {
        input = in;
        thread = new Thread(this);
    }

    public void start() {
        thread.start();
        run = true;
    }

    public void stop() {
        i = 0;
        run = false;
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
                    BufferedImage referenceImage;

                    synchronized (entry) {
                        ByteBuffer img = entry.img;
                        wrap = new byte[img.remaining()];
                        img.get(wrap);
                        img.rewind();

                        referenceImage = ImageTools.toBufferedImage(img);
                    }
                    Image toolkitImage = referenceImage.getScaledInstance(DS_IMAGE_WIDTH, DS_IMAGE_HEIGHT, Image.SCALE_AREA_AVERAGING);
                    resizedImage = new BufferedImage(DS_IMAGE_WIDTH, DS_IMAGE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
                    Graphics g = resizedImage.getGraphics();
                    g.drawImage(toolkitImage, 0, 0, null);
                    g.dispose();
                    System.out.println(i);
                    i++;
                } else {
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
