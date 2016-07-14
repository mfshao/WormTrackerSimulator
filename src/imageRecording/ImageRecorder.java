package imageRecording;

import imageAcquisition.ImageProducer;
import imageProcessing.ImageTools;
import imageProcessing.ImageTools.ImageEntry;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageRecorder implements Runnable {

    private final ImageProducer imageProducer;
    private final Thread thread;
    private final String outputDirectory;
    private boolean run = true;

    public ImageRecorder(ImageProducer imageProducer, String destination) {
        this.imageProducer = imageProducer;
        outputDirectory = destination;
        thread = new Thread(this, "Image Recorder");
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        run = false;
    }

    @Override
    public void run() {
        int frame = 0;
        try {
            try (DataOutputStream os = new DataOutputStream(new FileOutputStream(new File(outputDirectory + "/log.dat")))) {
                File file = new File(outputDirectory + "/log.txt");
                try (FileWriter fw = new FileWriter(file)) {
                    imageProducer.clear();
                    while (run) {
                        if (imageProducer.size() < 30) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                            }
                            continue;
                        }
                        ImageEntry entry = imageProducer.get();
                        BufferedImage img;
                        synchronized (entry) {
                            img = ImageTools.toBufferedImage(entry.img);
                        }
                        try {
                            ImageIO.write(img, "jpeg", new File(outputDirectory + "/" + String.format("%07d", frame) + ".jpeg"));
                            fw.write(String.format("%07d %d %d %d %d%n", frame, entry.timeStamp, entry.x, entry.y, entry.moving));
                            os.writeInt(frame);
                            os.writeLong(entry.timeStamp);
                            os.writeInt(entry.x);
                            os.writeInt(entry.y);
                            os.writeInt(entry.moving);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            frame++;
                        }
                        if (frame % 30 == 0) {
                            os.flush();
                        }
                    }
                    fw.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
