/*
 * This application is developed and maintained by Alexander Javier Lopez
 *
 * Name: Alexander Lopez
 * Created: 2/10/2023
 */

package MainProgramStructure;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * This class holds the controller for the program.
 */
public class MainGUIController implements Initializable {
    @FXML
    private Button select;

    @FXML
    private Button generate;

    @FXML
    private Button save;

    @FXML
    private ProgressBar progressBar;

    private int numFiles;
    private int numProcessed;
    private File directory;
    private File[] childFiles;
    private File tempFile = new File("temp.plys");

    /**
     * This method initializes the controller and sets the progress bar to 0%.
     * @param url ignored.
     * @param resourceBundle ignored.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        progressBar.setProgress(0);
    }

    /**
     * This method allows the user to select a
     */
    @FXML
    private void selectDirectory(){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select the directory containing the PLY files to be processed");
        directory = chooser.showDialog(null);
        numFiles = Objects.requireNonNull(directory.listFiles()).length;
        numProcessed = 0;
        childFiles = directory.listFiles();
        updateProgress();
    }

    @FXML
    private void generatePLYS(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Started File generation. This may take a few minutes. Press OK to continue", ButtonType.OK);
        alert.showAndWait();
        for (File file:childFiles) {
            try(Scanner in = new Scanner(new FileInputStream(file))) {
                tempFile.createNewFile();
                PrintWriter write = new PrintWriter(new FileOutputStream(tempFile, true), true);
                while(in.hasNextLine()){
                    write.println(in.nextLine());
                }
                write.close();
                numProcessed++;
            } catch (IOException e) {
                Alert temp = new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK);
                temp.show();
            }
            updateProgress();
        }
        Alert alert2 = new Alert(Alert.AlertType.INFORMATION, "Generating .PLYS file Complete", ButtonType.OK);
        alert2.show();
    }

    @FXML
    private void saveFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose save destination");
        File newFile = chooser.showSaveDialog(null);
        try{
            Files.move(tempFile.toPath(), newFile.toPath());
        } catch (IOException e){
            Alert alert = new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK);
            alert.show();
        }
        numFiles = 0;
        numProcessed = 0;
        directory = null;
        tempFile = new File("temp.plys");
    }

    private void updateProgress(){
        progressBar.setProgress((double)numProcessed/numFiles);
    }
}
