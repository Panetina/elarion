package panetina.elarion.core.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.config.ElarionConfigChangeError;
import panetina.elarion.core.config.ElarionConfigChangeResult;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigEditControl;
import panetina.elarion.core.config.ElarionConfigEditTarget;
import panetina.elarion.core.config.ElarionConfigPermission;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionConfigEditPayloadTest {
    @Test
    void targetNormalizesIdsAndExposesStableKey() {
        ElarionConfigEditTarget target = new ElarionConfigEditTarget("Core", "UI", "Font-Scale");

        assertEquals("core", target.domainId());
        assertEquals("ui", target.categoryId());
        assertEquals("font-scale", target.entryId());
        assertEquals("core:ui:font-scale", target.targetKey());
        assertThrows(IllegalArgumentException.class,
                () -> new ElarionConfigEditTarget("core", "bad id", "font-scale"));
    }

    @Test
    void openPayloadRoundTripsEditControlSnapshot() {
        ElarionConfigEditControl control = new ElarionConfigEditControl(
                target(),
                "Font Scale",
                "Server-wide Elarion UI font scale.",
                "ui_theme.yml.defaults.font-scale-percent",
                ElarionConfigCodec.ValueType.INTEGER,
                "100",
                "100",
                List.of("100", "125", "150"),
                "100",
                "150",
                true,
                false,
                ElarionConfigPermission.PUBLIC,
                ElarionConfigPermission.OPERATOR,
                true,
                false,
                "",
                "Config apply is not enabled yet.");
        ElarionConfigEditOpenPayload payload = new ElarionConfigEditOpenPayload(control, "Ready");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        ElarionConfigEditOpenPayload.CODEC.encode(buffer, payload);
        ElarionConfigEditOpenPayload decoded = ElarionConfigEditOpenPayload.CODEC.decode(buffer);

        assertEquals(payload, decoded);
        assertEquals(ElarionConfigCodec.ValueType.INTEGER, decoded.control().valueType());
        assertEquals(List.of("100", "125", "150"), decoded.control().choices());
        assertTrue(decoded.control().inputEditable());
        assertFalse(decoded.control().applyAvailable());
        assertEquals("Config apply is not enabled yet.", decoded.control().applyDisabledReason());
    }

    @Test
    void requestPayloadRoundTripsIntentAndExpectedCurrentValue() {
        ElarionConfigEditRequestPayload payload = new ElarionConfigEditRequestPayload(
                target(),
                "100",
                "125",
                "admin-panel-preview",
                ElarionConfigEditRequestPayload.Intent.VALIDATE);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        ElarionConfigEditRequestPayload.CODEC.encode(buffer, payload);
        ElarionConfigEditRequestPayload decoded = ElarionConfigEditRequestPayload.CODEC.decode(buffer);

        assertEquals(payload, decoded);
        assertEquals("core:ui:font-scale-percent", decoded.target().targetKey());
    }

    @Test
    void resultPayloadRoundTripsStructuredErrorsAndAuditPreview() {
        ElarionConfigEditResultPayload payload = new ElarionConfigEditResultPayload(
                target(),
                ElarionConfigChangeResult.Status.REJECTED,
                "100",
                "",
                false,
                false,
                false,
                "Would change Core UI font scale.",
                List.of(ElarionConfigChangeError.of(
                        ElarionConfigChangeError.Code.VALIDATION_FAILED,
                        "ui_theme.yml.defaults.font-scale-percent",
                        "Must be between 100 and 150.")),
                "Invalid value.");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        ElarionConfigEditResultPayload.CODEC.encode(buffer, payload);
        ElarionConfigEditResultPayload decoded = ElarionConfigEditResultPayload.CODEC.decode(buffer);

        assertEquals(payload, decoded);
        assertEquals(ElarionConfigChangeError.Code.VALIDATION_FAILED, decoded.errors().getFirst().code());
        assertEquals("Must be between 100 and 150.", decoded.errors().getFirst().message());
    }

    private static ElarionConfigEditTarget target() {
        return new ElarionConfigEditTarget("core", "ui", "font-scale-percent");
    }
}
