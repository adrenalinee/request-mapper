package malibu.requestmapper;

import malibu.requestmapper.util.Maps;

import java.util.Map;
import java.util.Objects;

/**
 * 최초에 String으로 들어온 값을 handler method argument의 타입에 맞게 변환시키는 용도로 사용.
 */
public final class TypeConverterRegistry {

    private final Map<Class<?>, TypeConverter<?>> defaultConverter = Maps.newHashMap();

    private final Map<Class<?>, TypeConverter<?>> customConverter = Maps.newHashMap();

    private static final TypeConverterRegistry instance = new TypeConverterRegistry();

    private TypeConverterRegistry() {
        addDefaultConverter();
    }

    public static TypeConverterRegistry getInstance() {
        return instance;
    }

    private void addDefaultConverter() {
        defaultConverter.put(String.class, new StringConverter());

        defaultConverter.put(boolean.class, new BooleanConverter());
        defaultConverter.put(Boolean.class, new BooleanConverter());

        defaultConverter.put(int.class, new IntegerConverter());
        defaultConverter.put(Integer.class, new IntegerConverter());
        defaultConverter.put(long.class, new LongConverter());
        defaultConverter.put(Long.class, new LongConverter());
        defaultConverter.put(double.class, new DoubleConverter());
        defaultConverter.put(Double.class, new DoubleConverter());
        defaultConverter.put(float.class, new FloatConverter());
        defaultConverter.put(Float.class, new FloatConverter());
    }

    /**
     *
     * @param requiredType
     * @param typeConverter
     */
    public <T> void addCustomConverter(Class<T> requiredType, TypeConverter<T> typeConverter) {
        Objects.requireNonNull(requiredType);
        Objects.requireNonNull(typeConverter);

        customConverter.put(requiredType, typeConverter);
    }

    <T> TypeConverter<T> find(Class<T> requiredType) {
        if (defaultConverter.containsKey(requiredType)) {
            return (TypeConverter<T>) defaultConverter.get(requiredType);
        }

        return (TypeConverter<T>) customConverter.get(requiredType);
    }

    boolean exist(Class<?> requiredType) {
        if (defaultConverter.containsKey(requiredType)) {
            return true;
        }

        return customConverter.containsKey(requiredType);
    }
}

/**
 *
 */
final class StringConverter implements TypeConverter<String> {

    @Override
    public String convert(String value, Class<String> parameterTypeClass) {
        return value;
    }
}

/**
 *
 */
final class BooleanConverter implements TypeConverter<Boolean> {

    @Override
    public Boolean convert(String value, Class<Boolean> parameterTypeClass) {
        return Boolean.parseBoolean(value);
    }
}

/**
 *
 */
final class IntegerConverter implements TypeConverter<Integer> {

    @Override
    public Integer convert(String value, Class<Integer> parameterTypeClass) {
        return Integer.parseInt(value);
    }
}

/**
 *
 */
final class LongConverter implements TypeConverter<Long> {

    @Override
    public Long convert(String value, Class<Long> parameterTypeClass) {
        return Long.parseLong(value);
    }
}

/**
 *
 */
final class DoubleConverter implements TypeConverter<Double> {

    @Override
    public Double convert(String value, Class<Double> parameterTypeClass) {
        return Double.parseDouble(value);
    }
}

/**
 *
 */
final class FloatConverter implements TypeConverter<Float> {

    @Override
    public Float convert(String value, Class<Float> parameterTypeClass) {
        return Float.parseFloat(value);
    }
}