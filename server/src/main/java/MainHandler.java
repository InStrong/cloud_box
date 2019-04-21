import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg==null){
                return;
            }
            if (msg instanceof FileListRequest){
                FileListRequest flr = new FileListRequest(getFilesList());
                ctx.writeAndFlush(flr);
            }

            if (msg instanceof FileRequest){
                FileRequest fr = (FileRequest) msg;

                if (Files.exists(
                        Paths.get("server_storage"+ File.pathSeparator+fr.getFilename()))){
                    FileMessage fm = new FileMessage(
                            Paths.get("server_storage"+ File.pathSeparator+fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }
            }


        }finally {
            ReferenceCountUtil.release(msg);
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
