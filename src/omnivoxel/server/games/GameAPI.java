package omnivoxel.server.games;

import org.graalvm.polyglot.HostAccess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GameAPI {
    public static String GAME_DIRECTORY = null;

    @HostAccess.Export
    public String readFile(String path) throws IOException {
        return Files.readString(Path.of(GAME_DIRECTORY, path));
    }
}