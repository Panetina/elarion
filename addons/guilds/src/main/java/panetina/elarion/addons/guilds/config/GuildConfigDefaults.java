package panetina.elarion.addons.guilds.config;

public final class GuildConfigDefaults {
    private GuildConfigDefaults() {
    }

    public static final String GUILDS = """
            config-version: 1
            enabled: true

            creation:
              fee: 100

            invitations:
              lifetime-days: 7

            tags:
              min-length: 2
              max-length: 6
              pattern: "[A-Z0-9]{2,6}"
              blocked:
                - "ADMIN"
                - "STAFF"
                - "OP"

            identity:
              id-pattern: "[a-z0-9_-]{3,32}"
              max-name-length: 48
            """;
}
