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
    private static final String[] LR_IMAGE_STR = {"0000509CP_head100", "0000509CP_tail100", "0000539CP_head100", "0000539CP_tail100", "0000569CP_head100", "0000569CP_tail100", "0000509CP_head400", "0000509CP_tail400", "0000539CP_head400", "0000539CP_tail400", "0000569CP_head400", "0000569CP_tail400"};
    private static final String[] HR_IMAGE_STR = {"0000509CP_head400", "0000509CP_tail400", "0000539CP_head400", "0000539CP_tail400", "0000569CP_head400", "0000569CP_tail400", "0000509CP_head1600", "0000509CP_tail1600", "0000539CP_head1600", "0000539CP_tail1600", "0000569CP_head1600", "0000569CP_tail1600"};
    private static final String[] TYPE_STR = {"AIB_DS_nf2", "AIB_HR_nf2_L", "che2_DS_f8", "che2_HR_f8_L"};

    private ByteBuffer img = null;
    private int imgWidth = 0;
    private int imgHeight = 0;
    private static final String filePath = "F:\\testdata\\che2_HR_f8_L";

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
            String csvFile = filePath + "\\" + HR_IMAGE_STR[index] + ".csv";
            System.out.println(HR_IMAGE_STR[index]);
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
//        for (int i = 500; i < MAX_FILE_NUM; i++) {
//            try {
//                byte[] imgBytes;
//                imgBytes = ((DataBufferByte) (ImageIO.read(new File(filePath + "\\" + String.format("%07d", i) + "CP" + IMAGE_EXTENSION)).getRaster().getDataBuffer())).getData();
////            IntensityExportor iepr = new IntensityExportor(ByteBuffer.wrap(imgBytes), 1280, 960);
////            IntensityExportor iepr = new IntensityExportor(ByteBuffer.wrap(imgBytes), 640, 480);
//                IntensityExportor iepr = new IntensityExportor(ByteBuffer.wrap(imgBytes), 400, 400);
//                iepr.getIntensities(i);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        for (int i = 0; i < LR_IMAGE_STR.length; i++) {
            try {
                byte[] imgBytes;
                imgBytes = ((DataBufferByte) (ImageIO.read(new File(filePath + "\\" + HR_IMAGE_STR[i] + ".jpg")).getRaster().getDataBuffer())).getData();
                int size = (int) Math.sqrt(Double.parseDouble(HR_IMAGE_STR[i].substring(14)));
                IntensityExportor iepr = new IntensityExportor(ByteBuffer.wrap(imgBytes), size, size);
                iepr.getIntensities(i);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
