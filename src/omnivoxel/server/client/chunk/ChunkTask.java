package omnivoxel.server.client.chunk;

import io.netty.channel.ChannelHandlerContext;

public record ChunkTask(ChannelHandlerContext ctx, int x, int y, int z) {
}