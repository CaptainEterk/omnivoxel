package omnivoxel.server.client.block;

import omnivoxel.server.client.structure.Structure;

public record StructureSeed(Structure structure) implements Block {
    @Override
    public byte[] getBytes() {
        return structure.getBlocks()[0][0][0].getBytes();
    }
}