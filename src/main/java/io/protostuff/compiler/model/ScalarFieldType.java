package io.protostuff.compiler.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Field types for scalar value types.
 * See https://developers.google.com/protocol-buffers/docs/proto#scalar
 *
 * @author Kostiantyn Shchepanovskyi
 */
public interface ScalarFieldType extends FieldType {

    enum Holder implements ScalarFieldType {

        INT32,
        INT64,
        UINT32,
        UINT64,
        SINT32,
        SINT64,
        FIXED32,
        FIXED64,
        SFIXED32,
        SFIXED64,
        FLOAT,
        DOUBLE,
        BOOL,
        STRING,
        BYTES;

        private static Map<String, ScalarFieldType> map = new HashMap<>();

        static {
            for (ScalarFieldType type : values()) {
                map.put(type.getReference(), type);
            }
        }

        @Override
        public String getReference() {
            return name().toLowerCase();
        }

        static ScalarFieldType getByName(String name) {
            return map.get(name);
        }

        @Override
        public String toString() {
            return getReference();
        }
    }

    /**
     * Get scalar type by its reference/name. Scalar type names are:
     *
     * <ol>
     *     <li>{@code int32}</li>
     *     <li>{@code int64}</li>
     *     <li>{@code uint32}</li>
     *     <li>{@code uint64}</li>
     *     <li>{@code sint32}</li>
     *     <li>{@code sint64}</li>
     *     <li>{@code fixed32}</li>
     *     <li>{@code fixed64}</li>
     *     <li>{@code sfixed32}</li>
     *     <li>{@code sfixed64}</li>
     *     <li>{@code float}</li>
     *     <li>{@code double}</li>
     *     <li>{@code bool}</li>
     *     <li>{@code string}</li>
     *     <li>{@code bytes}</li>
     * </ol>
     */
    static ScalarFieldType getByName(String name) {
        return Holder.getByName(name);
    }

}