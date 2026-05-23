package omnivoxel.server.client.chunk.result.generated;

import omnivoxel.common.annotations.NotNull;
import omnivoxel.common.network.NetworkService;
import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.server.PackageID;
import omnivoxel.server.client.ServerClient;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.result.ChunkResult;
import omnivoxel.world.chunk.Chunk;
import omnivoxel.world.chunk.SingleBlockChunk;

import java.util.ArrayList;
import java.util.List;

public abstract class GeneratedChunk {
    private static ChunkResult emptyChunk = null;

    // TODO: Move this to something similar to ChunkIO
    public static ChunkResult getResult(GeneratedChunk generatedChunk, ServerClient client) {
        if (generatedChunk instanceof EmptyGeneratedChunk && client != null) {
            if (emptyChunk == null) {
                emptyChunk = getResult(new EmptyGeneratedChunk(), null);
            }
            return emptyChunk;
        }

        Chunk<ServerBlock> chunkOut = new SingleBlockChunk<>(ServerBlock.AIR);
        List<ServerBlock> palette = new ArrayList<>();
        int[] chunk = new int[ConstantCommonSettings.BLOCKS_IN_CHUNK_PADDED];
        byte[] rotations = new byte[ConstantCommonSettings.BLOCKS_IN_CHUNK_PADDED];
        int chunkByteOffset = 0;
        for (int x = -1; x < ConstantCommonSettings.CHUNK_WIDTH + 1; x++) {
            for (int z = -1; z < ConstantCommonSettings.CHUNK_LENGTH + 1; z++) {
                for (int y = -1; y < ConstantCommonSettings.CHUNK_HEIGHT + 1; y++) {
                    ServerBlock block = generatedChunk.getBlock(x, y, z);
                    byte rotation = generatedChunk.getBlockRotation(x, y, z);
                    if (!palette.contains(block)) {
                        palette.add(block);
                    }
                    if (x > 0 && x < ConstantCommonSettings.CHUNK_WIDTH &&
                            y > 0 && y < ConstantCommonSettings.CHUNK_HEIGHT &&
                            z > 0 && z < ConstantCommonSettings.CHUNK_LENGTH) {
                        chunkOut = chunkOut.setBlock(x, y, z, block, rotation);
                    }
                    chunk[chunkByteOffset] = palette.indexOf(block);
                    rotations[chunkByteOffset] = rotation;
                    chunkByteOffset++;
                }
            }
        }

        if (client != null) {
            // TODO: This shouldn't handle anything server/client related
            palette.forEach(serverBlock -> {
                if (client.registerBlockID(serverBlock.id())) {
                    NetworkService.sendBytes(client.getCTX().channel(), PackageID.REGISTER_BLOCK, null, serverBlock.getBytes());
                }
            });
        }

        List<Integer> runBlockIDs = new ArrayList<>();
        List<Integer> runCounts = new ArrayList<>();
        List<Byte> runRotations = new ArrayList<>();
        int currentID = chunk[0];
        byte currentRotation = rotations[0];
        int count = 1;
        for (int i = 1; i < chunk.length; i++) {
            if (chunk[i] == currentID && rotations[i] == currentRotation) {
                count++;
                continue;
            }
            runBlockIDs.add(currentID);
            runCounts.add(count);
            runRotations.add(currentRotation);
            currentID = chunk[i];
            currentRotation = rotations[i];
            count = 1;
        }
        runBlockIDs.add(currentID);
        runCounts.add(count);
        runRotations.add(currentRotation);

        byte[] chunkBytes = new byte[runBlockIDs.size() * 9];
        for (int i = 0; i < runBlockIDs.size(); i++) {
            int blockID = runBlockIDs.get(i);
            int blockCount = runCounts.get(i);
            int offset = i * 9;
            chunkBytes[offset] = (byte) (blockID >> 24);
            chunkBytes[offset + 1] = (byte) (blockID >> 16);
            chunkBytes[offset + 2] = (byte) (blockID >> 8);
            chunkBytes[offset + 3] = (byte) blockID;
            chunkBytes[offset + 4] = (byte) (blockCount >> 24);
            chunkBytes[offset + 5] = (byte) (blockCount >> 16);
            chunkBytes[offset + 6] = (byte) (blockCount >> 8);
            chunkBytes[offset + 7] = (byte) blockCount;
            chunkBytes[offset + 8] = runRotations.get(i);
        }

        List<byte[]> paletteBytesList = new ArrayList<>();
        int paletteLength = 0;
        for (ServerBlock block : palette) {
            byte[] blockBytes = block.getBlockBytes();
            byte[] bytes = new byte[blockBytes.length];
            System.arraycopy(blockBytes, 0, bytes, 0, blockBytes.length);
            paletteBytesList.add(bytes);
            paletteLength += bytes.length;
        }

        byte[] paletteBytes = new byte[2 + paletteLength];
        paletteBytes[0] = (byte) (palette.size() >> 8);
        paletteBytes[1] = (byte) palette.size();
        int paletteIndex = 2;
        for (byte[] paletteBites : paletteBytesList) {
            System.arraycopy(paletteBites, 0, paletteBytes, paletteIndex, paletteBites.length);
            paletteIndex += paletteBites.length;
        }

        byte[] out = new byte[chunkBytes.length + paletteBytes.length];
        System.arraycopy(paletteBytes, 0, out, 0, paletteBytes.length);
        System.arraycopy(chunkBytes, 0, out, paletteBytes.length, chunkBytes.length);

        return new ChunkResult(out, chunkOut);
    }

    abstract public ServerBlock getBlock(int x, int y, int z);

    abstract public GeneratedChunk setBlock(int x, int y, int z, @NotNull ServerBlock block);

    abstract public byte getBlockRotation(int x, int y, int z);

    abstract public GeneratedChunk setBlockRotation(int x, int y, int z, byte rotation);
}
