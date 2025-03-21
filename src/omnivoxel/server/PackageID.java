package omnivoxel.server;

public enum PackageID {
    // TCP

    // CLIENT -> SERVER

    REGISTER_CLIENT,
    CHUNK_REQUEST,
    CLOSE,

    // SERVER -> CLIENT

    NEW_PLAYER,
    REGISTER_PLAYERS,
    CHUNK,
    NEW_ENTITY,


    // UDP

    // Client -> Server
    PLAYER_UPDATE,


}