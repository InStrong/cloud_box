import java.util.List;

public class FileListRequest extends AbstractMessage {

    private List<String> files;

    public List<String> getFiles() {
        return files;
    }

    public FileListRequest(List<String> files) {
        this.files = files;
    }
}
