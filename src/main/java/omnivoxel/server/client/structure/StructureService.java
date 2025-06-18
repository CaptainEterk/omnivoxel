package omnivoxel.server.client.structure;

import core.biomes.PlainsBiome;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.biomeService.climate.ClimateVector;

import java.util.ArrayList;
import java.util.List;

public class StructureService {
    private final List<Structure> structures;

    public StructureService() {
        structures = new ArrayList<>();
    }

    public void register(Structure structure) {
        structures.add(structure);
    }

    public StructureSeed getStructure(Biome biome, int x, int y, int z, ClimateVector climateVector2D, ClimateVector climateVector3D) {
//        if (biome instanceof TundraBiome && x % ConstantGameSettings.CHUNK_WIDTH == 0 && z % ConstantGameSettings.CHUNK_LENGTH == 0 && (climateVector3D.get(0) >= y && climateVector3D.get(0) < y + 10)) {
//            return new StructureSeed(structures.getFirst(), new Position3D(0, (int) (climateVector3D.get(0) - y), 0));
//        } else {
//            return null;
//        }

        int yOffset = (int) (climateVector2D.get(0) - y);
        if (biome instanceof PlainsBiome &&
                Math.floorMod(x, ConstantGameSettings.CHUNK_WIDTH) == 0 &&
                Math.floorMod(z, ConstantGameSettings.CHUNK_LENGTH) == 0 &&
                yOffset == 0
        ) {
            return new StructureSeed(structures.getFirst(), null);
        }
        return null;
    }
}