/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageProcessing;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
     
     public static boolean[] getMovingMatrix(){
         
     }
}
