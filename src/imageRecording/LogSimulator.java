package imageRecording;

import static dto.Properties.IMAGE_EXTENSION;
import static dto.Properties.LOG_FAILURE_THRESHOLD;
import imageAcquisition.ImageProducer;
import imageProcessing.ImageTools.ImageEntry;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

public class LogSimulator implements Runnable {

    private final ImageProducer imageProducer;
    private final Thread thread;
    private final String outputDirectory;
    private boolean run = true;
    private long lastLog;

    public LogSimulator(ImageProducer imageProducer, String destination) {
        this.imageProducer = imageProducer;
        outputDirectory = destination;
        thread = new Thread(this, "Log Simulator");
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        run = false;
    }

    public int getImageCount() {
        int count = 0;
        File files = new File(outputDirectory);
        for (File f : files.listFiles()) {
            if (f.isFile() && (f.getName().endsWith(IMAGE_EXTENSION))) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void run() {
        int totalFrame = this.getImageCount();
        int frame = 0;
        lastLog = System.currentTimeMillis();
        try {
            try (DataOutputStream os = new DataOutputStream(new FileOutputStream(new File(outputDirectory + "/log.dat")))) {
                File file = new File(outputDirectory + "/log.txt");
                try (FileWriter fw = new FileWriter(file)) {
                    imageProducer.clear();
                    while (run) {
                        if (System.currentTimeMillis() - lastLog > LOG_FAILURE_THRESHOLD) {
                            run = false;
                            System.out.println("break!");
                            break;
                        }
                        if (frame < totalFrame - 10) {
                            if ((imageProducer.size() < 10)) {
                                try {
                                  System.out.println("continuing");
                                   System.out.println(imageProducer.size());
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                continue;
                            }
                        } else {
                            try {
//                                System.out.println(imageProducer.size());
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        ImageEntry entry = imageProducer.poll();
                        if (entry != null) {
                            synchronized (entry) {
                                ByteBuffer img = entry.img;
                            }
                            lastLog = System.currentTimeMillis();
                            try {
                                fw.write(String.format("%07d %d %d %d %d%n", frame, entry.timeStamp, entry.x, entry.y, entry.moving));
                                os.writeInt(frame);
                                os.writeLong(entry.timeStamp);
                                os.writeInt(entry.x);
                                os.writeInt(entry.y);
                                os.writeInt(entry.moving);
                                System.out.println("frame: " + frame);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                frame++;
                            }
                        } else {
                            System.out.println("null!");
                        }
                        if (frame % 30 == 0 || frame < 30) {
                            System.out.println("flush!");
                            os.flush();
                        }
                    }
                    System.out.println("Done!");
                    fw.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
