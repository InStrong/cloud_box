import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

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
    @FXML
    TextField loginArea;
    @FXML
    PasswordField passArea;
    @FXML
    GridPane rootNode;
    @FXML
    GridPane registration;
    @FXML
    TextField loginRegistration;
    @FXML
    PasswordField passRegistration;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
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
                    if (am instanceof AuthMessage) {                //проверка прохождения авторизации
                        AuthMessage aum = (AuthMessage) am;
                        if (aum.isAuthPassed) {
                            changeScene("main", rootNode);
                        } else {
                            loginArea.clear();
                            passArea.clear();
                            sendAlert("Authorization failed", "Wrong login or password", Alert.AlertType.ERROR);
                        }
                    }
                    if (am instanceof RegistrationMessage) {                //проверка прохождения авторизации
                        RegistrationMessage rm = (RegistrationMessage) am;
                        if (rm.isRegistrationPassed()) {
                            sendAlert("Registration is successful", "Now you can login", Alert.AlertType.INFORMATION);
                            changeScene("login", registration);
                        } else {
                            sendAlert("Registration failed", "Login is already taken", Alert.AlertType.ERROR);
                        }

                    }
                }
            } catch (IOException | ClassNotFoundException e) {
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
        try {
            Files.write(Paths.get("local_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
            sendAlert(filesListRemote.getSelectionModel().getSelectedItem(), "File downloaded", Alert.AlertType.INFORMATION);
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
        sendAlert(filesListRemote.getSelectionModel().getSelectedItem(), "File deleted", Alert.AlertType.INFORMATION);

    }

    public void refreshLocalButton(ActionEvent actionEvent) throws IOException {
        refreshLocalFiles();

    }

    private void refreshLocalFiles() throws IOException {
        ObservableList<String> list = FXCollections.observableArrayList();

        if (Platform.isFxApplicationThread()) {
            filesListLocal.getItems().clear();
            Files.list(Paths.get("local_storage")).map(p -> p.getFileName().toString()).forEach(list::add);
            filesListLocal.setItems(list);
        } else {
            Platform.runLater(() -> {
                filesListLocal.getItems().clear();
                try {
                    Files.list(Paths.get("local_storage")).map(p -> p.getFileName().toString()).forEach(list::add);
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
        sendAlert(filesListLocal.getSelectionModel().getSelectedItem(), "File deleted", Alert.AlertType.INFORMATION);

    }

    public void uploadButton(ActionEvent actionEvent) {
        Network.sendMsg(new FileMessage(Paths.get("local_storage/" + filesListLocal.getSelectionModel().getSelectedItem())));
        sendAlert(filesListRemote.getSelectionModel().getSelectedItem(), "File was uploaded", Alert.AlertType.INFORMATION);

    }

    public void authAction(ActionEvent actionEvent) {
        Network.sendMsg(new AuthMessage(loginArea.getText(), Encrypter.encrypt(passArea.getText())));
    }


    private void changeScene(String screen, GridPane gridPane) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Parent mainScene;
                    try {
                        mainScene = FXMLLoader.load(getClass().getResource("/" + screen + ".fxml"));
                        ((Stage) gridPane.getScene().getWindow()).setScene(new Scene(mainScene));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Parent mainScene;
            try {
                mainScene = FXMLLoader.load(getClass().getResource("/" + screen + ".fxml"));
                ((Stage) gridPane.getScene().getWindow()).setScene(new Scene(mainScene));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void regWindow(ActionEvent actionEvent) {
        changeScene("registration", rootNode);
    }

    public void regAction(ActionEvent actionEvent) {
        Network.sendMsg(new RegistrationMessage(loginRegistration.getText(), Encrypter.encrypt(passRegistration.getText())));
    }

    private void sendAlert(String headerText, String contentText, Alert.AlertType type) {
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(type, "", ButtonType.OK);
            alert.setHeaderText(headerText);
            alert.setContentText(contentText);
            alert.showAndWait();
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(type, "", ButtonType.OK);
                alert.setHeaderText(headerText);
                alert.setContentText(contentText);
                alert.showAndWait();
            });
        }
    }

}