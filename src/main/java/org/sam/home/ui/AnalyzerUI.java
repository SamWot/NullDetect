package org.sam.home.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class AnalyzerUI extends Application {
    private static final String ANALYZER_FXML = "/ui/AnalyzerUI.fxml";

    private AnalyzerUIController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ANALYZER_FXML));
        this.controller = loader.<AnalyzerUIController>getController();

        Pane root = loader.load();
        Scene primaryScene = new Scene(root);

        primaryStage.setTitle("Null Detect");
        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        this.controller.cancelTask();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
