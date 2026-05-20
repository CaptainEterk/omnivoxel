package omnivoxel.util.bytes;

import io.netty.buffer.ByteBuf;
import omnivoxel.common.annotations.NotNull;

public class ByteUtils {
    public static String bytesToHex(ByteBuf byteBuf, int start, int length) {
        StringBuilder hex = new StringBuilder();
        byte[] clientIDBytes = getBytes(byteBuf, start, length);
        for (int i = 0; i < 32; i++) {
            hex.append(String.format("%02X", clientIDBytes[i]));
        }
        return hex.toString();
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    public static byte[] getBytes(ByteBuf byteBuf, int i, int length) {
        byte[] bytes = new byte[length];
        byteBuf.getBytes(i, bytes);
        return bytes;
    }

    public static void addFloat(byte @NotNull [] bytes, float f, int index) {
        addInt(bytes, Float.floatToIntBits(f), index);
    }

    public static float getFloat(byte[] bytes, int index) {
        return Float.intBitsToFloat(getInt(bytes, index));
    }

    public static void addInt(byte @NotNull [] bytes, int i, int index) {
        bytes[index] = (byte) (i >> 24);
        bytes[index + 1] = (byte) (i >> 16);
        bytes[index + 2] = (byte) (i >> 8);
        bytes[index + 3] = (byte) (i);
    }

    public static int getInt(byte @NotNull [] bytes, int index) {
        return ((bytes[index] & 0xFF) << 24) |
                ((bytes[index + 1] & 0xFF) << 16) |
                ((bytes[index + 2] & 0xFF) << 8) |
                (bytes[index + 3] & 0xFF);
    }

    public static void addFloats(byte @NotNull [] bytes, int i, float... floats) {
        for (int j = 0; j < floats.length; j++) {
            addFloat(bytes, floats[j], i + j * Float.BYTES);
        }
    }

    public static void addDouble(byte[] bytes, double d, int index) {
        addLong(bytes, Double.doubleToLongBits(d), index);
    }

    public static double getDouble(byte[] bytes, int index) {
        return Double.longBitsToDouble(getLong(bytes, index));
    }

    public static void addLong(byte @NotNull [] bytes, long i, int index) {
        bytes[index] = (byte) (i >> 56);
        bytes[index + 1] = (byte) (i >> 48);
        bytes[index + 2] = (byte) (i >> 40);
        bytes[index + 3] = (byte) (i >> 32);
        bytes[index + 4] = (byte) (i >> 24);
        bytes[index + 5] = (byte) (i >> 16);
        bytes[index + 6] = (byte) (i >> 8);
        bytes[index + 7] = (byte) (i);
    }

    public static long getLong(byte @NotNull [] bytes, int index) {
        return ((long) (bytes[index] & 0xFF) << 56) |
                ((long) (bytes[index + 1] & 0xFF) << 48) |
                ((long) (bytes[index + 2] & 0xFF) << 40) |
                ((long) (bytes[index + 3] & 0xFF) << 32) |
                ((long) (bytes[index + 4] & 0xFF) << 24) |
                ((long) (bytes[index + 5] & 0xFF) << 16) |
                ((long) (bytes[index + 6] & 0xFF) << 8) |
                ((long) (bytes[index + 7] & 0xFF));
    }
}