/*
 * Created: 2/10/2023
 */

package generator;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Scanner;

/**
 * This class holds the controller for the program.
 *
 * @author Alex Lopez & Patrick Rafferty
 */
public class MainGUIController {
    /**
     * Prefix added to temp files.
     */
    private static final String TEMP_PREFIX = "PLY_Gen";
    /**
     * plys suffix.
     */
    private static final String PLYS_SUFFIX = ".plys";
    /**
     * A progress bar.
     */
    @FXML
    private ProgressBar progressBar;
    /**
     * Number of files being processed.
     */
    private int numFiles;
    /**
     * Number of files processed.
     */
    private int numProcessed;
    /**
     * Directory being worked with.
     */
    private File directory;
    /**
     * List of files in the working directory.
     */
    private File[] childFiles;
    /**
     * Temporary file for generating .plys.
     */
    private File tempFile;
    /**
     * The stage.
     */
    @FXML
    private Stage stage;
    
    /**
     * This method initializes the controller.
     *
     * @throws IOException If there was a problem creating a temp file.
     */
    public void initialize() throws IOException {
        tempFile = File.createTempFile(TEMP_PREFIX, PLYS_SUFFIX);
        stage.show();
    }
    
    /**
     * This method allows the user to select a directory.
     */
    @FXML
    private void selectDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select the directory containing the PLY files to be processed");
        directory = chooser.showDialog(stage);
        numFiles = Objects.requireNonNull(directory.listFiles()).length;
        numProcessed = 0;
        childFiles = directory.listFiles();
        updateProgress();
    }
    
    /**
     * Generates the plys file.
     */
    @FXML
    private void generatePLYS() {
        new Alert(Alert.AlertType.INFORMATION,
                "Started File generation. This may take a few minutes. Press OK to continue",
                ButtonType.OK).showAndWait();
        for (File file : childFiles) {
            try (Scanner in = new Scanner(new FileInputStream(file), StandardCharsets.UTF_8)) {
                try (PrintWriter write = new PrintWriter(new FileOutputStream(tempFile, true), true,
                        StandardCharsets.UTF_8)) {
                    while (in.hasNextLine()) {
                        write.println(in.nextLine());
                    }
                }
                numProcessed++;
            } catch (IOException e) {
                new Alert(Alert.AlertType.WARNING, "Read error: " + e.getMessage(),
                        ButtonType.OK).show();
            }
            updateProgress();
        }
        new Alert(Alert.AlertType.INFORMATION, "Generating .PLYS file Complete",
                ButtonType.OK).show();
    }
    
    /**
     * Saves the file.
     *
     * @throws IOException todo
     */
    @FXML
    private void saveFile() throws IOException {
        //todo: better error handling.
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter =
                new FileChooser.ExtensionFilter("PLYS", '*' + PLYS_SUFFIX);
        chooser.getExtensionFilters().add(extensionFilter);
        chooser.setSelectedExtensionFilter(extensionFilter);
        chooser.setTitle("Choose save destination");
        File newFile = chooser.showSaveDialog(stage);
        try {
            Files.move(tempFile.toPath(), newFile.toPath());
        } catch (IOException e) {
            new Alert(Alert.AlertType.WARNING, "Save error: " + e.getMessage(),
                    ButtonType.OK).show();
        }
        numFiles = 0;
        numProcessed = 0;
        directory = null;
        Files.delete(tempFile.toPath());
        tempFile = File.createTempFile(TEMP_PREFIX, PLYS_SUFFIX);
    }
    
    /**
     * Update the progress bar.
     */
    private void updateProgress() {
        progressBar.setProgress((double) numProcessed / numFiles);
    }
}
