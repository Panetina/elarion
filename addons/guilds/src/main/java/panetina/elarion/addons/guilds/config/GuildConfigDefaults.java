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

            progression:
              levels:
                - required-contributions: 0
                  member-capacity: 10
                - required-contributions: 250
                  member-capacity: 15
                - required-contributions: 750
                  member-capacity: 20
                - required-contributions: 1750
                  member-capacity: 30
                - required-contributions: 3500
                  member-capacity: 40

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
