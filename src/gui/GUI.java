package gui;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class GUI extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    private static Controller controller;

    private static volatile boolean isShowingAlert = false;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("NemJava Tracker");
        buildGUI();
        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }

    public static Controller getController() {
        return controller;
    }

    private void buildGUI() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(GUI.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();

            loader = new FXMLLoader();
            loader.setLocation(GUI.class.getResource("view/InputViewer.fxml"));
            AnchorPane inputViewer = (AnchorPane) loader.load();
            rootLayout.setCenter(inputViewer);
            controller = loader.getController();
            controller.stage = primaryStage;
            controller.refreshVideoInputDevices();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showWarning(String headerText, String contectText) {
        if (!isShowingAlert) {
            isShowingAlert = true;
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(headerText);
            alert.setContentText(contectText);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    isShowingAlert = false;
                }
            });
        }
    }

    public static void showExceptionError(Exception ex, String headerText, String contectText) {
        if (!isShowingAlert) {
            isShowingAlert = true;
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error: Exception");
            alert.setHeaderText(headerText);
            alert.setContentText(contectText);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    isShowingAlert = false;
                }
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
