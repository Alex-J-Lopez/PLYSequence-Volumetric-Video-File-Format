/*
 * Created: 2/10/2023
 */

package generator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * This class holds the Model for the application.
 * @author Alex Lopez
 */
public class ProgramDriver extends Application {

    @Override
    public void start(final Stage primaryStage) throws java.io.IOException {
        FXMLLoader.load(
                Objects.requireNonNull(getClass().getResource("MainGUI.fxml")));
    }
}
