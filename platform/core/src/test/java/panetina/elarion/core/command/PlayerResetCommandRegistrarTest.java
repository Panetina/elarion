package panetina.elarion.core.command;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.reset.PlayerResetRegistry;
import panetina.elarion.core.service.PlayerResetService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class PlayerResetCommandRegistrarTest {
    @Test
    void registersAdminOnlyPreviewConfirmAndCancelPaths() {
        PlayerResetService service = new PlayerResetService(
                LoggerFactory.getLogger(PlayerResetCommandRegistrarTest.class), new PlayerResetRegistry());
        var reset = PlayerResetCommandRegistrar.register(service).build();

        assertEquals(4, PlayerResetCommandRegistrar.ADMIN_PERMISSION_LEVEL);
        assertNotNull(reset.getChild("players"));
        assertNotNull(reset.getChild("players").getChild("confirm"));
        assertNotNull(reset.getChild("players").getChild("confirm").getChild("token"));
        assertNotNull(reset.getChild("players").getChild("cancel"));
        assertNotNull(reset.getChild("players").getChild("cancel").getChild("token"));
    }
}
