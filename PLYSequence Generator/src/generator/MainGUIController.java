/*
 * Created: 2/10/2023
 */

package generator;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * This class holds the controller for the program.
 *
 * @author Alex Lopez and Patrick Rafferty
 */
public class MainGUIController {
    /**
     * A progress bar.
     */
    @FXML
    private ProgressBar progressBar;
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
     * Generates the plys file.
     *
     * @throws IOException if there was a problem reading or writing.
     */
    @FXML
    private void runGeneration() throws IOException {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select the directory containing the PLY files to be processed");
        final PLYSGenerator plysGenerator;
        try {
            plysGenerator = new PLYSGenerator(
                    Objects.requireNonNull(directoryChooser.showDialog(stage).listFiles()),
                    compressionMode.getSelectedToggle() == gZIPCompression,
                    Float.parseFloat(new TextInputDialog().showAndWait().get()));
        } catch (IOException e) {
            new Alert(Alert.AlertType.WARNING, "Could not read directory.").show();
            throw e;
        }
        new Alert(Alert.AlertType.INFORMATION, "Generating .PLYS file Complete",
                ButtonType.OK).show();
        final FileChooser fileChooser = new FileChooser();
        final FileChooser.ExtensionFilter extensionFilter;
        if (compressionMode.getSelectedToggle() == gZIPCompression) {
            extensionFilter = new FileChooser.ExtensionFilter("Compressed PLYS",
                    '*' + PLYSGenerator.PLYS_SUFFIX + ".gz");
        } else {
            extensionFilter =
                    new FileChooser.ExtensionFilter("PLYS", '*' + PLYSGenerator.PLYS_SUFFIX);
        }
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setSelectedExtensionFilter(extensionFilter);
        fileChooser.setTitle("Choose save destination");
        try {
            plysGenerator.save(fileChooser.showSaveDialog(stage));
        } catch (IOException e) {
            new Alert(Alert.AlertType.WARNING, "Save error: " + e.getMessage(),
                    ButtonType.OK).show();
            throw e;
        }
    }
}
