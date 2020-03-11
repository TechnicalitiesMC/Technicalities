package com.technicalitiesmc.lib.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class CollectionUtils {

    public static <K extends Enum<K>, V> EnumMap<K, V> newFilledEnumMap(Class<K> keyType, Supplier<V> valueSupplier) {
        return newFilledEnumMap(keyType, k -> valueSupplier.get());
    }

    public static <K extends Enum<K>, V> EnumMap<K, V> newFilledEnumMap(Class<K> keyType, Function<K, V> valueMapper) {
        EnumMap<K, V> map = new EnumMap<>(keyType);
        for (K key : keyType.getEnumConstants()) {
            map.put(key, valueMapper.apply(key));
        }
        return map;
    }


    public static <E extends Enum<E>> EnumSet<E> newFilledEnumSet(Class<E> type, Predicate<E> filter) {
        EnumSet<E> set = EnumSet.noneOf(type);
        for (E value : type.getEnumConstants()) {
            if (filter.test(value)) {
                set.add(value);
            }
        }
        return set;
    }

    public static <E> List<E> replaceOrDeleteAll(List<E> list, UnaryOperator<E> operator) {
        ListIterator<E> iterator = list.listIterator();
        while (iterator.hasNext()) {
            E currentStack = iterator.next();
            E newStack = operator.apply(currentStack);
            if (newStack == null) {
                iterator.remove();
            } else {
                iterator.set(newStack);
            }
        }
        return list;
    }

    public static <E> E random(List<E> list) {
        if (list.isEmpty()) return null;
        int idx = (int) (list.size() * Math.random());
        return list.get(idx);
    }

    public static <E> E random(List<E> list, Random random) {
        if (list.isEmpty()) return null;
        int idx = random.nextInt(list.size());
        return list.get(idx);
    }

}
