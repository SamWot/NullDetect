package org.sam.home.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class AnalyzerUI extends Application {
    private static final String ANALYZER_FXML = "/ui/AnalyzerUI.fxml";

    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane root = FXMLLoader.load(getClass().getResource(ANALYZER_FXML));
        Scene primaryScene = new Scene(root);

        primaryStage.setTitle("Null Detect");
        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
