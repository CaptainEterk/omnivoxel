package omnivoxel.client.game.graphics.chunk;

import omnivoxel.client.game.graphics.camera.Camera;
import omnivoxel.client.game.position.DistanceChunk;
import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.util.math.Position3D;
import omnivoxel.util.thread.CoalescingWorkerThread;

import java.util.ArrayList;
import java.util.List;

public final class RenderedChunkProvider {
    private final CoalescingWorkerThread<RenderingData> coalescingWorkerThread;
    private volatile List<DistanceChunk> output = List.of();

    public RenderedChunkProvider() {
        coalescingWorkerThread = new CoalescingWorkerThread<>(this::calculateRenderedChunks, true);
    }

    public void update(int frustumBias, int renderDistance, Camera camera) {
        coalescingWorkerThread.updateInput(new RenderingData(
                frustumBias,
                renderDistance,
                camera
        ));
    }

    public List<DistanceChunk> getOutput() {
        return output;
    }

    @SuppressWarnings("unchecked")
    private void calculateRenderedChunks(RenderingData renderingData) {
        Camera camera = renderingData.camera();

        int frustumBias = renderingData.frustumBias();
        int renderDistance = renderingData.renderDistance();

        int chunkX = Math.round((float) renderDistance / ConstantCommonSettings.CHUNK_WIDTH) + 1;
        int chunkY = Math.round((float) renderDistance / ConstantCommonSettings.CHUNK_HEIGHT) + 1;
        int chunkZ = Math.round((float) renderDistance / ConstantCommonSettings.CHUNK_LENGTH) + 1;

        int rdChunks = renderDistance / ConstantCommonSettings.CHUNK_SIZE + 1;
        int squaredRenderDistance = rdChunks * rdChunks;

        int maxDistance = chunkX * chunkX + chunkY * chunkY + chunkZ * chunkZ;

        ArrayList<DistanceChunk>[] buckets = (ArrayList<DistanceChunk>[]) new ArrayList[maxDistance * frustumBias + 1];

        int ccx = (int) -Math.floor(camera.getX() / ConstantCommonSettings.CHUNK_WIDTH);
        int ccy = (int) -Math.floor(camera.getY() / ConstantCommonSettings.CHUNK_HEIGHT);
        int ccz = (int) -Math.floor(camera.getZ() / ConstantCommonSettings.CHUNK_LENGTH);

        int count = 0;

        var frustum = camera.getFrustum();

        for (int x = -chunkX; x <= chunkX; x++) {
            for (int y = -chunkY; y <= chunkY; y++) {
                for (int z = -chunkZ; z <= chunkZ; z++) {

                    int distance = x * x + y * y + z * z;

                    if (distance >= squaredRenderDistance) {
                        continue;
                    }

                    Position3D position = new Position3D(
                            x - ccx,
                            y - ccy,
                            z - ccz
                    );

                    int sort = distance;

                    if (!frustum.isChunkInFrustum(position)) {
                        sort *= frustumBias;
                    }

                    ArrayList<DistanceChunk> bucket = buckets[sort];
                    if (bucket == null) {
                        bucket = new ArrayList<>();
                        buckets[sort] = bucket;
                    }

                    bucket.add(new DistanceChunk(distance, sort, position));
                    count++;
                }
            }
        }

        ArrayList<DistanceChunk> out = new ArrayList<>(count);

        for (ArrayList<DistanceChunk> bucket : buckets) {
            if (bucket != null) {
                out.addAll(bucket);
            }
        }

        output = out;
    }

    private record RenderingData(
            int frustumBias,
            int renderDistance,
            Camera camera
    ) {
    }
}