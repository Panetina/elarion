package panetina.elarion.addons.groups.config;

public final class GroupConfigDefaults {
    private GroupConfigDefaults() {
    }

    public static final String GROUPS = """
            config-version: 1
            enabled: true

            creation:
              fee: 25

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
