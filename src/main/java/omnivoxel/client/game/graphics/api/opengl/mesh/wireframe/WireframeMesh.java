package omnivoxel.client.game.graphics.api.opengl.mesh.wireframe;

import omnivoxel.client.game.graphics.api.opengl.OpenGLChecks;
import omnivoxel.client.game.graphics.api.opengl.mesh.Mesh;
import omnivoxel.common.block.shape.BlockShape;
import omnivoxel.common.block.shape.BlockVertex;
import omnivoxel.common.face.BlockFace;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.util.*;

public record WireframeMesh(int vao, int vbo, int ebo, int indexCount) implements Mesh {
    public static WireframeMesh create(BlockShape blockShape) {
        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Map<BlockVertex, Integer> vertexIndices = new HashMap<>();
        Set<Long> edges = new HashSet<>();

        for (BlockFace face : BlockFace.values()) {
            if (face == BlockFace.NONE) {
                continue;
            }

            BlockVertex[] faceVertices = blockShape.vertices()[face.ordinal()];
            for (int i = 0; i < faceVertices.length; i++) {
                int a = getWireframeVertexIndex(faceVertices[i], vertices, vertexIndices);
                int b = getWireframeVertexIndex(faceVertices[(i + 1) % faceVertices.length], vertices, vertexIndices);
                addWireframeEdge(a, b, edges, indices);
            }
        }

        if (vertices.isEmpty() || indices.isEmpty()) {
            return new WireframeMesh(0, 0, 0, 0);
        }

        int vao1 = GL30C.glGenVertexArrays();
        GL30C.glBindVertexArray(vao1);

        int vbo1 = GL15C.glGenBuffers();
        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vbo1);
        GL15C.glBufferData(GL15C.GL_ARRAY_BUFFER, toFloatArray(vertices), GL15C.GL_STATIC_DRAW);

        int ebo1 = GL15C.glGenBuffers();
        GL15C.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, ebo1);
        GL15C.glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, toIntArray(indices), GL15C.GL_STATIC_DRAW);

        GL20C.glEnableVertexAttribArray(3);
        GL20C.glVertexAttribPointer(3, 3, GL11C.GL_FLOAT, false, 3 * Float.BYTES, 0L);

        GL30C.glBindVertexArray(0);
        GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
        GL15C.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, 0);

        OpenGLChecks.checkError("bufferize block highlight wireframe");
        return new WireframeMesh(vao1, vbo1, ebo1, indices.size());
    }

    private static int getWireframeVertexIndex(BlockVertex vertex, List<Float> vertices, Map<BlockVertex, Integer> vertexIndices) {
        Integer index = vertexIndices.get(vertex);
        if (index != null) {
            return index;
        }

        int newIndex = vertexIndices.size();
        vertexIndices.put(vertex, newIndex);
        vertices.add(vertex.px());
        vertices.add(vertex.py());
        vertices.add(vertex.pz());
        return newIndex;
    }

    private static void addWireframeEdge(int a, int b, Set<Long> edges, List<Integer> indices) {
        int min = Math.min(a, b);
        int max = Math.max(a, b);
        long edge = ((long) min << 32) | (max & 0xFFFFFFFFL);
        if (edges.add(edge)) {
            indices.add(min);
            indices.add(max);
        }
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] out = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            out[i] = list.get(i);
        }
        return out;
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] out = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            out[i] = list.get(i);
        }
        return out;
    }

    public void cleanup() {
        if (vao > 0) {
            GL30C.glDeleteVertexArrays(vao);
        }
        if (vbo > 0) {
            GL15C.glDeleteBuffers(vbo);
        }
        if (ebo > 0) {
            GL15C.glDeleteBuffers(ebo);
        }
    }
}