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

import java.io.IOException;

public class ServerLauncher {
    private static final int PORT = 5000;
    private static final String IP = "192.168.14.162";

    public ServerLauncher() {
    }

    public static void main(String[] args) {
        new ServerLauncher().run(100L);
    }

    public void run(long seed) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBlockService blockService = new ServerBlockService();

        ServerWorld world = new ServerWorld(blockService.getBlock("omnivoxel:air"));

        try {
            ServerHandler serverHandler = new ServerHandler(new Server(seed, world, blockService));

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
            System.out.println("Server started at " + IP + ":" + PORT);

            future.channel().closeFuture().sync();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}