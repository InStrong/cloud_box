import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends AbstractMessage {

    private String filename;
    private byte[] data;

    public byte[] getData() {
        return data;
    }

    public String getFilename() {

        return filename;
    }
    public FileMessage(Path path) {

        try {
            filename = path.getFileName().toString();
            data = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
