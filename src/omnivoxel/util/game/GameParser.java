package omnivoxel.util.game;

import omnivoxel.server.ConstantServerSettings;
import omnivoxel.server.games.Game;
import omnivoxel.util.game.nodes.*;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class GameParser {
    public static GameNode parseNode(String string) {
        return parseNode(string, null, true).value;
    }

    private static Result parseNode(String string, String key, boolean root) {
        int i = nextChar(string, 0);
        char c = string.charAt(i);

        if (c == '{') {
            return parseObject(string, i + 1, key);
        } else if (c == '[') {
            return parseArray(string, i + 1, key);
        } else if (c == '"') {
            return parseString(string, i + 1, key);
        } else if (Character.isDigit(c) || c == '-') {
            return parseNumber(string, i, key);
        } else if (string.startsWith("true", i)) {
            return new Result(new BooleanGameNode(key, true), i + 4);
        } else if (string.startsWith("false", i)) {
            return new Result(new BooleanGameNode(key, false), i + 5);
        } else if (string.startsWith("null", i)) {
            return new Result(new NullGameNode(key), i + 4);
        }

        throw new IllegalArgumentException("Unexpected character: " + c);
    }

    private static Result parseObject(String string, int idx, String key) {
        Map<String, GameNode> map = new LinkedHashMap<>();
        int i = nextChar(string, idx);

        while (string.charAt(i) != '}') {
            Result keyRes = parseString(string, i + 1, null);
            String fieldKey = ((StringGameNode) keyRes.value).value();
            i = nextChar(string, keyRes.next);

            if (string.charAt(i) != ':') {
                throw new IllegalArgumentException("Expected ':' after key");
            }
            i = nextChar(string, i + 1);

            Result valRes = parseNode(string.substring(i), fieldKey, false);
            map.put(fieldKey, valRes.value);
            i += valRes.next;
            i = nextChar(string, i);

            if (string.charAt(i) == ',') {
                i = nextChar(string, i + 1);
            } else if (string.charAt(i) != '}') {
                throw new IllegalArgumentException("Expected ',' or '}' in object");
            }
        }
        return new Result(handleObject(new ObjectGameNode(key, map)), i + 1);
    }

    private static Result parseArray(String string, int idx, String key) {
        List<GameNode> list = new ArrayList<>();
        int i = nextChar(string, idx);

        int counter = 0;
        while (string.charAt(i) != ']') {
            String elementKey = String.valueOf(counter++);
            Result valRes = parseNode(string.substring(i), elementKey, false);
            list.add(valRes.value);
            i += valRes.next;
            i = nextChar(string, i);

            if (string.charAt(i) == ',') {
                i = nextChar(string, i + 1);
            } else if (string.charAt(i) != ']') {
                throw new IllegalArgumentException("Expected ',' or ']' in array");
            }
        }
        return new Result(new ArrayGameNode(key, list.toArray(GameNode[]::new)), i + 1);
    }

    private static Result parseString(String string, int idx, String key) {
        StringBuilder sb = new StringBuilder();
        int i = idx;
        while (true) {
            char c = string.charAt(i);
            if (c == '"') break;
            if (c == '\\') { // escape
                char next = string.charAt(++i);
                if (next == 'n') sb.append('\n');
                else if (next == 't') sb.append('\t');
                else if (next == 'r') sb.append('\r');
                else if (next == '"') sb.append('"');
                else if (next == '\\') sb.append('\\');
                else throw new IllegalArgumentException("Unknown escape: \\" + next);
            } else {
                sb.append(c);
            }
            i++;
        }
        return new Result(new StringGameNode(key, sb.toString()), i + 1);
    }

    private static Result parseNumber(String string, int idx, String key) {
        int i = idx;
        while (i < string.length() &&
                (Character.isDigit(string.charAt(i)) || string.charAt(i) == '.' || string.charAt(i) == '-')) {
            i++;
        }
        String numStr = string.substring(idx, i);

        double num = Double.parseDouble(numStr);
        return new Result(new DoubleGameNode(key, num), i);
    }

    private static int nextChar(String string, int idx) {
        int i = idx;
        while (i < string.length() && shouldSkip(string.charAt(i))) {
            i++;
        }
        return i;
    }

    private static boolean shouldSkip(char c) {
        return Character.isWhitespace(c);
    }

    private static GameNode handleObject(ObjectGameNode objectGameNode) {
        StringGameNode type = Game.checkGameNodeType(objectGameNode.object().get("type"), StringGameNode.class);

        if (type == null) {
            return objectGameNode;
        } else if (Objects.equals(type.value(), "read_file")) {
            String path = ConstantServerSettings.GAME_LOCATION + Game.checkGameNodeType(objectGameNode.object().get("file"), StringGameNode.class).value() + ".json";
            try {
                String content = Files.readString(new File(path).toPath());
                return GameParser.parseNode(content);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read file: " + path, e);
            }
        } else if (Objects.equals(type.value(), "read_directory")) {
            String path = ConstantServerSettings.GAME_LOCATION + Game.checkGameNodeType(objectGameNode.object().get("file"), StringGameNode.class).value();
            File dir = new File(path);
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException("Path is not a directory: " + path);
            }

            List<GameNode> children = new ArrayList<>();
            int index = 0;
            for (File f : Objects.requireNonNull(dir.listFiles())) {
                if (f.isFile()) {
                    try {
                        String content = Files.readString(f.toPath());
                        GameNode parsed = GameParser.parseNode(content);
                        if (parsed instanceof ObjectGameNode obj) {
                            children.add(new ObjectGameNode(String.valueOf(index++), obj.object()));
                        } else if (parsed instanceof ArrayGameNode arr) {
                            children.add(new ArrayGameNode(String.valueOf(index++), arr.nodes()));
                        } else {
                            children.add(parsed);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to read file: " + f, e);
                    }
                }
            }
            return new ArrayGameNode(objectGameNode.key(), children.toArray(GameNode[]::new));
        } else {
            return objectGameNode;
        }
    }

    private record Result(GameNode value, int next) {
    }
}