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
import omnivoxel.server.client.chunk.ChunkGenerator;
import omnivoxel.server.client.chunk.worldDataService.BasicWorldDataService;
import omnivoxel.server.world.BasicWorld;
import omnivoxel.server.world.World;

import java.io.IOException;
import java.util.Random;

public class ServerLauncher {
    private static final int PORT = 5000;

    public ServerLauncher() {
    }

    public static void main(String[] args) {
        new ServerLauncher().run(0L);
    }

    public void run(long seed) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        Random random = new Random(seed);

        World world = new BasicWorld();

        ChunkGenerator chunkGenerator = new ChunkGenerator(new BasicWorldDataService(random, world));

        try {
            ServerHandler serverHandler = new ServerHandler(new Server(chunkGenerator, world));

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(
                                            1048576,
                                            0,
                                            4,
                                            0,
                                            4
                                    ),
                                    new LengthFieldPrepender(4),
                                    serverHandler
                            );
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(PORT).sync();
            System.out.println("Server started on port " + PORT);

            future.channel().closeFuture().sync();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}