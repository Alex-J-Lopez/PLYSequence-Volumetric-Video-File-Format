/*
 * Created: 2/10/2023
 */

package generator;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPOutputStream;

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
     * List of files in the working directory.
     */
    private File[] childFiles;
    /**
     * Temporary file for generating .plys.
     */
    private Path tempFile;
    /**
     * The stage.
     */
    @FXML
    private Stage stage;
    /**
     * Which compression mode is active.
     */
    @FXML
    private ToggleGroup compressionMode;
    /**
     * Button for GZIP compression.
     */
    @FXML
    private RadioButton gZIPCompression;
    
    /**
     * This method initializes the controller.
     */
    public void initialize() {
        stage.show();
    }
    
    /**
     * This method allows the user to select a directory.
     */
    private void selectDirectory() {
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select the directory containing the PLY files to be processed");
        File directory = chooser.showDialog(stage);
        childFiles = directory.listFiles();
    }
    
    /**
     * Generates the plys file.
     * @throws IOException todo
     */
    private void generatePLYS() throws IOException {
        new Alert(Alert.AlertType.INFORMATION,
                "Started File generation. This may take a few minutes. Press OK to continue.",
                ButtonType.OK).showAndWait();
        if (compressionMode.getSelectedToggle() == gZIPCompression) {
            tempFile = Files.createTempFile(TEMP_PREFIX, PLYS_SUFFIX + ".gz");
        } else {
            tempFile = Files.createTempFile(TEMP_PREFIX, PLYS_SUFFIX);
        }
        int numProcessed = 0;
        try (OutputStream outputStream = getOutputStream()) {
            for (File file : childFiles) {
                try (InputStream in = new FileInputStream(file)) {
                    outputStream.write(in.readAllBytes());
                    numProcessed++;
                } catch (IOException e) {
                    new Alert(Alert.AlertType.WARNING, "Read error: " + e.getMessage(),
                            ButtonType.OK).show();
                }
                progressBar.setProgress((double) numProcessed / childFiles.length);
            }
        }
        new Alert(Alert.AlertType.INFORMATION, "Generating .PLYS file Complete",
                ButtonType.OK).show();
    }
    
    private OutputStream getOutputStream() throws IOException {
        OutputStream outputStream = new FileOutputStream(tempFile.toFile(), true);
        if (compressionMode.getSelectedToggle() == gZIPCompression) {
            // Note: FilterOutputStream closes its filtered stream with itself.
            outputStream = new GZIPOutputStream(outputStream);
        }
        return outputStream;
    }
    
    /**
     * Saves the file.
     */
    private void saveFile() {
        //todo: better error handling.
        final FileChooser chooser = new FileChooser();
        final FileChooser.ExtensionFilter extensionFilter;
        if (compressionMode.getSelectedToggle() == gZIPCompression) {
            extensionFilter =
                new FileChooser.ExtensionFilter("Compressed PLYS", '*' + PLYS_SUFFIX + ".gz");
        } else {
             extensionFilter =
                new FileChooser.ExtensionFilter("PLYS", '*' + PLYS_SUFFIX);
        }
        chooser.getExtensionFilters().add(extensionFilter);
        chooser.setSelectedExtensionFilter(extensionFilter);
        chooser.setTitle("Choose save destination");
        final File newFile = chooser.showSaveDialog(stage);
        try {
            Files.move(tempFile, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            new Alert(Alert.AlertType.WARNING, "Save error: " + e.getMessage(),
                    ButtonType.OK).show();
        }
    }
    
    /**
     * Runs the generation process.
     * @throws IOException todo
     */
    @FXML
    private void runGeneration() throws IOException {
        selectDirectory();
        generatePLYS();
        saveFile();
    }
}
