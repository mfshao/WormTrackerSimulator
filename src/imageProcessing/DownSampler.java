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
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextArea;
import javax.imageio.ImageIO;

/**
 *
 * @author Travis Shao
 */
public final class DownSampler implements Runnable {

    private BufferedImage resizedImage;
    private final File inputDirectory;
    private final File outputDirectory;
    private final Thread thread;
    private final TextArea textArea;
    private final int totalFrame;
    public boolean run = true;
    private int frame = 0;

    public DownSampler(File inputDirectory, File outputDirectory, TextArea textArea) {
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        this.textArea = textArea;
        this.totalFrame = this.getImageCount(inputDirectory);
        thread = new Thread(this);
    }

    public void start() {
        thread.start();
        run = true;
    }

    public void stop() {
        frame = 0;
        run = false;
    }

    public int getImageCount(File inputDirectory) {
        int count = 0;
        for (File f : inputDirectory.listFiles()) {
            if (f.isFile() && (f.getName().endsWith(".jpeg"))) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void run() {
        while (frame <= totalFrame) {
            try {
                BufferedImage img = ImageIO.read(new File(inputDirectory + "\\" + String.format("%07d", frame++) + ".jpeg"));
                Image toolkitImage = img.getScaledInstance(DS_IMAGE_WIDTH, DS_IMAGE_HEIGHT, Image.SCALE_AREA_AVERAGING);
                resizedImage = new BufferedImage(DS_IMAGE_WIDTH, DS_IMAGE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
                Graphics g = resizedImage.getGraphics();
                g.drawImage(toolkitImage, 0, 0, null);
                g.dispose();
                try {
                    ImageIO.write(resizedImage, "jpeg", new File(outputDirectory + "\\" + String.format("%07d", frame) + ".jpeg"));
                } catch (IOException e) {
                } finally {
                    textArea.appendText(outputDirectory + "\\" + String.format("%07d", frame) + ".jpeg");
                    textArea.appendText("\n");
                }
            } catch (IOException ex) {
            }
        }
        textArea.appendText("Done!");
        textArea.appendText("\n");
        this.stop();
    }
}
