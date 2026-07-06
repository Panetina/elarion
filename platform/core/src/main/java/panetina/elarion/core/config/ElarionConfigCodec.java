package panetina.elarion.core.config;

import java.util.Locale;
import java.util.function.Function;

public interface ElarionConfigCodec<T> {
    ElarionConfigCodec<String> STRING = new Basic<>("string", ValueType.STRING,
            value -> value == null ? "" : value, value -> value);
    ElarionConfigCodec<Integer> INTEGER = new Basic<>("integer", ValueType.INTEGER,
            value -> Integer.toString(value == null ? 0 : value), value -> Integer.parseInt(value.trim()));
    ElarionConfigCodec<Long> LONG = new Basic<>("long", ValueType.LONG,
            value -> Long.toString(value == null ? 0L : value), value -> Long.parseLong(value.trim()));
    ElarionConfigCodec<Boolean> BOOLEAN = new Basic<>("boolean", ValueType.BOOLEAN,
            value -> Boolean.toString(Boolean.TRUE.equals(value)), ElarionConfigCodec::parseBoolean);

    String id();

    ValueType valueType();

    String format(T value);

    T parse(String value);

    enum ValueType {
        STRING,
        INTEGER,
        LONG,
        BOOLEAN,
        DECIMAL,
        ENUM
    }

    private static Boolean parseBoolean(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(normalized)) return true;
        if ("false".equals(normalized)) return false;
        throw new IllegalArgumentException("Expected true or false");
    }

    record Basic<T>(
            String id,
            ValueType valueType,
            Function<T, String> formatter,
            Function<String, T> parser
    ) implements ElarionConfigCodec<T> {
        @Override
        public String format(T value) {
            return formatter.apply(value);
        }

        @Override
        public T parse(String value) {
            return parser.apply(value);
        }
    }
}

