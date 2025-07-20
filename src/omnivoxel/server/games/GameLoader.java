package omnivoxel.server.games;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.File;

public class GameLoader {
    private final String targetGameId;

    public GameLoader(String targetGameId) {
        this.targetGameId = targetGameId;
    }

    public void loadMods(File modsDir) {
        File[] files = modsDir.listFiles((dir, name) -> name.endsWith(".js"));
        if (files == null) return;

        for (File file : files) {
            try {
                loadModFile(file);
            } catch (Exception e) {
                System.err.println("Failed to load mod: " + file.getName());
                e.printStackTrace();
            }
        }
    }

    private void loadModFile(File jsFile) throws Exception {
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(false)
                .build()) {

            Value mod = context.eval(Source.newBuilder("js", jsFile).build());

            String modName = mod.getMember("name").asString();
            String modGameId = mod.getMember("game").asString();

            if (!modGameId.equals(targetGameId)) {
                System.out.println("Skipping mod: " + modName + " (for game: " + modGameId + ")");
                return;
            }

            System.out.println("Initializing JS mod: " + modName);
            mod.getMember("init").execute();
        }
    }
}