package omnivoxel.chunk;

import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.world.chunk.ChunkShell;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ChunkShellMergeTest {

    @DataProvider(name = "lods")
    public Object[][] lods() {
        return new Object[][]{
                {0, 0},
                {0, 1},
                {0, 2},
                {1, 1},
                {1, 2},
                {2, 2}
        };
    }

    @Test(dataProvider = "lods")
    public void testMerge(int lodA, int lodB) {

        ChunkShell<Integer> a = new ChunkShell<>(lodA);
        ChunkShell<Integer> b = new ChunkShell<>(lodB);

        fill(a);
        fill(b);

        ChunkShell<Integer> result =
                lodA > lodB ? a.merge(b) : b.merge(a);

        int size = ConstantCommonSettings.CHUNK_WIDTH >> result.getLOD();

        // X faces
        for (int z = 0; z < size; z++) {
            for (int y = 0; y < size; y++) {
                Assert.assertNotNull(result.getBlock(0, y, z));
                Assert.assertNotNull(result.getBlock(size - 1, y, z));
            }
        }

        // Y faces
        for (int z = 0; z < size; z++) {
            for (int x = 0; x < size; x++) {
                Assert.assertNotNull(result.getBlock(x, 0, z));
                Assert.assertNotNull(result.getBlock(x, size - 1, z));
            }
        }

        // Z faces
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Assert.assertNotNull(result.getBlock(x, y, 0));
                Assert.assertNotNull(result.getBlock(x, y, size - 1));
            }
        }
    }

    private void fill(ChunkShell<Integer> shell) {
        int size = ConstantCommonSettings.CHUNK_WIDTH >> shell.getLOD();

        // X faces
        for (int z = 0; z < size; z++) {
            for (int y = 0; y < size; y++) {
                shell.setBlock(0, y, z, 1);
                shell.setBlock(size - 1, y, z, 2);
            }
        }

        // Y faces
        for (int z = 0; z < size; z++) {
            for (int x = 0; x < size; x++) {
                shell.setBlock(x, 0, z, 3);
                shell.setBlock(x, size - 1, z, 4);
            }
        }

        // Z faces
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                shell.setBlock(x, y, 0, 5);
                shell.setBlock(x, y, size - 1, 6);
            }
        }
    }

    @Test
    public void testLOD1ToLOD2Downsampling() {
        ChunkShell<Integer> lod1 = new ChunkShell<>(1);
        ChunkShell<Integer> lod2 = new ChunkShell<>(2);

        int fineSize = ConstantCommonSettings.CHUNK_WIDTH >> 1;   // 16
        int coarseSize = ConstantCommonSettings.CHUNK_WIDTH >> 2; // 8

        // Fill only the +Z face with unique values.
        for (int y = 0; y < fineSize; y++) {
            for (int x = 0; x < fineSize; x++) {
                lod1.setBlock(x, y, fineSize - 1, y * fineSize + x);
                System.out.println("Setting " + x + " " + y + " to " + (y * fineSize + x));
            }
        }

        ChunkShell<Integer> merged = lod2.merge(lod1);

        for (int y = 0; y < coarseSize; y++) {
            for (int x = 0; x < coarseSize; x++) {
                System.out.println("Block at " + x + " " + y + " " + merged.getBlock(x, y, coarseSize - 1));
            }
        }

        Assert.assertSame(merged, lod2);

        for (int y = 0; y < coarseSize; y++) {
            for (int x = 0; x < coarseSize; x++) {

                int expected = (y * 2) * fineSize + (x * 2);
                Integer actual = merged.getBlock(x, y, coarseSize - 1);

                Assert.assertEquals(
                        actual,
                        Integer.valueOf(expected),
                        String.format(
                                "Incorrect sample at coarse (%d,%d): expected %d, got %s",
                                x, y, expected, actual
                        )
                );
            }
        }
    }
}