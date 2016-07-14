package imageAqcuisition.imageInputSource;

import static dto.Properties.*;

import java.awt.Dimension;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Scanner;

import com.github.sarxos.webcam.Webcam;

/**
 * An image source that gets images from a USB Camera
 *
 * @author Kyle Moy
 *
 */
public class SerialCamera implements ImageInputSource {

    private static Webcam camera;

    public SerialCamera() throws CameraConnectException {
        // Get a list of all probable cameras from the list of serial devices
        List<Webcam> webcamList = Webcam.getWebcams();

        // Let user choose if multiple devices exist
        if (webcamList.isEmpty()) {
            throw new CameraConnectException("ERROR: No cameras detected.");
        } else if (webcamList.size() > 1) {
            System.out.println("WARNING: Multiple cameras detected. Select one of the following [int]:");
            int camCount = 0;
            for (Webcam cam : webcamList) {
                System.out.println("[" + camCount + "]: " + cam);
                camCount++;
            }
            System.out.print("> ");

            Scanner in = new Scanner(System.in);
            Integer selectedCam = in.nextInt();
            camera = webcamList.get(selectedCam);
        } else {
            camera = Webcam.getDefault(); // grabs default, first cam from list
        }
        startCamera();
    }

    public static String[] getCameras() {
        List<Webcam> webcamList = Webcam.getWebcams();
        String[] cams = new String[webcamList.size()];
        for (int i = 0; i < webcamList.size(); i++) {
            cams[i] = webcamList.get(i).getName();
        }
        return cams;
    }

    @Override
    public ByteBuffer getImage() {
        return camera.getImageBytes();
    }

    @Override
    public boolean isReady() {
        return camera.isImageNew();
    }

    public void close() {
        camera.close();
    }

    private void startCamera() {
        // Hardcoded Resolution
        Dimension dimension = new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT);
        Dimension[] myResolution = new Dimension[]{dimension};
        camera.setCustomViewSizes(myResolution);
        camera.setViewSize(myResolution[0]);
        camera.open(false);

        //System.out.println("RESOLUTION: " + camera.getViewSize());
        // Sleep to prevent initial green/black image
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
