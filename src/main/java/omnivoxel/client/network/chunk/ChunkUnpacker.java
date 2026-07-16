package omnivoxel.client.network.chunk;

import io.netty.buffer.ByteBuf;
import omnivoxel.client.game.graphics.block.BlockWithMesh;
import omnivoxel.client.game.world.ClientWorld;
import omnivoxel.client.network.chunk.worldDataService.ClientWorldDataService;
import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.util.math.Position3D;
import omnivoxel.world.block.Block;
import omnivoxel.world.block.BlockService;
import omnivoxel.world.chunk.Chunk;
import omnivoxel.world.chunk.ChunkShell;
import omnivoxel.world.chunk.SingleBlockChunk;

public class ChunkUnpacker {
    private static BlockWithMesh AIR = null;

    public static void unpackChunkPadded(ByteBuf byteBuf, Position3D pos, ClientWorldDataService worldDataService, BlockService<BlockWithMesh> blockService, ClientWorld world) {
        if (AIR == null) {
            AIR = new BlockWithMesh("omnivoxel:air", worldDataService.getBlock("omnivoxel:air"));
        }

        byteBuf.readerIndex(24);

        Block[] palette = new Block[byteBuf.readShort()];

        for (int i = 0; i < palette.length; i++) {
            short len = byteBuf.readShort();
            StringBuilder id = new StringBuilder();

            for (int j = 0; j < len; j++) {
                id.append((char) byteBuf.readByte());
            }

            palette[i] = new Block(id.toString());
        }

        Chunk<BlockWithMesh> center = new SingleBlockChunk<>(AIR);
        Chunk<BlockWithMesh> negX = new ChunkShell<>();
        Chunk<BlockWithMesh> posX = new ChunkShell<>();
        Chunk<BlockWithMesh> negY = new ChunkShell<>();
        Chunk<BlockWithMesh> posY = new ChunkShell<>();
        Chunk<BlockWithMesh> negZ = new ChunkShell<>();
        Chunk<BlockWithMesh> posZ = new ChunkShell<>();

        int x = -1, y = -1, z = -1;

        for (int i = 0; i < ConstantCommonSettings.BLOCKS_IN_CHUNK_PADDED; ) {
            int blockID = byteBuf.readInt();
            int blockCount = byteBuf.readInt();
            byte rotation = (byte) (byteBuf.readByte() & 3);
            BlockWithMesh block = blockService.getBlock(palette[blockID].id());

            for (int j = 0; j < blockCount; j++) {
                if (x >= 0 && x < ConstantCommonSettings.CHUNK_WIDTH &&
                        y >= 0 && y < ConstantCommonSettings.CHUNK_HEIGHT &&
                        z >= 0 && z < ConstantCommonSettings.CHUNK_LENGTH) {

                    center = center.setBlock(x, y, z,
                            block,
                            rotation);
                } else if (x == -1 && y >= 0 && y < ConstantCommonSettings.CHUNK_HEIGHT &&
                        z >= 0 && z < ConstantCommonSettings.CHUNK_LENGTH) {

                    negX = negX.setBlock(
                            ConstantCommonSettings.CHUNK_WIDTH - 1,
                            y,
                            z,
                            block,
                            rotation);
                } else if (x == ConstantCommonSettings.CHUNK_WIDTH &&
                        y >= 0 && y < ConstantCommonSettings.CHUNK_HEIGHT &&
                        z >= 0 && z < ConstantCommonSettings.CHUNK_LENGTH) {

                    posX = posX.setBlock(
                            0,
                            y,
                            z,
                            block,
                            rotation);
                } else if (z == -1 && x >= 0 && x < ConstantCommonSettings.CHUNK_WIDTH &&
                        y >= 0 && y < ConstantCommonSettings.CHUNK_HEIGHT) {

                    negZ = negZ.setBlock(
                            x,
                            y,
                            ConstantCommonSettings.CHUNK_LENGTH - 1,
                            block,
                            rotation);
                } else if (z == ConstantCommonSettings.CHUNK_LENGTH &&
                        x >= 0 && x < ConstantCommonSettings.CHUNK_WIDTH &&
                        y >= 0 && y < ConstantCommonSettings.CHUNK_HEIGHT) {

                    posZ = posZ.setBlock(
                            x,
                            y,
                            0,
                            block,
                            rotation);
                } else if (y == -1 && x >= 0 && x < ConstantCommonSettings.CHUNK_WIDTH &&
                        z >= 0 && z < ConstantCommonSettings.CHUNK_LENGTH) {

                    negY = negY.setBlock(
                            x,
                            ConstantCommonSettings.CHUNK_HEIGHT - 1,
                            z,
                            block,
                            rotation);
                } else if (y == ConstantCommonSettings.CHUNK_HEIGHT &&
                        x >= 0 && x < ConstantCommonSettings.CHUNK_WIDTH &&
                        z >= 0 && z < ConstantCommonSettings.CHUNK_LENGTH) {

                    posY = posY.setBlock(
                            x,
                            0,
                            z,
                            block,
                            rotation);
                }

                y++;
                if (y > ConstantCommonSettings.CHUNK_HEIGHT) {
                    y = -1;
                    z++;

                    if (z > ConstantCommonSettings.CHUNK_LENGTH) {
                        z = -1;
                        x++;
                    }
                }
            }

            i += blockCount;
        }

        byteBuf.release();

        world.addChunkData(pos, center, false);
        world.addChunkData(pos.add(-1, 0, 0), negX, true);
        world.addChunkData(pos.add(1, 0, 0), posX, true);
        world.addChunkData(pos.add(0, -1, 0), negY, true);
        world.addChunkData(pos.add(0, 1, 0), posY, true);
        world.addChunkData(pos.add(0, 0, -1), negZ, true);
        world.addChunkData(pos.add(0, 0, 1), posZ, true);
    }
}
