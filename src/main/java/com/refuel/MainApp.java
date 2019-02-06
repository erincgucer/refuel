package com.refuel;

import com.refuel.view.RefuelController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

/**
 * Created by egucer on 01-Feb-19.
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private RefuelController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Refuel");
        this.primaryStage.getIcons().add(new Image("images/icon.png"));

        initClose();
        initRootLayout();
        initRefuel();
        setScene();
    }

    /**
     * Initializes the root layout.
     */
    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("view/RootLayout.fxml"));
            rootLayout = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the refuel fxml.
     */
    private void initRefuel() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("view/Refuel.fxml"));
            AnchorPane refuel = loader.load();
            rootLayout.setCenter(refuel);
            controller = loader.getController();
            controller.setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets and shows the scene.
     */
    private void setScene() {
        Scene scene = new Scene(rootLayout);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("view/css/barchart.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Initializes event handler for stopping additional threads on close.
     */
    private void initClose() {
        this.primaryStage.setOnCloseRequest(stopThreadsEventHandler);

        Button closeButton = new Button("Close Application");
        closeButton.setOnAction(event ->
                this.primaryStage.fireEvent(
                        new WindowEvent(
                                this.primaryStage,
                                WindowEvent.WINDOW_CLOSE_REQUEST
                        )
                )
        );
    }

    private EventHandler<WindowEvent> stopThreadsEventHandler = event -> controller.stopDataFileWatcher();

}
