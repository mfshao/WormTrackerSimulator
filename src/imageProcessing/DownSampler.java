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
import static dto.Properties.IMAGE_EXTENSION;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javafx.scene.control.Button;
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
    private final Button resizeBtn;
    private final int totalFrame;
    public boolean run = true;
    private int frame = 0;
    

    public DownSampler(File inputDirectory, File outputDirectory, TextArea textArea, Button btn) {
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        this.textArea = textArea;
        this.totalFrame = this.getImageCount(inputDirectory);
        resizeBtn = btn;
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
            if (f.isFile() && (f.getName().endsWith(IMAGE_EXTENSION))) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void run() {
        textArea.appendText(Integer.toString(totalFrame));
        textArea.appendText("\n");
        while (run) {
            if (frame >= totalFrame) {
                run = false;
            }
            try {
                BufferedImage img = ImageIO.read(new File(inputDirectory + "\\" + String.format("%07d", frame) + IMAGE_EXTENSION));
                Image toolkitImage = img.getScaledInstance(DS_IMAGE_WIDTH, DS_IMAGE_HEIGHT, Image.SCALE_AREA_AVERAGING);
                resizedImage = new BufferedImage(DS_IMAGE_WIDTH, DS_IMAGE_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
                Graphics g = resizedImage.getGraphics();
                g.drawImage(toolkitImage, 0, 0, null);
                g.dispose();
                try {
                    ImageIO.write(resizedImage, "jpeg", new File(outputDirectory + "\\" + String.format("%07d", frame++) + IMAGE_EXTENSION));
                } catch (IOException e) {
                } finally {
//                    textArea.appendText(outputDirectory + "\\" + String.format("%07d", frame++) + IMAGE_EXTENSION);
//                    textArea.appendText("\n");
                }
            } catch (IOException ex) {
            }
        }
        textArea.appendText("Done!");
        textArea.appendText("\n");
        resizeBtn.setDisable(false);
        this.stop();
    }
}
