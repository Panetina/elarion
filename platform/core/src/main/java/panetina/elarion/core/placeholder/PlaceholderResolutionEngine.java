package panetina.elarion.core.placeholder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlaceholderResolutionEngine {
    private static final Pattern TOKEN = Pattern.compile("%([A-Za-z0-9_.:-]+)%|\\{([A-Za-z][A-Za-z0-9_.:-]*)}");
    private final ElarionPlaceholderRegistry registry;

    public PlaceholderResolutionEngine(ElarionPlaceholderRegistry registry) {
        this.registry = registry;
    }

    public PlaceholderResolution resolve(String template, PlaceholderResolutionContext context,
                                         PlaceholderResolutionLimits limits) {
        Request request = new Request(context == null
                ? PlaceholderResolutionContext.publicContext(PlaceholderRenderContext.UI, Map.of()) : context,
                limits == null ? PlaceholderResolutionLimits.DEFAULTS : limits);
        String result = resolveText(template == null ? "" : template, request, 0);
        boolean truncated = result.length() > request.limits.maxOutputLength();
        if (truncated) result = result.substring(0, request.limits.maxOutputLength());
        return new PlaceholderResolution(result, request.resolvedCount, truncated, request.diagnostics);
    }

    private String resolveText(String input, Request request, int depth) {
        if (input.isEmpty()) return "";
        if (depth >= request.limits.maxDepth()) {
            request.diagnostic("depth", "", "Placeholder nesting limit reached");
            return input;
        }
        Matcher matcher = TOKEN.matcher(input);
        StringBuilder output = new StringBuilder(Math.min(input.length() + 32, request.limits.maxOutputLength()));
        int cursor = 0;
        while (matcher.find() && output.length() < request.limits.maxOutputLength()) {
            output.append(input, cursor, matcher.start());
            String rawToken = matcher.group();
            String id = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (++request.encountered > request.limits.maxPlaceholders()) {
                request.diagnostic("count", rawToken, "Placeholder count limit reached");
                output.append(rawToken);
            } else {
                output.append(resolveToken(id, rawToken, request, depth));
            }
            cursor = matcher.end();
        }
        if (cursor < input.length() && output.length() < request.limits.maxOutputLength()) {
            int remaining = request.limits.maxOutputLength() - output.length();
            output.append(input, cursor, Math.min(input.length(), cursor + remaining));
        }
        return output.toString();
    }

    private String resolveToken(String requestedId, String rawToken, Request request, int depth) {
        PlaceholderAlias alias = registry.alias(requestedId).orElse(null);
        String canonicalId = alias == null ? normalize(requestedId) : alias.targetId();
        ElarionPlaceholderRegistry.Registration registration = registry.registration(canonicalId).orElse(null);
        if (registration == null) {
            request.diagnostic("unknown", rawToken, "Unknown placeholder");
            return rawToken;
        }
        PlaceholderDescriptor descriptor = registration.descriptor();
        if (!descriptor.contexts().contains(request.context.renderContext())) {
            request.diagnostic("context", rawToken, "Placeholder unavailable in this rendering context");
            return failure(descriptor.unauthorizedBehavior(), rawToken);
        }
        if (!descriptor.visibility().allows(request.context)) {
            request.diagnostic("unauthorized", rawToken, "Placeholder visibility denied");
            return failure(descriptor.unauthorizedBehavior(), rawToken);
        }
        for (String key : descriptor.requiredContextKeys()) {
            if (request.context.value(key).isBlank()) {
                request.diagnostic("missing-context", rawToken, "Missing context key " + key);
                return failure(descriptor.missingBehavior(), rawToken);
            }
        }
        String value = request.memo.get(canonicalId);
        if (value == null) {
            if (!request.resolving.add(canonicalId)) {
                request.diagnostic("cycle", rawToken, "Placeholder cycle detected");
                return rawToken;
            }
            try {
                value = registration.resolver().resolve(request.context);
                if (value == null || value.isBlank()) {
                    request.diagnostic("missing", rawToken, "Placeholder resolver returned no value");
                    return failure(descriptor.missingBehavior(), rawToken);
                }
                value = TOKEN.matcher(value).find() ? resolveText(value, request, depth + 1) : value;
                request.memo.put(canonicalId, value);
            } catch (RuntimeException exception) {
                request.diagnostic("resolver", rawToken, "Placeholder resolver failed");
                return failure(descriptor.missingBehavior(), rawToken);
            } finally {
                request.resolving.remove(canonicalId);
            }
        }
        value = alias == null ? value : alias.transform().apply(value);
        request.resolvedCount++;
        return value;
    }

    private static String failure(PlaceholderFailureBehavior behavior, String token) {
        return behavior == PlaceholderFailureBehavior.EMPTY ? "" : token;
    }

    private static String normalize(String id) {
        try {
            return PlaceholderDescriptor.normalize(id);
        } catch (IllegalArgumentException exception) {
            return "";
        }
    }

    private static final class Request {
        private final PlaceholderResolutionContext context;
        private final PlaceholderResolutionLimits limits;
        private final Map<String, String> memo = new HashMap<>();
        private final Set<String> resolving = new HashSet<>();
        private final List<PlaceholderDiagnostic> diagnostics = new ArrayList<>();
        private int encountered;
        private int resolvedCount;

        private Request(PlaceholderResolutionContext context, PlaceholderResolutionLimits limits) {
            this.context = context;
            this.limits = limits;
        }

        private void diagnostic(String code, String token, String message) {
            if (diagnostics.size() < limits.maxDiagnostics()) {
                diagnostics.add(new PlaceholderDiagnostic(code, token, message));
            }
        }
    }
}
