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

    private static final int MAX_FILE_NUM = 580;

    private ByteBuffer img = null;
    private int imgWidth = 0;
    private int imgHeight = 0;
    private static final String filePath = "E:\\testdata\\AIB_HR_nf2\\useful1";

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

    public void getIntensities(int index) {
        FileWriter fw = null;
        try {
            byte[] wrap;
            wrap = new byte[img.remaining()];
            img.get(wrap);
            img.rewind();
            int[] gray = ImageProcessor.getGray(wrap);
            ArrayList<String> vals = new ArrayList<>();
            String csvFile = filePath + "\\" + String.format("%07d", index) + ".csv";
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
        for (int i = 500; i < MAX_FILE_NUM; i++) {
            try {
                byte[] imgBytes;
                imgBytes = ((DataBufferByte) (ImageIO.read(new File(filePath + "\\" + String.format("%07d", i) + "CP" + IMAGE_EXTENSION)).getRaster().getDataBuffer())).getData();
//            IntensityExportor iepr = new IntensityExportor(ByteBuffer.wrap(imgBytes), 1280, 960);
//            IntensityExportor iepr = new IntensityExportor(ByteBuffer.wrap(imgBytes), 640, 480);
                IntensityExportor iepr = new IntensityExportor(ByteBuffer.wrap(imgBytes), 400, 400);
                iepr.getIntensities(i);;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
