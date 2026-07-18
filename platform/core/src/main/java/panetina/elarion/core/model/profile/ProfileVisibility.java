package panetina.elarion.core.model.profile;

public enum ProfileVisibility {
    PUBLIC,
    SELF,
    ADMIN;

    public boolean canView(CitizenProfileRequestContext context) {
        if (context == null) return this == PUBLIC;
        return switch (this) {
            case PUBLIC -> true;
            case SELF -> context.self() || context.administrator();
            case ADMIN -> context.administrator();
        };
    }
}
