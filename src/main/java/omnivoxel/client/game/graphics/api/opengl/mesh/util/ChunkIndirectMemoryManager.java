package omnivoxel.client.game.graphics.api.opengl.mesh.util;

import java.util.Map;
import java.util.TreeMap;

public class ChunkIndirectMemoryManager {
    private final MemorySegment vertexArena = new MemorySegment();
    private final MemorySegment indexArena = new MemorySegment();

    public void init(long vertexCapacity, long indexCapacity) {
        vertexArena.init(vertexCapacity);
        indexArena.init(indexCapacity);
    }

    public long usedVertexBytes() {
        return vertexArena.used();
    }

    public long usedIndexBytes() {
        return indexArena.used();
    }

    public ChunkMeshAllocation allocate(
            int vertexSize,
            int indexSize
    ) {
        if (vertexSize <= 0 || indexSize <= 0) {
            throw new IllegalArgumentException(
                    "Allocation sizes must be greater than zero"
            );
        }

        long vertexOffset = vertexArena.allocate(vertexSize);

        try {
            long indexOffset = indexArena.allocate(indexSize);

            return new ChunkMeshAllocation(
                    vertexOffset,
                    vertexSize,
                    indexOffset,
                    indexSize
            );
        } catch (RuntimeException exception) {
            vertexArena.free(vertexOffset, vertexSize);
            throw exception;
        }
    }

    public void free(ChunkMeshAllocation allocation) {
        if (allocation == null) {
            return;
        }

        if (allocation.vertexSize() > 0) {
            vertexArena.free(
                    allocation.vertexOffset(),
                    allocation.vertexSize()
            );
        }

        if (allocation.indexSize() > 0) {
            indexArena.free(
                    allocation.indexOffset(),
                    allocation.indexSize()
            );
        }
    }

    public long remainingVertexBytes() {
        return vertexArena.remaining();
    }

    public long remainingIndexBytes() {
        return indexArena.remaining();
    }

    public void reset() {
        vertexArena.reset();
        indexArena.reset();
    }

    private static final class MemorySegment {

        private final TreeMap<Long, Long> freeBlocks =
                new TreeMap<>();
        /*
         * Tracks every currently allocated block.
         *
         * Key   = offset
         * Value = size
         */
        private final TreeMap<Long, Long> allocatedBlocks =
                new TreeMap<>();
        private long capacity;
        private long remaining;

        public synchronized long used() {
            return capacity - remaining;
        }

        public synchronized void init(long capacity) {
            if (capacity <= 0) {
                throw new IllegalArgumentException(
                        "Capacity must be greater than zero"
                );
            }

            this.capacity = capacity;
            reset();
        }

        public synchronized long allocate(long size) {
            if (size <= 0) {
                throw new IllegalArgumentException(
                        "Allocation size must be greater than zero"
                );
            }

            Map.Entry<Long, Long> selected = null;

            for (Map.Entry<Long, Long> entry : freeBlocks.entrySet()) {
                if (entry.getValue() >= size) {
                    selected = entry;
                    break;
                }
            }

            if (selected == null) {
                throw new IllegalStateException(
                        "Memory arena is full: requested=" + size +
                                ", remaining=" + remaining +
                                ", capacity=" + capacity
                );
            }

            long offset = selected.getKey();
            long blockSize = selected.getValue();

            freeBlocks.remove(offset);

            long remainingSize = blockSize - size;

            if (remainingSize > 0) {
                freeBlocks.put(
                        offset + size,
                        remainingSize
                );
            }

            Long previous = allocatedBlocks.put(offset, size);

            if (previous != null) {
                throw new IllegalStateException(
                        "Allocator corruption: allocation already exists at " +
                                offset
                );
            }

            remaining -= size;

            return offset;
        }

        public synchronized void free(long offset, long size) {
            if (size <= 0) {
                return;
            }

            Long allocatedSize = allocatedBlocks.remove(offset);

            if (allocatedSize == null) {
                throw new IllegalStateException(
                        "Attempted to free an allocation that is not active: " +
                                "offset=" + offset +
                                ", size=" + size
                );
            }

            if (allocatedSize != size) {
                allocatedBlocks.put(offset, allocatedSize);

                throw new IllegalStateException(
                        "Allocation size mismatch: offset=" + offset +
                                ", expected=" + allocatedSize +
                                ", got=" + size
                );
            }

            long newOffset = offset;
            long newSize = size;

            Map.Entry<Long, Long> previous =
                    freeBlocks.floorEntry(offset);

            if (previous != null) {
                long previousOffset = previous.getKey();
                long previousSize = previous.getValue();

                if (previousOffset + previousSize == newOffset) {
                    newOffset = previousOffset;
                    newSize += previousSize;

                    freeBlocks.remove(previousOffset);
                }
            }

            Map.Entry<Long, Long> next =
                    freeBlocks.ceilingEntry(newOffset);

            if (next != null) {
                long nextOffset = next.getKey();
                long nextSize = next.getValue();

                if (newOffset + newSize == nextOffset) {
                    newSize += nextSize;

                    freeBlocks.remove(nextOffset);
                }
            }

            freeBlocks.put(newOffset, newSize);

            remaining += size;
        }

        public synchronized long remaining() {
            return remaining;
        }

        public synchronized void reset() {
            freeBlocks.clear();
            allocatedBlocks.clear();

            freeBlocks.put(0L, capacity);
            remaining = capacity;
        }
    }
}