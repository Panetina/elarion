package panetina.elarion.core.integration.minecraft;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class MinecraftBridgeProtocolTest {
    @Test
    void parsesBoundedStrictlyOrderedCommands() {
        String body = """
                {"ok":true,"commands":[
                  {"sequence":"41","minecraftUuid":"123e4567-e89b-12d3-a456-426614174000","minecraftName":"Panyel","action":"ADD"},
                  {"sequence":"42","minecraftUuid":"123e4567-e89b-12d3-a456-426614174001","minecraftName":"ElarionUser","action":"REMOVE"}
                ]}
                """;

        List<MinecraftBridgeProtocol.Command> commands = MinecraftBridgeProtocol.parseChanges(body, 40);

        assertEquals(2, commands.size());
        assertEquals(41, commands.getFirst().sequence());
        assertEquals(MinecraftBridgeProtocol.Action.REMOVE, commands.getLast().action());
    }

    @Test
    void rejectsDuplicateOrOutOfOrderSequences() {
        String body = """
                {"ok":true,"commands":[
                  {"sequence":"41","minecraftUuid":"123e4567-e89b-12d3-a456-426614174000","minecraftName":"Panyel","action":"ADD"},
                  {"sequence":"41","minecraftUuid":"123e4567-e89b-12d3-a456-426614174001","minecraftName":"ElarionUser","action":"REMOVE"}
                ]}
                """;

        assertThrows(IllegalArgumentException.class,
                () -> MinecraftBridgeProtocol.parseChanges(body, 40));
    }

    @Test
    void acknowledgementPayloadUsesStringSequencesAndBoundedErrors() {
        String body = MinecraftBridgeProtocol.acknowledgementBody(List.of(
                MinecraftBridgeProtocol.Acknowledgement.applied(41),
                MinecraftBridgeProtocol.Acknowledgement.failed(42, "rejected")));
        var values = JsonParser.parseString(body).getAsJsonObject().getAsJsonArray("acknowledgements");

        assertEquals("41", values.get(0).getAsJsonObject().get("sequence").getAsString());
        assertEquals("rejected", values.get(1).getAsJsonObject().get("error").getAsString());
    }
}
