package com.technicalitiesmc.lib.inventory;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class DroppingItemHandler implements IItemHandler {

    private final IWorld world;
    private final ItemEntity[] entities;
    private final Consumer<ItemEntity> entityConfigurator;

    public DroppingItemHandler(IWorld world, int slots, Consumer<ItemEntity> entityConfigurator) {
        this.world = world;
        this.entities = new ItemEntity[slots];
        this.entityConfigurator = entityConfigurator;
    }

    @Override
    public int getSlots() {
        return entities.length;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        ItemEntity entity = entities[slot];
        return entity != null && entity.isAlive() ? entity.getItem() : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (entities[slot] != null) return stack;
        if (simulate) return ItemStack.EMPTY;
        ItemEntity entity = new ItemEntity(world.getWorld(), 0, 0, 0, stack.copy());
        entity.setDefaultPickupDelay();
        entityConfigurator.accept(entity);
        world.addEntity(entity);
        entities[slot] = entity;
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return true;
    }

    public static Consumer<ItemEntity> dropInFront(BlockPos pos, Direction front) {
        return entity -> {
            entity.setPosition(
                pos.getX() + 0.5 + front.getXOffset() * 0.75,
                pos.getY() + 0.5 + front.getYOffset() * 0.75,
                pos.getZ() + 0.5 + front.getZOffset() * 0.75
            );
            entity.setMotion(entity.getMotion().scale(0.375F).add(
                front.getXOffset() * 0.0625F,
                front.getYOffset() * 0.0625F,
                front.getZOffset() * 0.0625F
            ));
        };
    }

}
