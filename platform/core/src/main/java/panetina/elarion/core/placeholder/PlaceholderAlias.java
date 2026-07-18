package panetina.elarion.core.placeholder;

public record PlaceholderAlias(String id, String targetId, PlaceholderTransform transform, boolean deprecated) {
    public PlaceholderAlias {
        id = PlaceholderDescriptor.normalize(id);
        targetId = PlaceholderDescriptor.normalize(targetId);
        transform = transform == null ? PlaceholderTransform.IDENTITY : transform;
    }
}
