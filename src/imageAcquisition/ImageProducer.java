package imageAcquisition;

import static dto.Properties.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import imageAqcuisition.imageInputSource.ImageInputSource;
import imageProcessing.ImageTools.ImageEntry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A producer thread for getting images from an image source and adding them to
 * a buffer for consumption.
 *
 * @author Kyle Moy
 *
 */
public class ImageProducer implements Runnable {

    private final BlockingQueue<ImageEntry> buffer = new LinkedBlockingQueue<>();
    private final ImageInputSource input;
    private boolean run = true;
    private final Thread thread;

    /**
     * Default constructor.
     *
     * @param in The image source to pull from
     */
    public ImageProducer(ImageInputSource in) {
        input = in;
        thread = new Thread(this);
    }

    @Override
    public void run() {
        while (run) {
            if (input.isReady()&&buffer.size() < IMAGE_BUFFER_SIZE) {
                buffer.add(new ImageEntry(input.getImage()));
//                if (buffer.size() > IMAGE_BUFFER_SIZE) {
//                    poll(); //Too many in buffer... Throw frames away.
//                }
            }else{
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ImageProducer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void start() {
        thread.start();
    }

    /**
     * Stops taking images
     */
    public void stop() {
        run = false;
    }

    /**
     * @return Image at the top of the queue !!!DONT WAITS FOR NEW IMAGE IF
     * EMPTY!!!
     */
    public ImageEntry poll() {
        return buffer.poll();
    }

    public ImageEntry take() {
        try {
            return buffer.take();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * @return Image at the top of the queue or null if empty
     */
    public ImageEntry peek() {
        return buffer.peek();
    }

    /**
     * Clears the queue
     */
    public void clear() {
        buffer.clear();
    }

    /**
     * @return The number of images in queue
     */
    public int size() {
        return buffer.size();
    }
}
