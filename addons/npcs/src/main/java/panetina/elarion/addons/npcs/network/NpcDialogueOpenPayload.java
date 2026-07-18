package panetina.elarion.addons.npcs.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record NpcDialogueOpenPayload(
        UUID npcId,
        String dialogueId,
        String nodeId,
        String npcName,
        String portrait,
        String portraitType,
        String portraitPlayerName,
        String portraitFallbackType,
        String portraitFallbackTexture,
        String playerText,
        String text,
        String npcSound,
        String npcVoice,
        String playerSound,
        String playerVoice,
        String presentationKind,
        String feedback,
        boolean feedbackError,
        boolean hasCurrencyBalance,
        long currencyBalance,
        String currencyPlural,
        String relationLabel,
        int relationValue,
        int panelWidth,
        int minPanelHeight,
        int maxPanelHeight,
        int minimumUiScalePercent,
        int optionRowHeight,
        int visibleOptionRows,
        int scrollbarWidth,
        int padding,
        int buttonHeight,
        int compactButtonHeight,
        int buttonGap,
        int contentGap,
        int npcRowHeight,
        int playerRowHeight,
        int optionColumnsWide,
        int portraitSize,
        int playerPortraitSize,
        boolean showPortraitReference,
        boolean showRelationBar,
        boolean typingEnabled,
        int typingCharactersPerSecond,
        boolean typingClickCompletes,
        boolean typingSoundEnabled,
        int typingSoundIntervalCharacters,
        String themeVariant,
        List<NpcDialogueCardPayload> cards,
        List<NpcDialogueOptionPayload> options
) implements CustomPayload {
    public static final Id<NpcDialogueOpenPayload> ID =
            new Id<>(Identifier.of("elarion_npcs", "dialogue_open"));

    public static final PacketCodec<PacketByteBuf, NpcDialogueOpenPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeUuid(payload.npcId());
                ElarionPacketCodecs.writeString(buffer, payload.dialogueId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.nodeId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.npcName(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.portrait(), 256);
                ElarionPacketCodecs.writeString(buffer, payload.portraitType(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.portraitPlayerName(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.portraitFallbackType(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.portraitFallbackTexture(), 256);
                ElarionPacketCodecs.writeString(buffer, payload.playerText(), 512);
                ElarionPacketCodecs.writeString(buffer, payload.text(), 4096);
                ElarionPacketCodecs.writeString(buffer, payload.npcSound(), 256);
                ElarionPacketCodecs.writeString(buffer, payload.npcVoice(), 256);
                ElarionPacketCodecs.writeString(buffer, payload.playerSound(), 256);
                ElarionPacketCodecs.writeString(buffer, payload.playerVoice(), 256);
                ElarionPacketCodecs.writeString(buffer, payload.presentationKind(), 32);
                ElarionPacketCodecs.writeString(buffer, payload.feedback(), 1024);
                buffer.writeBoolean(payload.feedbackError());
                buffer.writeBoolean(payload.hasCurrencyBalance());
                buffer.writeVarLong(payload.currencyBalance());
                ElarionPacketCodecs.writeString(buffer, payload.currencyPlural(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.relationLabel(), 128);
                buffer.writeVarInt(payload.relationValue());
                buffer.writeVarInt(payload.panelWidth());
                buffer.writeVarInt(payload.minPanelHeight());
                buffer.writeVarInt(payload.maxPanelHeight());
                buffer.writeVarInt(payload.minimumUiScalePercent());
                buffer.writeVarInt(payload.optionRowHeight());
                buffer.writeVarInt(payload.visibleOptionRows());
                buffer.writeVarInt(payload.scrollbarWidth());
                buffer.writeVarInt(payload.padding());
                buffer.writeVarInt(payload.buttonHeight());
                buffer.writeVarInt(payload.compactButtonHeight());
                buffer.writeVarInt(payload.buttonGap());
                buffer.writeVarInt(payload.contentGap());
                buffer.writeVarInt(payload.npcRowHeight());
                buffer.writeVarInt(payload.playerRowHeight());
                buffer.writeVarInt(payload.optionColumnsWide());
                buffer.writeVarInt(payload.portraitSize());
                buffer.writeVarInt(payload.playerPortraitSize());
                buffer.writeBoolean(payload.showPortraitReference());
                buffer.writeBoolean(payload.showRelationBar());
                buffer.writeBoolean(payload.typingEnabled());
                buffer.writeVarInt(payload.typingCharactersPerSecond());
                buffer.writeBoolean(payload.typingClickCompletes());
                buffer.writeBoolean(payload.typingSoundEnabled());
                buffer.writeVarInt(payload.typingSoundIntervalCharacters());
                ElarionPacketCodecs.writeString(buffer, payload.themeVariant(), 64);
                buffer.writeVarInt(payload.cards().size());
                payload.cards().forEach(card -> NpcDialogueCardPayload.write(card, buffer));
                buffer.writeVarInt(payload.options().size());
                payload.options().forEach(option -> NpcDialogueOptionPayload.write(option, buffer));
            },
            buffer -> {
                UUID npcId = buffer.readUuid();
                String dialogueId = ElarionPacketCodecs.readString(buffer, 128);
                String nodeId = ElarionPacketCodecs.readString(buffer, 128);
                String npcName = ElarionPacketCodecs.readString(buffer, 128);
                String portrait = ElarionPacketCodecs.readString(buffer, 256);
                String portraitType = ElarionPacketCodecs.readString(buffer, 64);
                String portraitPlayerName = ElarionPacketCodecs.readString(buffer, 64);
                String portraitFallbackType = ElarionPacketCodecs.readString(buffer, 64);
                String portraitFallbackTexture = ElarionPacketCodecs.readString(buffer, 256);
                String playerText = ElarionPacketCodecs.readString(buffer, 512);
                String text = ElarionPacketCodecs.readString(buffer, 4096);
                String npcSound = ElarionPacketCodecs.readString(buffer, 256);
                String npcVoice = ElarionPacketCodecs.readString(buffer, 256);
                String playerSound = ElarionPacketCodecs.readString(buffer, 256);
                String playerVoice = ElarionPacketCodecs.readString(buffer, 256);
                String presentationKind = ElarionPacketCodecs.readString(buffer, 32);
                String feedback = ElarionPacketCodecs.readString(buffer, 1024);
                boolean feedbackError = buffer.readBoolean();
                boolean hasCurrencyBalance = buffer.readBoolean();
                long currencyBalance = buffer.readVarLong();
                String currencyPlural = ElarionPacketCodecs.readString(buffer, 64);
                String relationLabel = ElarionPacketCodecs.readString(buffer, 128);
                int relationValue = buffer.readVarInt();
                int panelWidth = buffer.readVarInt();
                int minPanelHeight = buffer.readVarInt();
                int maxPanelHeight = buffer.readVarInt();
                int minimumUiScalePercent = buffer.readVarInt();
                int optionRowHeight = buffer.readVarInt();
                int visibleOptionRows = buffer.readVarInt();
                int scrollbarWidth = buffer.readVarInt();
                int padding = buffer.readVarInt();
                int buttonHeight = buffer.readVarInt();
                int compactButtonHeight = buffer.readVarInt();
                int buttonGap = buffer.readVarInt();
                int contentGap = buffer.readVarInt();
                int npcRowHeight = buffer.readVarInt();
                int playerRowHeight = buffer.readVarInt();
                int optionColumnsWide = buffer.readVarInt();
                int portraitSize = buffer.readVarInt();
                int playerPortraitSize = buffer.readVarInt();
                boolean showPortraitReference = buffer.readBoolean();
                boolean showRelationBar = buffer.readBoolean();
                boolean typingEnabled = buffer.readBoolean();
                int typingCharactersPerSecond = buffer.readVarInt();
                boolean typingClickCompletes = buffer.readBoolean();
                boolean typingSoundEnabled = buffer.readBoolean();
                int typingSoundIntervalCharacters = buffer.readVarInt();
                String themeVariant = ElarionPacketCodecs.readString(buffer, 64);
                int cardCount = ElarionPacketCodecs.readBoundedCount(buffer, 64);
                List<NpcDialogueCardPayload> cards = new ArrayList<>();
                for (int index = 0; index < cardCount; index++) {
                    cards.add(NpcDialogueCardPayload.read(buffer));
                }
                int count = ElarionPacketCodecs.readBoundedCount(buffer, 64);
                List<NpcDialogueOptionPayload> options = new ArrayList<>();
                for (int index = 0; index < count; index++) {
                    options.add(NpcDialogueOptionPayload.read(buffer));
                }
                return new NpcDialogueOpenPayload(
                        npcId, dialogueId, nodeId, npcName, portrait, portraitType,
                        portraitPlayerName, portraitFallbackType, portraitFallbackTexture,
                        playerText, text,
                        npcSound, npcVoice, playerSound, playerVoice, presentationKind, feedback, feedbackError,
                        hasCurrencyBalance, currencyBalance, currencyPlural, relationLabel, relationValue,
                        panelWidth, minPanelHeight, maxPanelHeight,
                        minimumUiScalePercent, optionRowHeight, visibleOptionRows, scrollbarWidth,
                        padding, buttonHeight, compactButtonHeight,
                        buttonGap, contentGap, npcRowHeight, playerRowHeight, optionColumnsWide,
                        portraitSize, playerPortraitSize, showPortraitReference, showRelationBar,
                        typingEnabled, typingCharactersPerSecond, typingClickCompletes,
                        typingSoundEnabled, typingSoundIntervalCharacters,
                        themeVariant, List.copyOf(cards),
                        List.copyOf(options));
            }
    );

    public NpcDialogueOpenPayload {
        dialogueId = dialogueId == null ? "" : dialogueId;
        portraitPlayerName = portraitPlayerName == null ? "" : portraitPlayerName;
        portraitFallbackType = portraitFallbackType == null ? "placeholder" : portraitFallbackType;
        portraitFallbackTexture = portraitFallbackTexture == null ? "" : portraitFallbackTexture;
        npcSound = npcSound == null ? "" : npcSound;
        npcVoice = npcVoice == null ? "" : npcVoice;
        playerSound = playerSound == null ? "" : playerSound;
        playerVoice = playerVoice == null ? "" : playerVoice;
        presentationKind = presentationKind == null || presentationKind.isBlank() ? "dialogue" : presentationKind;
        currencyPlural = currencyPlural == null || currencyPlural.isBlank() ? "Currency" : currencyPlural;
        themeVariant = themeVariant == null || themeVariant.isBlank() ? "npc" : themeVariant;
        cards = cards == null ? List.of() : List.copyOf(cards);
        options = options == null ? List.of() : List.copyOf(options);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
