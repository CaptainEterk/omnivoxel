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
    SERVER_INFO,
    REGISTER_BLOCK,
    REGISTER_BLOCK_SHAPE,

    // TODO: Implement UDP Client/Server (using TCP for now)
    // UDP

    // Client -> Server
    PLAYER_UPDATE,

    // Server -> Client
    ENTITY_UPDATE,
}