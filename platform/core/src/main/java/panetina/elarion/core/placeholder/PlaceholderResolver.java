package panetina.elarion.core.placeholder;

@FunctionalInterface
public interface PlaceholderResolver {
    String resolve(PlaceholderResolutionContext context);
}
