package omnivoxel.server.games;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GameFinder {
    private final File gamesDirectory;

    public GameFinder(File gamesDirectory) {
        this.gamesDirectory = gamesDirectory;
    }

    public List<String> findGames() {
        List<String> results = new ArrayList<>();
        File[] entries = gamesDirectory.listFiles(File::isDirectory);
        if (entries == null) return results;

        for (File dir : entries) {
            File gameScript = new File(dir, "game.properties");
            if (!gameScript.exists()) continue;

            results.add(dir.getAbsolutePath());
        }

        return results;
    }
}