import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import sun.plugin2.message.Message;;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class MainController extends Window implements Initializable {

    @FXML
    private ListView<String> filesListRemote;
    @FXML
    private ListView<String> filesListLocal;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        try {
            refreshLocalFiles();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread t = new Thread(() -> {
            try {
                while (true) {

                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileListMessage) {              //получение файлов на сервере
                        FileListMessage flr = (FileListMessage) am;
                        refreshServerFilesList(flr);
                    }

                    if (am instanceof FileMessage) {                //прием файла с сервера
                        FileMessage fm = (FileMessage) am;
                        downloadFile(fm);
                        refreshLocalFiles();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();

    }

    private void downloadFile(FileMessage fm) {
        if (Platform.isFxApplicationThread()) {
            saveFile(fm);
        } else {
            Platform.runLater(() -> saveFile(fm));
        }

    }

    private boolean saveFile(FileMessage fm) {
        FileChooser fileChooser = new FileChooser(); //это я потом буду делать окно с сохранением
        fileChooser.setInitialFileName(fm.getFilename());
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Any files", "*.*");
        fileChooser.getExtensionFilters().add(extensionFilter);
        try {
            Files.write(Paths.get("local_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
            Alert alert = new Alert(Alert.AlertType.INFORMATION,"",ButtonType.OK);
            alert.setHeaderText(filesListRemote.getSelectionModel().getSelectedItem());
            alert.setContentText("File downloaded");
            alert.showAndWait();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    public void downloadButton() {
        Network.sendMsg(
                new FileRequest(filesListRemote.getSelectionModel().getSelectedItem())
        );
    }

    @FXML
    private void refreshServerFilesList(FileListMessage flm) {
        if (Platform.isFxApplicationThread()) {
            filesListRemote.getItems().clear();
            flm.getFiles().forEach(s -> filesListRemote.getItems().add(s));
        } else {
            Platform.runLater(() -> {
                filesListRemote.getItems().clear();
                flm.getFiles().forEach(s -> filesListRemote.getItems().add(s));
            });
        }
    }

    public void deleteButton(ActionEvent actionEvent) {
        Network.sendMsg(new FileDeleteRequest(filesListRemote.getSelectionModel().getSelectedItem()));
    }

    public void refreshLocalButton(ActionEvent actionEvent) throws IOException {
        refreshLocalFiles();
    }

    private void refreshLocalFiles() throws IOException {
        ObservableList<String> list = FXCollections.observableArrayList();

        if (Platform.isFxApplicationThread()) {
            filesListLocal.getItems().clear();
            Files.list(Paths.get("local_storage")).map(p -> p.getFileName().toString()).forEach(o -> list.add(o));
            filesListLocal.setItems(list);
        } else {
            Platform.runLater(() -> {
                filesListLocal.getItems().clear();
                try {
                    Files.list(Paths.get("local_storage")).map(p -> p.getFileName().toString()).forEach(o -> list.add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                filesListLocal.setItems(list);
            });
        }
    }

    public void refreshServerButton(ActionEvent actionEvent) {
        Network.sendMsg(new FileListRequest());
    }

    public void deleteLocalFileButton(ActionEvent actionEvent) throws IOException {
        if (Platform.isFxApplicationThread()) {
            Files.delete(Paths.get("local_storage/" + filesListLocal.getSelectionModel().getSelectedItem()));
        } else {
            Platform.runLater(() -> {
                try {
                    Files.delete(Paths.get("local_storage/" + filesListLocal.getSelectionModel().getSelectedItem()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        }
        refreshLocalFiles();
    }

    public void uploadButton(ActionEvent actionEvent) {
        Network.sendMsg(new FileMessage(Paths.get("local_storage/"+filesListLocal.getSelectionModel().getSelectedItem())));
    }
}