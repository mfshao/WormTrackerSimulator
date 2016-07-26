package imageAqcuisition.imageInputSource;

import static dto.Properties.IMAGE_EXTENSION;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

/**
 * An image source that gets images from a file directory.
 *
 * @author Kyle Moy
 *
 */
public class ImageSequence implements ImageInputSource {

    File[] files;
    int seek;
    String filePath;
    boolean fancy = false;
    boolean ready = true;
    long timeReady = System.currentTimeMillis();
    int framesPerSecond = 30;
    int totalFrame;

    /**
     * Default constructor. Appends expected file name to a file path
     *
     * @param path The directory in which the image files are.
     */
    public ImageSequence(String path) {
        filePath = path + "\\";
        seek = 0;
        totalFrame = this.getImageCount();
    }

    public int getImageCount() {
        int count = 0;
        File files = new File(filePath);
        for (File f : files.listFiles()) {
            if (f.isFile() && (f.getName().endsWith(IMAGE_EXTENSION))) {
                count++;
            }
        }
        return count;
    }
    
    public void updateSeek(){
        seek++;
    }

    @Override
    public ByteBuffer getImage() {
        try {
            //Need to decode file format, then get BGR values as byte[]
            byte[] imgBytes;
            if (fancy) {
                imgBytes = ((DataBufferByte) (ImageIO.read(files[seek]).getRaster().getDataBuffer())).getData();
            } else {
                System.out.println("add: "+ seek + IMAGE_EXTENSION);
                imgBytes = ((DataBufferByte) (ImageIO.read(new File(filePath + String.format("%07d", seek) + IMAGE_EXTENSION)).getRaster().getDataBuffer())).getData();
            }
            return ByteBuffer.wrap(imgBytes);
        } catch (IOException e) {
            ready = false;
            e.printStackTrace();
        }

        //If the image fails to read or cast
        return null;
    }

    @Override
    public boolean isReady() {
        if (System.currentTimeMillis() - timeReady >= (1000 / framesPerSecond) && seek < totalFrame) {
            timeReady = System.currentTimeMillis();
            return new File(filePath + String.format("%07d", seek) + IMAGE_EXTENSION).exists();
        }
        return false;
    }

}
