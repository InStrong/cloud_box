import java.util.List;

public class FileListMessage extends AbstractMessage {
    private List<String> files;

    public List<String> getFiles() {
        return files;
    }

    public FileListMessage(List<String> files) {
        this.files = files;
    }
}
