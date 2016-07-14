package gui;

import static dto.Properties.IMAGE_HEIGHT;
import static dto.Properties.IMAGE_WIDTH;
import static dto.Properties.MOVE_DECISION_BOUNDARY_RATIO;
import static gui.GUI.showExceptionError;
import static gui.GUI.showWarning;
import imageAcquisition.ImageProducer;
import imageAqcuisition.imageInputSource.CameraConnectException;
import imageAqcuisition.imageInputSource.SerialCamera;
import imageProcessing.ImageProcessor;
import imageProcessing.ImageTools;
import imageProcessing.ImageTools.ImageEntry;
import imageRecording.ImageRecorder;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import jssc.SerialPortList;
import motorControl.MotorControl;

public class Controller extends VBox {

    private int dragX, dragY;
    private ImageProducer imageProducer;
    private ImageProcessor imageProcessor;
    private MotorControl motorControl;
    private InputViewFeed inputViewFeed;
    private ImageRecorder imageRecorder;
    private SerialCamera serialCamera;
    public Stage stage;
    //private File recordingLocation = null;
    private String recordingLocation;
    private boolean tracking = false;
    private boolean recording = false;

    @FXML
    private ChoiceBox videoInputDeviceList;
    @FXML
    private ChoiceBox motorControlDeviceList;
    @FXML
    private ChoiceBox resolutionChoiceBox;
    @FXML
    private ImageView imageView;
    @FXML
    private Button trackingButton;
    @FXML
    private Button recordingButton;
    @FXML
    private TextField fileLocation;
    @FXML
    private Accordion accordion;
    @FXML
    private TitledPane devicePane;
    @FXML
    private Button connectDevicesButton;

    public void updateImageView(ByteBuffer b) {
        Image img = ImageTools.toJavaFXImage(b); //Oh goodness...
        imageView.setImage(img);
    }

    @FXML
    protected void browseFileLocation() {
        /*
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Recording save location");
		fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MRF", "*.mrf"),
                new FileChooser.ExtensionFilter("All Images", "*.*")
            );
		recordingLocation = fileChooser.showSaveDialog(stage);
		fileLocation.setText(recordingLocation.getAbsolutePath());
         */
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Recording Location");
        chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        try {
            recordingLocation = chooser.showDialog(stage).getAbsolutePath();
            fileLocation.setText(recordingLocation);
        } catch (NullPointerException ex) {

        }
    }

    @FXML
    protected void connectDevices() {
        try {
            //Set the resolution of the input data
            String resolution = (String) resolutionChoiceBox.getSelectionModel().getSelectedItem();
            switch (resolution) {
                case "640*480":
                    dto.Properties.IMAGE_HEIGHT = 480;
                    dto.Properties.IMAGE_WIDTH = 640;
                    break;
                case "1280*720":
                    dto.Properties.IMAGE_HEIGHT = 720;
                    dto.Properties.IMAGE_WIDTH = 1280;
                    break;
                case "1280*960":
                    dto.Properties.IMAGE_HEIGHT = 960;
                    dto.Properties.IMAGE_WIDTH = 1280;
                    break;
            }

            dto.Properties.SEGMENTATION_WINDOW_SIZE = (int) Math.ceil((double) (dto.Properties.SEGMENTATION_WINDOW_SIZE * dto.Properties.IMAGE_HEIGHT * dto.Properties.IMAGE_WIDTH) / (640 * 480));
            dto.Properties.SEGMENTATION_COMPONENT_MIN_SIZE = (int) Math.ceil((double) (dto.Properties.SEGMENTATION_COMPONENT_MIN_SIZE * dto.Properties.IMAGE_HEIGHT * dto.Properties.IMAGE_WIDTH) / (640 * 480));
            dto.Properties.MOTOR_PX_PER_STEP_X = (double) (dto.Properties.IMAGE_WIDTH * dto.Properties.MOTOR_PX_PER_STEP_X) / 640;
            dto.Properties.MOTOR_PX_PER_STEP_Y = (double) (dto.Properties.IMAGE_HEIGHT * dto.Properties.MOTOR_PX_PER_STEP_Y) / 480;
            dto.Properties.MOVE_DECISION_CONFIDENCE_DISTANCE = (double) (dto.Properties.IMAGE_HEIGHT * dto.Properties.IMAGE_WIDTH * dto.Properties.MOVE_DECISION_CONFIDENCE_DISTANCE) / (640 * 480);
            dto.Properties.MOVE_DECISION_BOUNDARY_PX = (int) (IMAGE_WIDTH * MOVE_DECISION_BOUNDARY_RATIO);
            serialCamera = new SerialCamera();//new ImageSequence("//medixsrv/Nematodes/data/N2_nf7/input/");//
            imageProducer = new ImageProducer(serialCamera);
            motorControl = new MotorControl((String) motorControlDeviceList.getSelectionModel().getSelectedItem());
            imageProducer.start();
            inputViewFeed = new InputViewFeed(imageProducer, this);
            inputViewFeed.start();
            connectDevicesButton.setDisable(true);
            dto.Properties.run = true;
        } catch (CameraConnectException e) {
            showExceptionError(e, "CameraConnectException", "Cannot connect to camera!");
        } catch (NullPointerException e) {
            showExceptionError(e, "NullPointerException", "Please select a resolution first!");
        }
    }

    @FXML
    protected void tracking() {
        if (tracking) {
            motorControl.stop();
            trackingButton.setText("Start Tracking");
        } else {
            if (imageProducer == null) {
                showWarning("No devices connected", "Please connect a camera and motor control device before continuing.");
                return;
            }
            if (imageProcessor == null) {
                imageProcessor = new ImageProcessor(imageProducer);
                motorControl.attach(imageProcessor);
                imageProcessor.start();
            }
            motorControl.start();
            inputViewFeed.attach(imageProcessor);
            trackingButton.setText("Stop Tracking");
        }
        tracking = !tracking;
    }

    @FXML
    protected void recording() {
        if (recording) {
            imageRecorder.stop();
            recordingButton.setText("Start Recording");
        } else {
            if (imageProducer == null) {
                showWarning("No devices connected", "Please connect a camera and motor control device before continuing.");
                return;
            }
            if (recordingLocation == null) {
                showWarning("No recording location specified", "Please set a save location for the recording file, under the 'Options' tab.");
                return;
            }
            imageRecorder = new ImageRecorder(imageProducer, recordingLocation);
            imageRecorder.start();
            fileLocation.setText("");
            recordingLocation = null;
//            imageView.setImage(null);
            recordingButton.setText("Stop Recording");
        }
        recording = !recording;
    }

    @FXML
    protected void dragPressed(MouseEvent event) {
        dragX = (int) event.getX();
        dragY = (int) event.getY();
    }

    @FXML
    protected void dragReleased(MouseEvent event) {
        if (motorControl == null) {
            return;
        }
        int deltaX = (int) event.getX() - dragX;
        int deltaY = (int) event.getY() - dragY;
        //System.out.println(deltaX + "\t" + deltaY);

        //Oh goodness...
        (new Thread() {
            @Override
            public void run() {
                motorControl.move(deltaX, deltaY);
            }
        }).start();
    }

    @FXML
    protected void up() {
        if (motorControl == null) {
            return;
        }
        (new Thread() {
            @Override
            public void run() {
                motorControl.move(0, -IMAGE_HEIGHT / 2);
            }
        }).start();
    }

    @FXML
    protected void down() {
        if (motorControl == null) {
            return;
        }
        (new Thread() {
            @Override
            public void run() {
                motorControl.move(0, IMAGE_HEIGHT / 2);
            }
        }).start();
    }

    @FXML
    protected void left() {
        if (motorControl == null) {
            return;
        }
        (new Thread() {
            @Override
            public void run() {
                motorControl.move(-IMAGE_WIDTH / 2, 0);
            }
        }).start();
    }

    @FXML
    protected void right() {
        if (motorControl == null) {
            return;
        }
        (new Thread() {
            @Override
            public void run() {
                motorControl.move(IMAGE_WIDTH / 2, 0);
            }
        }).start();
    }

    @FXML
    protected void refreshVideoInputDevices() {
        Platform.runLater(() -> {
            String[] cams = SerialCamera.getCameras();
            if (cams.length == 0) {
                videoInputDeviceList.getItems().add("No Devices Detected");
            }
            videoInputDeviceList.getItems().addAll(Arrays.asList(cams));

            String[] ports = SerialPortList.getPortNames();
            if (ports.length == 0) {
                motorControlDeviceList.getItems().add("No Devices Detected");
            }
//		        Image image = new Image("0000023.jpg");
//                        Image image = new Image("0000023a.jpg");
//                        imageView.setImage(image);
            motorControlDeviceList.getItems().addAll(Arrays.asList(ports));
            motorControlDeviceList.getSelectionModel().selectFirst();
            videoInputDeviceList.getSelectionModel().selectFirst();
            accordion.setExpandedPane(devicePane);
        });
    }

    @FXML
    public void reset() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (imageRecorder != null) {
                    imageRecorder.stop();
                    imageRecorder = null;
                    recordingButton.setText("Start Recording");
                }
                if (imageProcessor != null) {
                    imageProcessor.stop();
                    imageProcessor = null;
                }
                motorControl.stop();
                motorControl.detach();
                motorControl.closePort();
                inputViewFeed.detach();
                serialCamera.close();
                dto.Properties.run = false;
                trackingButton.setText("Start Tracking");
                connectDevicesButton.setDisable(false);
            }
        });
    }

    public static class InputViewFeed implements Runnable {

        ImageProducer src;
        ImageProcessor prc;
        Controller dest;
        Thread thread;

        public InputViewFeed(ImageProducer source, Controller destination) {
            src = source;
            dest = destination;
            thread = new Thread(this, "InputViewFeed");
        }

        public void attach(ImageProcessor processor) {
            prc = processor;
        }

        public void detach() {
            prc = null;
        }

        public void start() {
            thread.start();
        }

        @Override
        public void run() {
            while (dto.Properties.run) {
                ImageEntry entry = src.peek();
                if (entry == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                ByteBuffer clone = null;
                synchronized (entry) {
                    ByteBuffer img = entry.img;
                    try {
                        clone = ByteBuffer.allocate(img.capacity());
                        img.rewind();
                        clone.put(img);
                        img.rewind();
                    } catch (NullPointerException e) {
                    }
                }
                if (clone != null) {
                    clone.flip();
                    if (prc != null) {
                        dest.updateImageView(prc.overlayImage(clone));
                    } else {
                        dest.updateImageView(clone);
                    }
                }
            }
        }
    }

}
