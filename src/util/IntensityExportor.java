/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import static dto.Properties.IMAGE_EXTENSION;
import imageProcessing.ImageProcessor;
import imageProcessing.ImageTools.ImageEntry;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author MSHAO1
 */
public class IntensityExportor {

    private ByteBuffer img = null;
    private int imgWidth = 0;
    private int imgHeight = 0;

    public IntensityExportor(ByteBuffer source, int w, int h) {
        if (source == null) {
            throw new IllegalArgumentException("Image source can not be null!");
        } else {
            img = source;
        }
        if (w <= 0) {
            throw new IllegalArgumentException("Image width must be positive!");
        } else {
            imgWidth = w;
        }
        if (h <= 0) {
            throw new IllegalArgumentException("Image height must be positive!");
        } else {
            imgHeight = h;
        }
    }

    public void getIntensities() {
        FileWriter fw = null;
        try {
            byte[] wrap;
            wrap = new byte[img.remaining()];
            img.get(wrap);
            img.rewind();
            int[] gray = ImageProcessor.getGray(wrap);
            ArrayList<String> vals = new ArrayList<>();
            String csvFile = "D:\\Untitled.csv";
            fw = new FileWriter(csvFile);
            for (int i = 0; i < imgHeight; i++) {
                vals.clear();
                for (int j = 0; j < imgWidth; j++) {
                    int val = gray[j + (i * imgWidth)];
                    if (val < 0) {
                        val += 255;
                    }
                    vals.add(Integer.toString(val));
                }
                CSVUtil.writeLine(fw, vals);
                fw.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(IntensityExportor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(IntensityExportor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        String filePath = "D:\\Untitled.jpeg";
        try {
            byte[] imgBytes;
            imgBytes = ((DataBufferByte) (ImageIO.read(new File(filePath)).getRaster().getDataBuffer())).getData();
//            IntensityExportor iepr = new IntensityExportor(ByteBuffer.wrap(imgBytes), 1280, 960);
//            IntensityExportor iepr = new IntensityExportor(ByteBuffer.wrap(imgBytes), 640, 480);
            IntensityExportor iepr = new IntensityExportor(ByteBuffer.wrap(imgBytes), 34, 64);
            iepr.getIntensities();;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
