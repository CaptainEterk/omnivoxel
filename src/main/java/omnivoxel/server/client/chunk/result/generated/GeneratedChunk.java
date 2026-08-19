package omnivoxel.server.client.chunk.result.generated;

import omnivoxel.common.annotations.NotNull;
import omnivoxel.common.network.NetworkService;
import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.server.PackageID;
import omnivoxel.server.client.ServerClient;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.result.ChunkResult;
import omnivoxel.util.bytes.ByteUtils;
import omnivoxel.world.chunk.Chunk;
import omnivoxel.world.chunk.SingleBlockChunk;

import java.util.ArrayList;
import java.util.List;

public abstract class GeneratedChunk {
    public static ChunkResult getResult(GeneratedChunk generatedChunk) {
        int lod = generatedChunk.getLOD();

        int width = ConstantCommonSettings.CHUNK_WIDTH >> lod;
        int height = ConstantCommonSettings.CHUNK_HEIGHT >> lod;
        int length = ConstantCommonSettings.CHUNK_LENGTH >> lod;

        int paddedBlocks = (width + 2) * (height + 2) * (length + 2);

        Chunk<ServerBlock> chunkOut = new SingleBlockChunk<>(generatedChunk.getBlock(0, 0, 0), lod);

        List<ServerBlock> palette = new ArrayList<>();

        int[] chunk = new int[paddedBlocks];
        byte[] rotations = new byte[paddedBlocks];

        int chunkByteOffset = 0;

        for (int x = -1; x < width + 1; x++) {
            for (int z = -1; z < length + 1; z++) {
                for (int y = -1; y < height + 1; y++) {

                    ServerBlock block = generatedChunk.getBlock(x, y, z);
                    byte rotation = generatedChunk.getBlockRotation(x, y, z);

                    int paletteIndex = palette.indexOf(block);
                    if (paletteIndex == -1) {
                        paletteIndex = palette.size();
                        palette.add(block);
                    }

                    if (x > 0 && x < width &&
                            y > 0 && y < height &&
                            z > 0 && z < length) {

                        chunkOut = chunkOut.setBlock(
                                x,
                                y,
                                z,
                                block,
                                rotation
                        );
                    }

                    chunk[chunkByteOffset] = paletteIndex;
                    rotations[chunkByteOffset] = rotation;
                    chunkByteOffset++;
                }
            }
        }

        List<Integer> runBlockIDs = new ArrayList<>();
        List<Integer> runCounts = new ArrayList<>();
        List<Byte> runRotations = new ArrayList<>();

        int currentID = chunk[0];
        byte currentRotation = rotations[0];
        int count = 1;

        for (int i = 1; i < chunk.length; i++) {
            if (chunk[i] == currentID &&
                    rotations[i] == currentRotation) {

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

        for (byte[] paletteBytesEntry : paletteBytesList) {
            System.arraycopy(
                    paletteBytesEntry,
                    0,
                    paletteBytes,
                    paletteIndex,
                    paletteBytesEntry.length
            );

            paletteIndex += paletteBytesEntry.length;
        }

        byte[] out = new byte[Integer.BYTES + paletteBytes.length + chunkBytes.length];

        ByteUtils.addInt(out, lod, 0);

        System.arraycopy(
                paletteBytes,
                0,
                out,
                Integer.BYTES,
                paletteBytes.length
        );

        System.arraycopy(
                chunkBytes,
                0,
                out,
                Integer.BYTES + paletteBytes.length,
                chunkBytes.length
        );

        return new ChunkResult(out, chunkOut);
    }

    public abstract ServerBlock getBlock(int x, int y, int z);

    public abstract GeneratedChunk setBlock(
            int x,
            int y,
            int z,
            @NotNull ServerBlock block
    );

    public abstract byte getBlockRotation(int x, int y, int z);

    public abstract GeneratedChunk setBlockRotation(
            int x,
            int y,
            int z,
            byte rotation
    );

    public abstract int getLOD();
}