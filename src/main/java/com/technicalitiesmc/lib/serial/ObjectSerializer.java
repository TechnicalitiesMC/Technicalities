package com.technicalitiesmc.lib.serial;

import com.technicalitiesmc.lib.util.value.Value;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ObjectSerializer {

    private final List<SerializedField> fields = new ArrayList<>();

    public ObjectSerializer(Object object, Runnable setCallback) {
        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            Serialize annotation = field.getAnnotation(Serialize.class);
            if (annotation == null) continue;
            if (field.getType() != Value.class) {
                throw new IllegalStateException("@Serialize must only be used on fields of type Value<T>. Offender: " + clazz.getName() + "." + field.getName());
            }
            Type genericFieldType = field.getGenericType();
            if (!(genericFieldType instanceof ParameterizedType)) {
                throw new IllegalStateException("@Serialize is used on a Value field without a generic type. Offender: " + clazz.getName() + "." + field.getName());
            }
            ParameterizedType parameterizedFieldType = (ParameterizedType) genericFieldType;
            Class<?> valueType = ((Class<?>) parameterizedFieldType.getActualTypeArguments()[0]);
            if (!valueType.isEnum()) {
                throw new IllegalStateException("@Serialize currently only supports enums. Offender: " + clazz.getName() + "." + field.getName());
            }

            field.setAccessible(true);

            String name = annotation.value();
            if (name.isEmpty()) name = field.getName();

            Value<?> value;
            try {
                value = ((Value<?>) field.get(object));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            if (setCallback != null) {
                value.onChanged(setCallback);
            }

            fields.add(new SerializedField(name, value, valueType));
        }
    }

    @Nonnull
    public CompoundNBT serialize() {
        if (fields.isEmpty()) return new CompoundNBT();
        CompoundNBT tag = new CompoundNBT();
        for (SerializedField field : fields) {
            tag.putInt(field.name, ((Enum<?>) field.value.get()).ordinal());
        }
        return tag;
    }

    public void deserialize(CompoundNBT tag) {
        if (fields.isEmpty()) return;
        for (SerializedField field : fields) {
            if (!tag.contains(field.name)) continue;
            int value = tag.getInt(field.name);
            field.value.set(field.enumType.getEnumConstants()[value]);
        }
    }

    private static class SerializedField {

        private final String name;
        private final Value value;
        private final Class<?> enumType;

        public SerializedField(String name, Value value, Class<?> enumType) {
            this.name = name;
            this.value = value;
            this.enumType = enumType;
        }

    }

}
