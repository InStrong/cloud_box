import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
                        refreshLocalFilesList();

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
        refreshLocalFilesList();

    }
    @FXML
    public void downloadButton(){
        Network.sendMsg(
                new FileRequest(filesList.getSelectionModel().getSelectedItem())
        );
    }
    @FXML
    private void refreshLocalFilesList() {
        if (Platform.isFxApplicationThread()){
            try {
                filesList.getItems().clear();
                Files.list(Paths.get("local_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems());
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
