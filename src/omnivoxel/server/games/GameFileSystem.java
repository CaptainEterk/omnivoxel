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

    public GameFileSystem(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    private Path resolve(Path path) throws IOException {
        Path resolved = root.resolve(path).normalize();
        if (!resolved.startsWith(root)) {
            throw new SecurityException("Access outside the sandbox is not allowed: " + path);
        }
        return resolved;
    }

    @Override
    public Path parsePath(URI uri) {
        return Paths.get(uri.getPath());
    }

    @Override
    public Path parsePath(String path) {
        return Paths.get(path);
    }

    @Override
    public void checkAccess(Path path, Set<? extends AccessMode> modes, LinkOption... linkOptions) throws IOException {
        Path resolved = resolve(path);
        resolved.getFileSystem().provider().checkAccess(resolved, modes.toArray(new AccessMode[0]));
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        Files.createDirectory(resolve(dir), attrs);
    }

    @Override
    public void delete(Path path) throws IOException {
        Files.delete(resolve(path));
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        return Files.newByteChannel(resolve(path), options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        return Files.newDirectoryStream(resolve(dir), filter);
    }

    @Override
    public Path toAbsolutePath(Path path) {
        return root.resolve(path).normalize();
    }

    @Override
    public Path toRealPath(Path path, LinkOption... linkOptions) throws IOException {
        return resolve(path).toRealPath(linkOptions);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        return Files.readAttributes(resolve(path), attributes, options);
    }

    // Optional: deny symbolic links
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

    // Optional: basic copy/move
    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        Files.copy(resolve(source), resolve(target), options);
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        Files.move(resolve(source), resolve(target), options);
    }
}