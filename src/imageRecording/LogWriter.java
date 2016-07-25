/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageRecording;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MSHAO1
 */
public class LogWriter {

    private DataOutputStream os = null;
    private FileWriter fw = null;
    private final String outputDirectory;

    public LogWriter(String destination) {
        outputDirectory = destination;
        try {
            os = new DataOutputStream(new FileOutputStream(new File(outputDirectory + "/log.dat")));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        try {
            fw = new FileWriter(new File(outputDirectory + "/log.txt"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        if (os != null) {
            try {
                os.close();
            } catch (IOException ex) {
            }
            os = null;
        }
        if (fw != null) {
            try {
                fw.close();
            } catch (IOException ex) {
            }
            fw = null;
        }
    }

    public void write(int frame, long timeStamp, int x, int y, int moving) {
        try {
            fw.write(String.format("%07d %d %d %d %d%n", frame, timeStamp, x, y, moving));
            os.writeInt(frame);
            os.writeLong(timeStamp);
            os.writeInt(x);
            os.writeInt(y);
            os.writeInt(moving);         
            if (frame % 30 == 0) {
                System.out.println("flush!");
                os.flush();
            }
            System.out.println("frame: " + frame);
        } catch (IOException ex) {
        }
    }
}
