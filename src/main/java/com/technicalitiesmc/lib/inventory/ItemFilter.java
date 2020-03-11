package com.technicalitiesmc.lib.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.IntBinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ItemFilter {

    @Nonnull
    static Prototype exactly(int amt) {
        return new Prototype(AmountMatchMode.EXACTLY, amt);
    }

    @Nonnull
    static Prototype atLeast(int amt) {
        return new Prototype(AmountMatchMode.AT_LEAST, amt);
    }

    @Nonnull
    static Prototype atMost(int amt) {
        return new Prototype(AmountMatchMode.AT_MOST, amt);
    }

    @Nonnull
    static ItemFilter combining(ItemFilter... filters) {
        return combining(Arrays.asList(filters));
    }

    @Nonnull
    static ItemFilter combining(Collection<ItemFilter> filters) {
        for (ItemFilter filter : filters) {
            if (filter instanceof Multi) {
                return new Multi(
                    filters.stream()
                        .flatMap(f -> f instanceof Multi ? ((Multi) f).filters.stream() : Stream.of(f))
                        .collect(Collectors.toList())
                );
            }
            if (!(filter instanceof Simple)) {
                throw new IllegalArgumentException("Only simple and combined filters are allowed to be combined.");
            }
        }
        return new Multi(filters);
    }

    boolean test(ItemStack stack);

    int getMatchedAmount(ItemStack stack);

    enum AmountMatchMode {
        EXACTLY((in, ref) -> in == ref ? ref : 0),
        AT_LEAST((in, ref) -> in >= ref ? in : 0),
        AT_MOST(Math::min);

        private final IntBinaryOperator test;

        AmountMatchMode(IntBinaryOperator test) {
            this.test = test;
        }

        public int test(int amount, int reference) {
            return test.applyAsInt(amount, reference);
        }

    }

    final class Prototype {

        private final AmountMatchMode mode;
        private final int amount;

        private Prototype(AmountMatchMode mode, int amount) {
            this.mode = mode;
            this.amount = amount;
        }

        @Nonnull
        public ItemFilter ofAnyItem() {
            return matching(s -> true);
        }

        @Nonnull
        public ItemFilter of(ItemStack stack) {
            return matching(s -> ItemHandlerHelper.canItemStacksStack(stack, s));
        }

        @Nonnull
        public ItemFilter in(ItemSet set) {
            return matching(set::contains);
        }

        @Nonnull
        public ItemFilter notIn(ItemSet set) {
            return matching(s -> !set.contains(s));
        }

        @Nonnull
        public ItemFilter matching(Predicate<ItemStack> predicate) {
            return new Simple(mode, amount, predicate);
        }

    }

    final class Simple implements ItemFilter {

        private final AmountMatchMode mode;
        private final int amount;
        private final Predicate<ItemStack> predicate;

        private Simple(AmountMatchMode mode, int amount, Predicate<ItemStack> predicate) {
            this.mode = mode;
            this.amount = amount;
            this.predicate = predicate;
        }

        @Override
        public boolean test(ItemStack stack) {
            return predicate.test(stack);
        }

        @Override
        public int getMatchedAmount(ItemStack stack) {
            return mode.test.applyAsInt(stack.getCount(), amount);
        }

        public AmountMatchMode getMode() {
            return mode;
        }

        public int getAmount() {
            return amount;
        }

    }

    final class Multi implements ItemFilter {

        private final Collection<ItemFilter> filters;

        private Multi(Collection<ItemFilter> filters) {
            this.filters = filters;
        }

        @Override
        public boolean test(ItemStack stack) {
            for (ItemFilter filter : filters) {
                if (filter.test(stack)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getMatchedAmount(ItemStack stack) {
            for (ItemFilter filter : filters) {
                if (filter.test(stack)) {
                    return filter.getMatchedAmount(stack);
                }
            }
            return 0;
        }

        @Nullable
        public ItemFilter.Simple getMatchingFilter(ItemStack stack) {
            for (ItemFilter filter : filters) {
                if (filter.test(stack)) {
                    return (Simple) filter;
                }
            }
            return null;
        }

    }

}
