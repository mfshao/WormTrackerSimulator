/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import imageProcessing.ImageProcessor;
import imageProcessing.ImageTools.ImageEntry;
import java.nio.ByteBuffer;

/**
 *
 * @author MSHAO1
 */
public class IntensityExportor {

    ImageEntry entry;

    public IntensityExportor(ImageEntry ie) {
        entry = ie;
    }

    public void getIntensities() {
        byte[] wrap;
        ByteBuffer img = entry.img;
        wrap = new byte[img.remaining()];
        img.get(wrap);
        img.rewind();
        int[] gray = ImageProcessor.getGray(wrap);
        
    }
}
