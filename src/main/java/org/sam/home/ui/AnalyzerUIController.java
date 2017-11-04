package org.sam.home.ui;

import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.sam.home.analyzer.NullCompareInst;

import java.io.File;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.Executors;

public class AnalyzerUIController {
    @FXML private TextField dirField;

    @FXML private Button browseButton;

    @FXML private TextArea resultsTextArea;

    @FXML private ProgressBar progressBar;

    @FXML private Button startButton;

    @FXML private Button cancelButton;

    /**
     * Run directory chooser and set dirField
     */
    @FXML
    private void browseButtonClicked(ActionEvent event) {
        final DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Choose directory for analyzing");
        final File dir = dirChooser.showDialog(this.browseButton.getScene().getWindow());
        if (dir != null) {
            this.dirField.setText(dir.getAbsolutePath());
        }
    }

    /**
     * Run analysis of selected directory
     */
    @FXML
    private void startButtonClicked(ActionEvent event) {
        // Check if valid directory specified
        if (dirField.getText().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Specifiy directory for analysis", ButtonType.OK).show();
            return;
        }
        final Path dir;
        try {
            dir = Paths.get(dirField.getText());
        } catch (InvalidPathException ex) {
            new Alert(Alert.AlertType.ERROR, "Invalid directory specified", ButtonType.OK).show();
            return;
        }
        if (!Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
            new Alert(Alert.AlertType.ERROR, "Invalid directory specified", ButtonType.OK).show();
            return;
        }

        // Create and configure AnalyzerTask with specified directory
        final AnalyzerTask task = new AnalyzerTask(dir);
        task.analysisResultsProperty().get().addListener(
                (MapChangeListener.Change<? extends Path, ? extends List<NullCompareInst>> change) -> {
                    if (!change.wasAdded()) {
                        return;
                    }
                    this.resultsTextArea.appendText("In class " + change.getKey().toAbsolutePath().toString() + " found "
                            + change.getValueAdded().size() + " redundant null checks:\n");
                    for (final NullCompareInst inst : change.getValueAdded()) {
                        this.resultsTextArea.appendText(inst.lineInfo() + "\n");
                    }
                    this.resultsTextArea.appendText("\n");
                }
        );
        this.progressBar.progressProperty().bind(task.progressProperty());

        // Execute task
        Executors.newSingleThreadExecutor().submit(task);
    }
}
