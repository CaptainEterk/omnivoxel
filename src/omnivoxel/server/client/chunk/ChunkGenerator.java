package omnivoxel.server.client.chunk;

import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.world.chunk.ByteChunk;
import omnivoxel.server.world.chunk.padded.GeneratedChunk;

public class ChunkGenerator {
    private final ServerWorldDataService worldDataService;

    public ChunkGenerator(ServerWorldDataService worldDataService) {
        this.worldDataService = worldDataService;
    }

    public ByteChunk generateChunk(ChunkPosition chunkPosition) {
        GeneratedChunk chunk = new GeneratedChunk();
        for (int x = -1; x < ConstantGameSettings.CHUNK_WIDTH + 1; x++) {
            for (int z = -1; z < ConstantGameSettings.CHUNK_LENGTH + 1; z++) {
                for (int y = -1; y < ConstantGameSettings.CHUNK_HEIGHT + 1; y++) {
                    chunk.setBlock(x, y, z, worldDataService.getBlockAt(chunkPosition, x, y, z));
                }
            }
        }
        return chunk.getByteChunk();
//        List<Block> palette = new ArrayList<>();
//        int[] chunk = new int[ConstantGameSettings.BLOCKS_IN_CHUNK_PADDED * 2];
//        int chunkByteOffset = 0;
//        for (int x = -1; x < ConstantGameSettings.CHUNK_WIDTH + 1; x++) {
//            for (int z = -1; z < ConstantGameSettings.CHUNK_LENGTH + 1; z++) {
//                for (int y = -1; y < ConstantGameSettings.CHUNK_HEIGHT + 1; y++) {
//                    Block block = worldDataService.getBlockAt(chunkPosition, x, y, z);
//                    if (!palette.contains(block)) {
//                        palette.add(block);
//                    }
//                    chunk[chunkByteOffset] = palette.indexOf(block) + 1;
//                    chunkByteOffset++;
//                }
//            }
//        }

//        List<BlockIDCount> chunkData = new ArrayList<>();
//        int count = 0;
//        int currentID = 0;
//        for (int i = 0; i < chunk.length; i++) {
//            int id = chunk[i];
//            if (i == 0 || currentID != id) {
//                if (i > 0) {
//                    chunkData.add(new BlockIDCount(currentID, count));
//                }
//                currentID = id;
//                count = 0;
//            }
//            count++;
//        }
//
//        byte[] chunkBytes = new byte[chunkData.size() * 8];
//        for (int i = 0; i < chunkData.size(); i++) {
//            BlockIDCount blockIDCount = chunkData.get(i);
//            chunkBytes[i * 8] = (byte) (blockIDCount.blockID() >> 24);
//            chunkBytes[i * 8 + 1] = (byte) (blockIDCount.blockID() >> 16);
//            chunkBytes[i * 8 + 2] = (byte) (blockIDCount.blockID() >> 8);
//            chunkBytes[i * 8 + 3] = (byte) (blockIDCount.blockID());
//            chunkBytes[i * 8 + 4] = (byte) (blockIDCount.count() >> 24);
//            chunkBytes[i * 8 + 5] = (byte) (blockIDCount.count() >> 16);
//            chunkBytes[i * 8 + 6] = (byte) (blockIDCount.count() >> 8);
//            chunkBytes[i * 8 + 7] = (byte) (blockIDCount.count());
//        }
//
//        // TODO: Compress this into one loop
//        List<byte[]> paletteBytesList = new ArrayList<>();
//        int paletteLength = 0;
//        for (Block block : palette) {
//            byte[] blockBytes = block.getBytes();
//            byte[] bytes = new byte[blockBytes.length];
//            System.arraycopy(blockBytes, 0, bytes, 0, blockBytes.length);
//            paletteBytesList.add(bytes);
//            paletteLength += bytes.length;
//        }
//
//        byte[] paletteBytes = new byte[2 + paletteLength];
//        paletteBytes[0] = (byte) (palette.size() >> 8);
//        paletteBytes[1] = (byte) palette.size();
//        int paletteIndex = 2;
//        for (byte[] paletteBites : paletteBytesList) {
//            System.arraycopy(paletteBites, 0, paletteBytes, paletteIndex, paletteBites.length);
//            paletteIndex += paletteBites.length;
//        }
//
//        byte[] out = new byte[chunkBytes.length + paletteBytes.length];
//        System.arraycopy(paletteBytes, 0, out, 0, paletteBytes.length);
//        System.arraycopy(chunkBytes, 0, out, paletteBytes.length, chunkBytes.length);
//
//        return out;
    }
}