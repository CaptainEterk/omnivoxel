package omnivoxel.world.chunk;

import omnivoxel.client.game.settings.ConstantGameSettings;

import java.util.Objects;

public class BiBlockChunk<B> implements Chunk<B> {
    private final int[] blocks;
    private final B air;
    private B block = null;

    public BiBlockChunk(B block) {
        this.air = block;
        blocks = new int[ConstantGameSettings.CHUNK_WIDTH * ConstantGameSettings.CHUNK_LENGTH];
    }

    @Override
    public B getBlock(int x, int y, int z) {
        return (blocks[z * ConstantGameSettings.CHUNK_WIDTH + x] & (1 << y)) != 0 ? this.block : air;
    }

    @Override
    public Chunk<B> setBlock(int x, int y, int z, B block) {
        if (Objects.equals(this.air, block)) {
            blocks[z * ConstantGameSettings.CHUNK_WIDTH + x] &= ~(1 << y);
        } else if (this.block == null) {
            this.block = block;
            blocks[z * ConstantGameSettings.CHUNK_WIDTH + x] |= (1 << y);
        } else if (Objects.equals(this.block, block)) {
            blocks[z * ConstantGameSettings.CHUNK_WIDTH + x] |= (1 << y);
        } else {
            return new ModifiedChunk<>(x, y, z, block, this);
        }
        return this;
    }
}