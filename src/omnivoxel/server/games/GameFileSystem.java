package omnivoxel.server.games;

import org.graalvm.polyglot.io.FileSystem;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.Set;

public class GameFileSystem implements FileSystem {
    private final Path root;
    private final Path graalLib;
    private final Path mainJar; // discovered at construction, may be null if unavailable

    public GameFileSystem(Path root) {
        this.root = root.toAbsolutePath().normalize();
        this.graalLib = Path.of("./lib/graalvm").toAbsolutePath().normalize();
        this.mainJar = discoverMainJar();
    }

    /**
     * Very strict resolver:
     * - Allows paths under 'root'
     * - Allows paths under './lib/graalvm'
     * - Allows exactly the main JAR file (for read-only use by JVM/Graal internals)
     * Everything else is forbidden.
     */
    private Path resolve(Path path) throws IOException {
        Path candidate = path;
        if (!candidate.isAbsolute()) {
            candidate = Path.of(".").toAbsolutePath().normalize().resolve(candidate);
        }

        candidate = candidate.normalize();

        if (candidate.startsWith(root)) {
            return candidate;
        }
        if (candidate.startsWith(graalLib)) {
            return candidate;
        }

        if (mainJar != null) {
            if (candidate.equals(mainJar)) {
                return candidate;
            }

            if (candidate.equals(mainJar.getParent())) {
                return candidate;
            }
        }

        throw new SecurityException("Access outside the sandbox is not allowed: " + path);
    }

    private Path discoverMainJar() {
        try {
            URI uri = GameFileSystem.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();
            Path p = Path.of(uri).toAbsolutePath().normalize();

            if (Files.isDirectory(p)) return null;

            return p;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Path parsePath(URI uri) {
        return parsePath(uri.getPath());
    }

    @Override
    public Path parsePath(String path) {
        try {
            return resolve(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Enforce access modes: if the target is the main JAR, disallow write operations.
     */
    @Override
    public void checkAccess(Path path, Set<? extends AccessMode> modes, LinkOption... linkOptions) throws IOException {
        Path resolved = resolve(path);

        if (isMainJar(resolved)) {
            // If any requested mode is WRITE, deny it.
            for (AccessMode m : modes) {
                if (m == AccessMode.WRITE) {
                    throw new SecurityException("Write access to the main JAR is forbidden.");
                }
                if (m == AccessMode.EXECUTE) {
                    // Disallow execute on the jar path explicitly (defense-in-depth)
                    throw new SecurityException("Execute access to the main JAR is forbidden.");
                }
            }
        }

        // Otherwise delegate to the platform provider for existence/readonly checks.
        resolved.getFileSystem().provider().checkAccess(resolved, modes.toArray(new AccessMode[0]));
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) {
        throw new UnsupportedOperationException("Mods cannot create directories.");
    }

    @Override
    public void delete(Path path) {
        throw new UnsupportedOperationException("Mods cannot delete files.");
    }

    /**
     * Enforce read-only semantics for the main JAR when opening channels.
     * Any attempt to open mainJar with WRITE/CREATE/TRUNCATE/APPEND is rejected.
     */
    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        Path resolved = resolve(path);

        if (isMainJar(resolved)) {
            for (OpenOption opt : options) {
                if (opt == StandardOpenOption.WRITE
                        || opt == StandardOpenOption.CREATE
                        || opt == StandardOpenOption.CREATE_NEW
                        || opt == StandardOpenOption.APPEND
                        || opt == StandardOpenOption.TRUNCATE_EXISTING) {
                    throw new SecurityException("Mods cannot open the main JAR for writing or creation.");
                }
            }
        }

        return Files.newByteChannel(resolved, options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        Path resolved = resolve(dir);

        if (mainJar != null && resolved.equals(mainJar.getParent()) && !resolved.startsWith(root) && !resolved.startsWith(graalLib)) {
            throw new SecurityException("Listing the main JAR's parent directory is forbidden.");
        }

        return Files.newDirectoryStream(resolved, filter);
    }

    @Override
    public Path toAbsolutePath(Path path) {
        try {
            return resolve(path);
        } catch (IOException e) {
            throw new RuntimeException("Invalid path: " + path, e);
        }
    }

    @Override
    public Path toRealPath(Path path, LinkOption... linkOptions) throws IOException {
        return resolve(path).toRealPath(linkOptions);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        return Files.readAttributes(resolve(path), attributes, options);
    }

    // Symbolic links denied
    @Override
    public void createLink(Path link, Path existing) {
        throw new UnsupportedOperationException("Symbolic links are not supported.");
    }

    @Override
    public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) {
        throw new UnsupportedOperationException("Symbolic links are not supported.");
    }

    @Override
    public Path readSymbolicLink(Path link) {
        throw new UnsupportedOperationException("Symbolic links are not supported.");
    }

    @Override
    public void setCurrentWorkingDirectory(Path currentWorkingDirectory) {
        throw new UnsupportedOperationException("Changing working directory is not supported.");
    }

    @Override
    public String getSeparator() {
        return FileSystems.getDefault().getSeparator();
    }

    @Override
    public String getPathSeparator() {
        return File.pathSeparator;
    }

    @Override
    public String getMimeType(Path path) {
        return null; // Let GraalVM guess based on extension
    }

    @Override
    public Charset getEncoding(Path path) {
        return Charset.defaultCharset(); // Default encoding
    }

    @Override
    public Path getTempDirectory() {
        throw new UnsupportedOperationException("Temporary directories not supported.");
    }

    @Override
    public boolean isSameFile(Path path1, Path path2, LinkOption... options) throws IOException {
        return resolve(path1).equals(resolve(path2));
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) {
        throw new UnsupportedOperationException("Mods cannot copy files.");
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) {
        throw new UnsupportedOperationException("Mods cannot move files.");
    }

    private boolean isMainJar(Path p) {
        return p != null && p.equals(mainJar);
    }
}