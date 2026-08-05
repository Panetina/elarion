package panetina.elarion.core.model;

/** Channels exposed by the compact chat composer. Guild dispatch stays addon-owned. */
public enum ElarionChatChannel {
    LOCAL,
    REALM,
    ALLIANCE,
    GUILD,
    /** Legacy transport value; deliberately omitted from the composer UI. */
    PRIVATE
}
