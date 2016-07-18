package gui;

import static dto.Properties.DS_IMAGE_WIDTH;
import static dto.Properties.MOVE_DECISION_BOUNDARY_RATIO;
import static gui.GUI.showExceptionError;
import static gui.GUI.showWarning;
import imageAcquisition.ImageProducer;
import imageAqcuisition.imageInputSource.ImageInputSource;
import imageAqcuisition.imageInputSource.ImageSequence;
import imageProcessing.DownSampler;
import imageProcessing.ImageProcessor;
import imageProcessing.ImageTools;
import imageProcessing.ImageTools.ImageEntry;
import imageRecording.ImageRecorder;
import java.io.File;
import java.nio.ByteBuffer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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

public class Controller extends VBox {

    private int dragX, dragY;
    private ImageProducer imageProducer;
    private ImageProcessor imageProcessor;
    private InputViewFeed inputViewFeed;
    private ImageRecorder imageRecorder;
    private DownSampler downSampler;
    public Stage stage;
    private String inputLocation;
    private String outputLocation;
    private String imageLocation;
    private boolean tracking = false;
    private boolean recording = false;

    @FXML
    private ChoiceBox inputResSelector;
    @FXML
    private ChoiceBox outputResSelector;
    @FXML
    private ImageView imageView;
    @FXML
    private Button startResizeBtn;
    @FXML
    private Button startSimBtn;
    @FXML
    private Button endSimBtn;
    @FXML
    private Button inputBrowseBtn;
    @FXML
    private Button outputBrowseBtn;
    @FXML
    private Button imgBrowseBtn;
    @FXML
    private TextField inputLocBox;
    @FXML
    private TextField outputLocBox;
    @FXML
    private TextField imageSeqLocBox;
    @FXML
    private Accordion accordion;
    @FXML
    private TitledPane resizePane;

    public void updateImageView(ByteBuffer b) {
        Image img = ImageTools.toJavaFXImage(b); //Oh goodness...
        imageView.setImage(img);
    }

    @FXML
    protected void browseFileLocation(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        try {
            Button clickedBtn = (Button) event.getSource();
            TextField locationTF = null;
            String clickedBtnId = clickedBtn.getId();
            switch (clickedBtnId) {
                case "inputBrowseBtn":
                    locationTF = inputLocBox;
                    inputLocation = chooser.showDialog(stage).getAbsolutePath();
                    locationTF.setText(inputLocation);
                    break;
                case "outputBrowseBtn":
                    locationTF = outputLocBox;
                    outputLocation = chooser.showDialog(stage).getAbsolutePath();
                    locationTF.setText(outputLocation);
                    break;
                case "imgBrowseBtn":
                    locationTF = imageSeqLocBox;
                    imageLocation = chooser.showDialog(stage).getAbsolutePath();
                    locationTF.setText(imageLocation);
                    break;
                default:
                    break;
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    protected void startResize() {
        try {
            String inputResolution = (String) inputResSelector.getSelectionModel().getSelectedItem();
            switch (inputResolution) {
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
            String outputRresolution = (String) outputResSelector.getSelectionModel().getSelectedItem();
            switch (outputRresolution) {
                case "640*480":
                    dto.Properties.DS_IMAGE_HEIGHT = 480;
                    dto.Properties.DS_IMAGE_WIDTH = 640;
                    break;
                case "1280*720":
                    dto.Properties.DS_IMAGE_HEIGHT = 720;
                    dto.Properties.DS_IMAGE_WIDTH = 1280;
                    break;
                case "1280*960":
                    dto.Properties.DS_IMAGE_HEIGHT = 960;
                    dto.Properties.DS_IMAGE_WIDTH = 1280;
                    break;
            }

//            dto.Properties.SEGMENTATION_WINDOW_SIZE = (int) Math.ceil((double) (dto.Properties.SEGMENTATION_WINDOW_SIZE * dto.Properties.DS_IMAGE_HEIGHT * dto.Properties.DS_IMAGE_WIDTH) / (640 * 480));
//            dto.Properties.SEGMENTATION_COMPONENT_MIN_SIZE = (int) Math.ceil((double) (dto.Properties.SEGMENTATION_COMPONENT_MIN_SIZE * dto.Properties.DS_IMAGE_HEIGHT * dto.Properties.DS_IMAGE_WIDTH) / (640 * 480));
//            dto.Properties.MOTOR_PX_PER_STEP_X = (double) (dto.Properties.DS_IMAGE_HEIGHT * dto.Properties.MOTOR_PX_PER_STEP_X) / 640;
//            dto.Properties.MOTOR_PX_PER_STEP_Y = (double) (dto.Properties.DS_IMAGE_HEIGHT * dto.Properties.MOTOR_PX_PER_STEP_Y) / 480;
//            dto.Properties.MOVE_DECISION_CONFIDENCE_DISTANCE = (double) (dto.Properties.DS_IMAGE_HEIGHT * dto.Properties.DS_IMAGE_WIDTH * dto.Properties.MOVE_DECISION_CONFIDENCE_DISTANCE) / (640 * 480);
//            dto.Properties.MOVE_DECISION_BOUNDARY_PX = (int) (DS_IMAGE_WIDTH * MOVE_DECISION_BOUNDARY_RATIO);
            ImageInputSource imageSource = new ImageSequence(inputLocation);
            imageProducer = new ImageProducer(imageSource);
            imageProducer.start();
            downSampler = new DownSampler(imageProducer);
            downSampler.start();
            dto.Properties.run = true;
            if (inputLocation == null || outputLocation == null) {
                showWarning("No input/output location specified", "Please choose an input location and an output location first.");
                return;
            }
            imageRecorder = new ImageRecorder(imageProducer, outputLocation, false);
            imageRecorder.start();
        } catch (NullPointerException e) {
            showExceptionError(e, "NullPointerException", "Please select a resolution first!");
        }
    }

    @FXML
    protected void tracking() {
//        if (tracking) {
//            motorControl.stop();
//        } else {
//            if (imageProducer == null) {
//                showWarning("No devices connected", "Please connect a camera and motor control device before continuing.");
//                return;
//            }
//            if (imageProcessor == null) {
//                imageProcessor = new ImageProcessor(imageProducer);
//                motorControl.attach(imageProcessor);
//                imageProcessor.start();
//            }
//            motorControl.start();
//            inputViewFeed.attach(imageProcessor);
//        }
        tracking = !tracking;
    }

    @FXML
    protected void recording() {
        if (recording) {
            imageRecorder.stop();
        } else if (imageProducer == null) {
            showWarning("No devices connected", "Please connect a camera and motor control device before continuing.");
            return;
        } //            if (recordingLocation == null) {
        //                showWarning("No recording location specified", "Please set a save location for the recording file, under the 'Options' tab.");
        //                return;
        //            }
        //            imageRecorder = new ImageRecorder(imageProducer, recordingLocation);
        //            imageRecorder.start();
        //            recordingLocation = null;
        //            imageView.setImage(null);
        recording = !recording;
    }

    @FXML
    public void reset() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (imageRecorder != null) {
                    imageRecorder.stop();
                    imageRecorder = null;
                }
                if (imageProcessor != null) {
                    imageProcessor.stop();
                    imageProcessor = null;
                }
//                motorControl.stop();
                inputViewFeed.detach();
                dto.Properties.run = false;
            }
        });
    }

    public void initialize() {
        accordion.setExpandedPane(resizePane);
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
