import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private SQLHandler sqlHandler = new SQLHandler();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg==null){
                return;
            }
            if (msg instanceof AuthMessage){
                AuthMessage am = new AuthMessage(((AuthMessage) msg).login,((AuthMessage) msg).password);
                if (sqlHandler.isAuthPassed(am.login, am.password)){
                    am.setAuthPassed(true);ctx.writeAndFlush(am);
                }
                
                else {
                    am.setAuthPassed(false);
                    ctx.writeAndFlush(am);
                }
            }
            if (msg instanceof FileListRequest){
                FileListMessage flm = new FileListMessage(getFilesList());
                ctx.writeAndFlush(flm);
            }

            if (msg instanceof FileRequest){
                FileRequest fr = (FileRequest) msg;

                if (Files.exists(
                        Paths.get("server_storage/"+fr.getFilename()))){
                    FileMessage fm = new FileMessage(
                            Paths.get("server_storage/"+fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }
            }

            if (msg instanceof FileDeleteRequest){
                FileDeleteRequest fdr = (FileDeleteRequest) msg;
                if (Files.exists(Paths.get("server_storage/"+fdr.getFilename()))){
                    Files.delete(Paths.get("server_storage/"+fdr.getFilename()));
                    FileListMessage flm = new FileListMessage(getFilesList());
                    ctx.writeAndFlush(flm);
                }
            }

            if (msg instanceof FileMessage){
                FileMessage fm = (FileMessage) msg;
                uploadFile(fm);
                FileListMessage flm = new FileListMessage(getFilesList());
                ctx.writeAndFlush(flm);
            }


        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void uploadFile(FileMessage fm) {
        FileChooser fileChooser = new FileChooser(); //это я потом буду делать окно с сохранением
        fileChooser.setInitialFileName(fm.getFilename());
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Any files", "*.*");
        fileChooser.getExtensionFilters().add(extensionFilter);
        try {
            Files.write(Paths.get("server_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getFilesList() {
        List<String> files = new ArrayList<>();
        try {
            Files.newDirectoryStream(
                    Paths.get("server_storage/")).forEach(
                            path -> files.add(path.getFileName().toString()));

        } catch (IOException e){
            e.printStackTrace();
        }
        return files;
    }
}
