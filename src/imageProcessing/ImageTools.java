package imageProcessing;

import static dto.Properties.IMAGE_HEIGHT;
import static dto.Properties.IMAGE_WIDTH;
import static gui.GUI.showExceptionError;
import imageRecording.LogSimulator;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import javafx.application.Platform;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import motorControl.MotorControlSimulator;


public class ImageTools {

    public static Image toJavaFXImage(ByteBuffer bytes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(
                    (RenderedImage) toBufferedImage(bytes), "png", out);
            out.flush();
        } catch (Exception e) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    showExceptionError(e, "BufferUnderflowException", "Image buffer underflow! This is mostly caused by selecting unsupported camera resolution. Please check again.");
                }
            });
            return null;
        }
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return new javafx.scene.image.Image(in);
    }

    public static BufferedImage toBufferedImage(ByteBuffer bytes) {
        BufferedImage img = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        byte[] array = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        bytes.get(array);
        return img;
    }

    public static BufferedImage toBufferedImage(ByteBuffer bytes, int type) {
        BufferedImage img = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, type);
        byte[] array = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        bytes.get(array);
        return img;
    }

    public static byte[] toGrayScale(byte[] img) {
        //By discarding 2 channels... Not averaging!
        byte[] gray = new byte[img.length / 3];
        for (int i = 0; i < gray.length; i++) {
            gray[i] = img[i * 3];
            if (gray[i] > 200) {
                gray[i] = 0;
            }
        }
        return gray;
    }

    public static class ImageEntry {

        public ByteBuffer img;
        public long timeStamp;
        public int x, y;
        public int moving;

        public ImageEntry(ByteBuffer image) {
            img = image;
            timeStamp = System.currentTimeMillis();
            int[] centroid = imageProcessor.getCentroid();
            x = centroid[0];
            y = centroid[1];
            moving = MotorControlSimulator.isMoving();
        }

        //Debug constructor
        public ImageEntry(ByteBuffer image, long time, int[] pos) {
            img = image;
            timeStamp = time;
            x = pos[0];
            y = pos[1];
            moving = 0;
        }
    }
}
