package panetina.elarion.core.integration.minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public final class MinecraftBridgeProtocol {
    private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{3,16}");

    private MinecraftBridgeProtocol() {
    }

    public static List<Command> parseChanges(String body, long after) {
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        if (!requiredBoolean(root, "ok")) throw new IllegalArgumentException("Bridge response was not successful.");
        JsonArray commands = root.getAsJsonArray("commands");
        if (commands == null || commands.size() > 100) throw new IllegalArgumentException("Invalid command batch.");
        List<Command> result = new ArrayList<>(commands.size());
        long previous = after;
        for (JsonElement element : commands) {
            JsonObject value = element.getAsJsonObject();
            long sequence = parseSequence(requiredString(value, "sequence"));
            if (sequence <= previous) throw new IllegalArgumentException("Commands are not strictly ordered.");
            UUID minecraftUuid = UUID.fromString(requiredString(value, "minecraftUuid"));
            String minecraftName = requiredString(value, "minecraftName");
            if (!PLAYER_NAME.matcher(minecraftName).matches()) throw new IllegalArgumentException("Invalid Minecraft name.");
            Action action = Action.valueOf(requiredString(value, "action"));
            result.add(new Command(sequence, minecraftUuid, minecraftName, action));
            previous = sequence;
        }
        return List.copyOf(result);
    }

    public static String acknowledgementBody(List<Acknowledgement> acknowledgements) {
        if (acknowledgements.isEmpty() || acknowledgements.size() > 100) {
            throw new IllegalArgumentException("Acknowledgement count must be between 1 and 100.");
        }
        JsonArray values = new JsonArray();
        for (Acknowledgement acknowledgement : acknowledgements) {
            JsonObject value = new JsonObject();
            value.addProperty("sequence", Long.toString(acknowledgement.sequence()));
            value.addProperty("applied", acknowledgement.applied());
            if (acknowledgement.error() != null && !acknowledgement.error().isBlank()) {
                value.addProperty("error", acknowledgement.error().substring(0, Math.min(500, acknowledgement.error().length())));
            }
            values.add(value);
        }
        JsonObject root = new JsonObject();
        root.add("acknowledgements", values);
        return root.toString();
    }

    public static void requireSuccess(String body) {
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        if (!requiredBoolean(root, "ok")) throw new IllegalArgumentException("Bridge response was not successful.");
    }

    private static long parseSequence(String value) {
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid bridge sequence.", exception);
        }
    }

    private static String requiredString(JsonObject object, String name) {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Missing bridge field: " + name);
        }
        return value.getAsString();
    }

    private static boolean requiredBoolean(JsonObject object, String name) {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("Missing bridge field: " + name);
        }
        return value.getAsBoolean();
    }

    public enum Action { ADD, REMOVE }

    public record Command(long sequence, UUID minecraftUuid, String minecraftName, Action action) {
    }

    public record Acknowledgement(long sequence, boolean applied, String error) {
        public static Acknowledgement applied(long sequence) {
            return new Acknowledgement(sequence, true, null);
        }

        public static Acknowledgement failed(long sequence, String error) {
            return new Acknowledgement(sequence, false, error);
        }
    }
}
