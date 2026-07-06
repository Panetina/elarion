package panetina.elarion.core.config;

import java.util.List;

@FunctionalInterface
public interface ElarionConfigValidator<T> {
    List<String> validate(T value);

    static <T> ElarionConfigValidator<T> pass() {
        return ignored -> List.of();
    }

    static ElarionConfigValidator<Integer> integerRange(String path, int minimum, int maximum) {
        return value -> {
            int checked = value == null ? 0 : value;
            if (checked < minimum || checked > maximum) {
                return List.of(path + ": must be between " + minimum + " and " + maximum);
            }
            return List.of();
        };
    }

    static ElarionConfigValidator<Integer> integerMinimum(String path, int minimum) {
        return value -> {
            int checked = value == null ? 0 : value;
            if (checked < minimum) {
                return List.of(path + ": must be at least " + minimum);
            }
            return List.of();
        };
    }

    static ElarionConfigValidator<Long> longMinimum(String path, long minimum) {
        return value -> {
            long checked = value == null ? 0L : value;
            if (checked < minimum) {
                return List.of(path + ": must be at least " + minimum);
            }
            return List.of();
        };
    }

    static ElarionConfigValidator<String> nonBlank(String path) {
        return value -> value == null || value.isBlank()
                ? List.of(path + ": must not be blank")
                : List.of();
    }
}
