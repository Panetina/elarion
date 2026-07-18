package panetina.elarion.core.placeholder;

public enum PlaceholderVisibility {
    PUBLIC,
    SELF,
    REALM,
    ADMIN;

    public boolean allows(PlaceholderResolutionContext context) {
        if (context == null) return this == PUBLIC;
        return switch (this) {
            case PUBLIC -> true;
            case SELF -> context.viewerId() != null && context.viewerId().equals(context.subjectId());
            case REALM -> !context.viewerRealmId().isBlank()
                    && context.viewerRealmId().equals(context.subjectRealmId());
            case ADMIN -> context.admin();
        };
    }
}
