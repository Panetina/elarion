package panetina.elarion.addons.guilds.config;

public final class GuildConfigException extends RuntimeException {
    public GuildConfigException(String message) {
        super(message);
    }

    public GuildConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
