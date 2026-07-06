package omnivoxel.server;

public enum PackageID {
    // TCP

    // CLIENT -> SERVER

    VERSION_HANDSHAKE,
    CHUNK_REQUEST,
    CLOSE,

    // SERVER -> CLIENT

    CHUNK,
    NEW_ENTITY,
    SERVER_INFO,
    REGISTER_BLOCK,
    REGISTER_BLOCK_SHAPE,
    REGISTER_BLOCK_HITBOX,
    REGISTER_GAME_RESOURCES,
    REPLACE_BLOCK,
    HEIGHTS,
    PLAYER_STATE,

    // TODO: Implement UDP Client/Server (using TCP for now)
    // UDP

    // Client -> Server
    PLAYER_UPDATE,

    // Server -> Client
    ENTITY_UPDATE
}