package omnivoxel.server;

public enum PackageID {
    // CLIENT -> SERVER

    REGISTER_CLIENT,
    SET_PLAYER,
    CHUNK_REQUEST,
    CLOSE,

    // SERVER -> CLIENT

    NEW_PLAYER,
    REGISTER_PLAYERS,
    CHUNK_RESPONSE,
    UPDATE_PLAYER,
}