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

    ByteBuffer img;

    public IntensityExportor(ByteBuffer source) {
        img = source;
    }

    public void getIntensities() {
        byte[] wrap;
        wrap = new byte[img.remaining()];
        img.get(wrap);
        img.rewind();
        int[] gray = ImageProcessor.getGray(wrap);
        
    }
    
    public static void main(String[] args){
        
    }
}
