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
import omnivoxel.common.BlockShape;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ServerLauncher {
    // TODO: Use a config file
    private static final int PORT = 5000;
    private static final String IP = "0.0.0.0";

    public static void main(String[] args) throws IOException {
        new ServerLauncher().run(100);
    }

    private static void createDirectories() throws IOException {
        Files.createDirectories(Path.of(ConstantServerSettings.WORLD_SAVE_LOCATION));
        Path chunkSaveLocation = Path.of(ConstantServerSettings.CHUNK_SAVE_LOCATION);
        clearDirectory(chunkSaveLocation);
        Files.createDirectories(chunkSaveLocation);
    }

    public static void clearDirectory(Path dir) throws IOException {
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return;
        }

        try (var paths = Files.walk(dir)) {
            paths
                    .sorted(Comparator.reverseOrder())
                    .filter(path -> !path.equals(dir))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete: " + path, e);
                        }
                    });
        }
    }

    public void run(int seed) throws IOException {
        createDirectories();

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        Map<String, String> blockIDMap = new HashMap<>();
        Map<String, BlockShape> blockShapeCache = new HashMap<>();

        ServerBlockService blockService = new ServerBlockService();

        ServerWorld world = new ServerWorld();

        try {
            Server server = new Server(seed, world, blockShapeCache, blockService, blockIDMap);
            Thread thread = new Thread(server::run, "Server Tick Loop");
            thread.start();
            ServerHandler serverHandler = new ServerHandler(server);

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4),
                                    serverHandler,
                                    new LengthFieldPrepender(4)
                            );
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(IP, PORT).sync();
            ServerLogger.logger.info("Server started at " + IP + ":" + PORT);

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}