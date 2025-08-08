package omnivoxel.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;
import omnivoxel.util.log.Logger;

public class ServerLauncher {
    // TODO: Use a config file
    private static final int PORT = 5000;
    private static final String IP = "localhost";
    private final Logger logger;

    public ServerLauncher() {
        logger = new Logger("Server", true);
    }

    public static void main(String[] args) {
        new ServerLauncher().run(100);
    }

    public void run(int seed) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBlockService blockService = new ServerBlockService();

        ServerWorld world = new ServerWorld();

        try {
            Server server = new Server(seed, world, blockService, logger);
            Thread thread = new Thread(server::run, "Server Tick Loop");
            thread.start();
            ServerHandler serverHandler = new ServerHandler(server);

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4), new LengthFieldPrepender(4), serverHandler);
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(IP, PORT).sync();
            logger.info("Server started at " + IP + ":" + PORT);

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}