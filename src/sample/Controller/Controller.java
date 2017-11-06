package sample.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import sample.Model.Macroprocessor;
import sample.Model.Syntax;

import java.io.File;

public class Controller {

    private Macroprocessor macroprocessor;
    private File file;

    @FXML
    private Label filePathLabel;
    @FXML
    private TextArea filePathArea;
    @FXML
    private Label logLabel;
    @FXML
    private TextArea logArea;

    public Controller(){
        macroprocessor = new Macroprocessor();
    }

    public void openFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("UUM32MASM", "*.uum32masm"));
        file = fileChooser.showOpenDialog(null);
        if(file != null){
            filePathArea.setText(file.getAbsolutePath());
        }
    }

    public void start(){
        if(file == null){
            filePathArea.setText("Файл не выбран.");
            return;
        }
        try {
            macroprocessor.transform(file);
        } catch (Syntax.SyntaxException e) {
            logArea.appendText("ОШИБКА: " + e.getMessage() + '\n');
            return;
        }
        logArea.appendText("Файл успешно создан." + '\n');
    }
}
