import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private ListView<String> filesList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(()-> {
            try {
                while(true){

                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileListRequest) {
                        FileListRequest flr = (FileListRequest) am;
                        refreshServerFilesList();

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
        refreshServerFilesList();

    }
    @FXML
    public void downloadButton(){
        Network.sendMsg(
                new FileRequest(filesList.getSelectionModel().getSelectedItem())
        );
    }
    @FXML
    private void refreshServerFilesList() {
        if (Platform.isFxApplicationThread()){
            try {
                filesList.setEditable(true);
                filesList.getItems().clear();
                ObservableList<String> list = FXCollections.observableArrayList();
                Files.list(Paths.get("local_storage")).map(p -> p.getFileName().toString()).forEach(o -> list.add(o));
                filesList.setItems(list);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    filesList.getItems().clear();
                    Files.list(Paths.get("local_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
