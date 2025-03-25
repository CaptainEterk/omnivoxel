package omnivoxel.client.game.shader;

import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL40C;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20C.glGetUniformLocation;
import static org.lwjgl.opengl.GL20C.glUseProgram;

public class ShaderProgram {
    private final Map<String, Integer> locationMap;
    private final int program;

    public ShaderProgram(int program) {
        this.program = program;
        this.locationMap = new HashMap<>();
    }

    public int addUniformLocation(String name) {
        int loc = glGetUniformLocation(program, name);
        locationMap.put(name, loc);
        return loc;
    }

    public Integer getLocation(String location) {
        return locationMap.getOrDefault(location, addUniformLocation(location));
    }

    public void setUniform(String name, int x, int y, int z) {
        GL30C.glUniform3i(getLocation(name), x, y, z);
    }

    public void setUniform(String name, float x, float y, float z) {
        GL30C.glUniform3f(getLocation(name), x, y, z);
    }

    public void setUniform(String name, Vector3fc vector) {
        GL30C.glUniform3f(getLocation(name), vector.x(), vector.y(), vector.z());
    }

    public void setUniform(String name, float v) {
        GL30C.glUniform1f(getLocation(name), v);
    }

    public void setUniform(String name, double v) {
        GL40C.glUniform1d(getLocation(name), v);
    }

    public void setUniform(String name, int v) {
        GL30C.glUniform1ui(getLocation(name), v);
    }

    public void setUniform(String name, boolean v) {
        GL40C.glUniform1i(getLocation(name), v ? 1 : 0);
    }

    public void setUniform(String name, Matrix4f v) {
        GL30C.glUniformMatrix4fv(getLocation(name), false, v.get(new float[16]));
    }

    public void setUniform(String name, float v1, float v2, float v3, float v4) {
        GL30C.glUniform4f(getLocation(name), v1, v2, v3, v4);
    }

    public void bind() {
        glUseProgram(program);
    }

    public void unbind() {
        glUseProgram(0);
    }
}