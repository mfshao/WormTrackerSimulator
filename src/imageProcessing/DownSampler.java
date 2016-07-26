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
import gui.GUI;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
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
    public boolean run = true;
    private int frame = 0;

    public DownSampler(File inputDirectory, File outputDirectory) {
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
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

    @Override
    public void run() {
//        textArea.appendText(Integer.toString(totalFrame));
//        textArea.appendText("\n");
        GUI.getController().updateStatusBox(Integer.toString(dto.Properties.imagecount));
        while (run) {
            if (frame >= dto.Properties.imagecount) {
                run = false;
            }
            try {
                BufferedImage img = ImageIO.read(new File(inputDirectory + "\\" + String.format("%07d", frame) + IMAGE_EXTENSION));
                resizedImage = new BufferedImage(DS_IMAGE_WIDTH, DS_IMAGE_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
                Graphics2D g = resizedImage.createGraphics();
                try {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g.drawImage(img, 0, 0, DS_IMAGE_WIDTH, DS_IMAGE_HEIGHT, null);
                } finally {
                    g.dispose();
                }
                try {
                    ImageIO.write(resizedImage, "jpeg", new File(outputDirectory + "\\" + String.format("%07d", frame++) + IMAGE_EXTENSION));
                } catch (IOException e) {
                } finally {
                    GUI.getController().updateStatusBox(outputDirectory + "\\" + String.format("%07d", frame) + IMAGE_EXTENSION);
//                    textArea.appendText(outputDirectory + "\\" + String.format("%07d", frame++) + IMAGE_EXTENSION);
//                    textArea.appendText("\n");
                }
            } catch (IOException ex) {
            }
        }
//        textArea.appendText("Done!");
//        textArea.appendText("\n");
        GUI.getController().updateStatusBox("Done!");
        dto.Properties.resizerun = false;
        this.stop();
    }
}
