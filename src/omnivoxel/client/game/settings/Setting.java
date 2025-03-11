package omnivoxel.client.game.settings;

public record Setting(String key, String value) {
    @Override
    public String toString() {
        return "{" + key + "=" + value + "}";
    }
}