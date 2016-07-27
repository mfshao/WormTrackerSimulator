/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageProcessing;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import gui.GUI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MSHAO1
 */
public class LogReader {

    private DataInputStream is = null;
    private final String inputDirectory;

    public LogReader(String source) {
        inputDirectory = source;
        try {
            is = new DataInputStream(new FileInputStream(new File(inputDirectory + "\\log.dat")));
//            GUI.getController().updateStatusBox(source);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        if (is != null) {
            try {
                is.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            is = null;
        }
    }

    public int[] getMovingMatrix() {
        int[] movingMatrix = new int[dto.Properties.imagecount];
        try {
            int i = 0;
            while ((is.available() > 0) && (i < dto.Properties.imagecount)) {
                is.readInt();//frame
                is.readLong();//timeStamp
                is.readInt();//x
                is.readInt();//y
                movingMatrix[i++] = is.readInt();
            }
            close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return movingMatrix;
    }
}
