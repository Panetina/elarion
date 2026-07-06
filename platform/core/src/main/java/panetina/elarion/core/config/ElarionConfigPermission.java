package panetina.elarion.core.config;

public enum ElarionConfigPermission {
    PUBLIC("Public"),
    OPERATOR("OP level 4");

    private final String label;

    ElarionConfigPermission(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}

