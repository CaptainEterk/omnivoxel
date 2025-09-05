package omnivoxel.server;

import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.worldDataService.ChunkInfo;
import omnivoxel.util.math.Position3D;
import omnivoxel.world.chunk.Chunk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerWorld {
    private final Map<Position3D, ChunkValue> chunks;
    private final Map<Position3D, ChunkInfo> chunkInfo;
    private int request = 0;

    public ServerWorld() {
        chunks = new ConcurrentHashMap<>();
        chunkInfo = new ConcurrentHashMap<>();
    }

    private static byte[] getBytes(Chunk<ServerBlock> chunk) {
        // Step 1: Build the palette
        List<ServerBlock> palette = new ArrayList<>();
        int[] chunkIndices = new int[ConstantGameSettings.CHUNK_WIDTH
                * ConstantGameSettings.CHUNK_LENGTH
                * ConstantGameSettings.CHUNK_HEIGHT];
        int offset = 0;

        for (int x = 0; x < ConstantGameSettings.CHUNK_WIDTH; x++) {
            for (int z = 0; z < ConstantGameSettings.CHUNK_LENGTH; z++) {
                for (int y = 0; y < ConstantGameSettings.CHUNK_HEIGHT; y++) {
                    ServerBlock block = chunk.getBlock(x, y, z);
                    if (!palette.contains(block)) {
                        palette.add(block);
                    }
                    chunkIndices[offset++] = palette.indexOf(block);
                }
            }
        }

        // Step 2: RLE compress the indices
        List<BlockIDCount> chunkData = new ArrayList<>();
        int currentID = chunkIndices[0];
        int count = 1;

        for (int i = 1; i < chunkIndices.length; i++) {
            int id = chunkIndices[i];
            if (id != currentID) {
                chunkData.add(new BlockIDCount(currentID, count));
                currentID = id;
                count = 1;
            } else {
                count++;
            }
        }
        chunkData.add(new BlockIDCount(currentID, count));

        // Step 3: Convert RLE data to bytes
        byte[] chunkBytes = new byte[chunkData.size() * 8];
        for (int i = 0; i < chunkData.size(); i++) {
            BlockIDCount blockIDCount = chunkData.get(i);
            chunkBytes[i * 8] = (byte) (blockIDCount.blockID() >> 24);
            chunkBytes[i * 8 + 1] = (byte) (blockIDCount.blockID() >> 16);
            chunkBytes[i * 8 + 2] = (byte) (blockIDCount.blockID() >> 8);
            chunkBytes[i * 8 + 3] = (byte) blockIDCount.blockID();
            chunkBytes[i * 8 + 4] = (byte) (blockIDCount.count() >> 24);
            chunkBytes[i * 8 + 5] = (byte) (blockIDCount.count() >> 16);
            chunkBytes[i * 8 + 6] = (byte) (blockIDCount.count() >> 8);
            chunkBytes[i * 8 + 7] = (byte) blockIDCount.count();
        }

        // Step 4: Convert palette to bytes
        List<byte[]> paletteBytesList = new ArrayList<>();
        int paletteLength = 0;
        for (ServerBlock block : palette) {
            byte[] blockBytes = block.getBlockBytes();
            byte[] copy = new byte[blockBytes.length];
            System.arraycopy(blockBytes, 0, copy, 0, blockBytes.length);
            paletteBytesList.add(copy);
            paletteLength += copy.length;
        }

        byte[] paletteBytes = new byte[2 + paletteLength];
        paletteBytes[0] = (byte) (palette.size() >> 8);
        paletteBytes[1] = (byte) palette.size();
        int paletteIndex = 2;
        for (byte[] pBytes : paletteBytesList) {
            System.arraycopy(pBytes, 0, paletteBytes, paletteIndex, pBytes.length);
            paletteIndex += pBytes.length;
        }

        // Step 5: Combine palette + chunk bytes
        byte[] out = new byte[paletteBytes.length + chunkBytes.length];
        System.arraycopy(paletteBytes, 0, out, 0, paletteBytes.length);
        System.arraycopy(chunkBytes, 0, out, paletteBytes.length, chunkBytes.length);

        return out;
    }

    public void tick() throws IOException {
        for (Map.Entry<Position3D, ChunkValue> entry : chunks.entrySet()) {
            checkForOldChunks(entry.getKey(), entry.getValue());
        }

        request++;
    }

    private void checkForOldChunks(Position3D position3D, ChunkValue chunkValue) throws IOException {
        if (chunkValue.shouldSave(this.request)) {
            chunks.remove(position3D);
            chunkInfo.remove(position3D);
        }
    }

    public void add(Position3D position3D, Chunk<ServerBlock> chunk) {
        this.chunks.put(position3D, new ChunkValue(chunk, request));
    }

    public void addChunkInfo(Position3D position3D, ChunkInfo chunkInfo) {
        this.chunkInfo.put(position3D, chunkInfo);
    }

    public Chunk<ServerBlock> get(Position3D position3D) {
        ChunkValue chunkValue = chunks.get(position3D);
        return chunkValue == null ? null : chunkValue.get(request);
    }

    public byte[] getBytes(Position3D position3D) throws IOException {
        Path path = Path.of(ConstantServerSettings.CHUNK_SAVE_LOCATION + position3D.getPath());
        return Files.exists(path) ? Files.readAllBytes(Path.of(ConstantServerSettings.CHUNK_SAVE_LOCATION + position3D.getPath())) : null;
    }

    public ChunkInfo getChunkInfo(Position3D position3D) {
        return chunkInfo.get(position3D);
    }

    public ServerBlock getBlock(Position3D chunkPosition, int x, int y, int z) {
        final int CW = ConstantGameSettings.CHUNK_WIDTH;
        final int CH = ConstantGameSettings.CHUNK_HEIGHT;
        final int CL = ConstantGameSettings.CHUNK_LENGTH;

        int dx = Math.floorDiv(x, CW);
        int dy = Math.floorDiv(y, CH);
        int dz = Math.floorDiv(z, CL);

        Position3D neighborChunk = chunkPosition.add(dx, dy, dz);
        Chunk<ServerBlock> chunk = get(neighborChunk);
        if (chunk == null) return null;

        int lx = Math.floorMod(x, CW);
        int ly = Math.floorMod(y, CH);
        int lz = Math.floorMod(z, CL);

        return chunk.getBlock(lx, ly, lz);
    }

    private static class ChunkValue {
        private final Chunk<ServerBlock> chunk;
        private int request;

        public ChunkValue(Chunk<ServerBlock> chunk, int request) {
            this.chunk = chunk;
            this.request = request;
        }

        public boolean shouldSave(int request) {
            return request - this.request > ConstantServerSettings.CHUNK_TIME_LIMIT;
        }

        public Chunk<ServerBlock> get(int request) {
            this.request = request;
            return chunk;
        }
    }
}