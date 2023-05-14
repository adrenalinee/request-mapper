package malibu.requestmapper;

public interface TypeConverter<T> {

    T convert(String value, Class<T> parameterTypeClass);
}
