package org.sam.home.ui;

import javafx.collections.MapChangeListener;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.sam.home.analyzer.NullCompareInst;

import java.io.File;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

public class AnalyzerUIController {
    @FXML
    private TextField dirField;

    @FXML
    private Button browseButton;

    @FXML
    private TextArea resultsTextArea;

    @FXML
    private Label stausText;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button startButton;

    @FXML
    private Button cancelButton;

    private Optional<AnalyzerTask> currentTask;

    public AnalyzerUIController() {
        this.currentTask = Optional.empty();
    }

    /**
     * Make all UI changes due to finish or cancelation of current task
     */
    private void finishTaskUI() {
        this.startButton.setDisable(false);
        this.cancelButton.setDisable(true);
        this.progressBar.progressProperty().unbind();
        this.progressBar.setProgress(0.0);
    }

    /**
     * Make all UI changes due to start of new task
     */
    private void startTaskUI() {
        this.resultsTextArea.clear();
        this.startButton.setDisable(true);
        this.cancelButton.setDisable(false);
        this.progressBar.setProgress(0.0);
        this.currentTask.ifPresent(task -> {
            this.progressBar.progressProperty().bind(task.progressProperty());
        });

    }

    private void setStatusText(WorkerStateEvent event) {
        final String status;
        if (event.getEventType().equals(WorkerStateEvent.WORKER_STATE_SUCCEEDED)) {
            status = "Analyze finished ";
        } else if (event.getEventType().equals(WorkerStateEvent.WORKER_STATE_FAILED)) {
            status = "Analyze failed ";
        } else if (event.getEventType().equals(WorkerStateEvent.WORKER_STATE_CANCELLED)) {
            status = "Analyze canceled ";
        } else if (event.getEventType().equals(WorkerStateEvent.WORKER_STATE_RUNNING)) {
            status = "Analyzing ";
        } else {
            throw new IllegalArgumentException("Invalid WorkerStateEvent");
        }

        this.currentTask.ifPresent(
                task -> this.stausText.setText(status + task.getAnalysisDir().toAbsolutePath()));
    }

    /**
     * Cancel current task
     */
    private void cancelTask() {
        this.currentTask.ifPresent(
                task -> {
                    if (task.getState() != Task.State.SUCCEEDED
                            && task.getState() != Task.State.FAILED
                            && task.getState() != Task.State.CANCELLED) {
                        task.cancel();
                    }
                }
        );
        this.finishTaskUI();
        this.currentTask = Optional.empty();
    }

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
            this.cancelTask();
        }
    }

    /**
     * Event handler for when task is finished due to it's success, failure or cancelation
     */
    private void currentTaskFinished(WorkerStateEvent event) {
        if (!event.getEventType().equals(WorkerStateEvent.WORKER_STATE_SUCCEEDED)
                && !event.getEventType().equals(WorkerStateEvent.WORKER_STATE_FAILED)
                && !event.getEventType().equals(WorkerStateEvent.WORKER_STATE_CANCELLED)) {
            throw new IllegalArgumentException("Wrong worker state event. Only finish states allowed.");
        }

        this.setStatusText(event);
        this.currentTask = Optional.empty();

        this.finishTaskUI();
    }

    /**
     * Run analysis of selected directory
     */
    @FXML
    private void startButtonClicked(ActionEvent event) {
        // Check if valid directory specified
        if (dirField.getText().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Specify directory for analysis", ButtonType.OK).show();
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
        this.currentTask = Optional.of(new AnalyzerTask(dir));
        this.currentTask.get().analysisResultsProperty().get().addListener(
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

        startTaskUI();

        this.currentTask.get().setOnSucceeded(this::currentTaskFinished);
        this.currentTask.get().setOnFailed(this::currentTaskFinished);
        this.currentTask.get().setOnCancelled(this::currentTaskFinished);
        this.currentTask.get().setOnRunning(this::setStatusText);

        // Execute task
        new Thread(this.currentTask.get()).start();
    }

    @FXML
    private void cancelButtonClicked(ActionEvent event) {
        cancelTask();
    }
}
