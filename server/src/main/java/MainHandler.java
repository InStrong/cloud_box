import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import javafx.stage.FileChooser;
import sun.applet.Main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private SQLHandler sqlHandler = new SQLHandler();
    private static final Logger logger = Logger.getLogger(MainHandler.class.getName());

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        System.out.println("client connected...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            sqlHandler.connect();
            if (msg==null){
                return;
            }
            if (msg instanceof AuthMessage){
                logger.log(Level.INFO,"auth message");
                AuthMessage am = new AuthMessage(((AuthMessage) msg).login,((AuthMessage) msg).password);
                if (sqlHandler.isAuthPassed(am.login, am.password)){
                    logger.log(Level.INFO,am.login+" logged in");
                    am.setAuthPassed(true);ctx.writeAndFlush(am);
                }

                else {
                    am.setAuthPassed(false);
                    logger.log(Level.INFO,am.login+" tried to log in, but pass was wrong");
                    ctx.writeAndFlush(am);
                }
            }
            if (msg instanceof FileListRequest){
                logger.log(Level.INFO,"file list request");
                FileListMessage flm = new FileListMessage(getFilesList());
                ctx.writeAndFlush(flm);
            }

            if (msg instanceof RegistrationMessage){
                logger.log(Level.INFO,"registration message");
                RegistrationMessage rm = new RegistrationMessage(((RegistrationMessage) msg).getLogin(),((RegistrationMessage) msg).getPassword());
                if (!sqlHandler.isLoginUsed(rm.getLogin())) {
                    sqlHandler.registerUser(rm.getLogin(), rm.getPassword());
                    rm.setRegistrationPassed(true);
                    ctx.writeAndFlush(rm);
                }
                else {
                    rm.setRegistrationPassed(false);
                    ctx.writeAndFlush(rm);
                }

            }


            if (msg instanceof FileRequest){
                logger.log(Level.INFO,"file request message");
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
                logger.log(Level.INFO,"file message");
                FileMessage fm = (FileMessage) msg;
                uploadFile(fm);
                FileListMessage flm = new FileListMessage(getFilesList());
                ctx.writeAndFlush(flm);
            }


        }finally {
            ReferenceCountUtil.release(msg);
            sqlHandler.disconnect();
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
