package com.technicalitiesmc.base.container;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.lib.container.TKContainer;
import com.technicalitiesmc.lib.container.TKContainerAdapter;
import com.technicalitiesmc.lib.inventory.Inventory;
import com.technicalitiesmc.lib.inventory.SimpleInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Optional;

// TODO: Refill grid with items from storage
public class WorkbenchContainer extends TKContainer {

    public static final ContainerType<TKContainerAdapter> TYPE = TKContainerAdapter.typeOf(WorkbenchContainer::new);
    public static final TranslationTextComponent NAME = new TranslationTextComponent("container." + Technicalities.MODID + ".workbench");

    private static final int STORAGE_ROWS = 2;
    private static final int STORAGE_COLUMNS = 9;
    public static final int STORAGE_SIZE = STORAGE_COLUMNS * STORAGE_ROWS;

    private static final int GRID_ROWS = 3;
    private static final int GRID_COLUMNS = 3;
    public static final int GRID_SIZE = GRID_COLUMNS * GRID_ROWS;

    private final PlayerEntity player;
    private final CustomCraftingInventory craftingInventory;
    private final CraftResultInventory gridOutput = new CraftResultInventory();

    public WorkbenchContainer(int windowId, PlayerInventory playerInventory, Inventory storageInv, Inventory gridInv) {
        super(TYPE, windowId);
        this.player = playerInventory.player;
        this.craftingInventory = new CustomCraftingInventory(gridInv);

        Region grid = addSlots(31, 18, GRID_ROWS, GRID_COLUMNS, gridInv).onChanged(s -> refreshGridOutput());
        addSlot(new CraftingResultSlot(
            playerInventory.player,
            craftingInventory,
            gridOutput,
            0,
            125, 36
        ) {
            @Override
            public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
                ItemStack result = super.onTake(thePlayer, stack);
                craftingInventory.updateInventory();
                refreshGridOutput();
                return result;
            }
        });
        Region storage = addSlots(8, 77, STORAGE_ROWS, STORAGE_COLUMNS, storageInv);
        Region playerInv = addSlots(8, 127, 3, 9, playerInventory, 9);
        Region playerHotbar = addSlots(8, 185, 1, 9, playerInventory, 0);

        addShiftClickTargets(grid, storage, playerHotbar.reversed(), playerInv.reversed());
        addShiftClickTargets(storage, playerHotbar.reversed(), playerInv.reversed());
        addShiftClickTargets(playerInv, storage);
        addShiftClickTargets(playerHotbar, storage);

        refreshGridOutput();
    }

    private WorkbenchContainer(int windowId, PlayerInventory playerInventory) {
        this(windowId, playerInventory, new SimpleInventory(STORAGE_SIZE), new SimpleInventory(GRID_SIZE));
    }

    private void refreshGridOutput() {
        if (player.world.isRemote) return;

        craftingInventory.updateGrid();

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) this.player;
        ItemStack itemstack = ItemStack.EMPTY;
        Optional<ICraftingRecipe> maybeRecipe = player.world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, craftingInventory, player.world);
        if (maybeRecipe.isPresent()) {
            ICraftingRecipe recipe = maybeRecipe.get();
            if (gridOutput.canUseRecipe(this.player.world, serverPlayer, recipe)) {
                itemstack = recipe.getCraftingResult(craftingInventory);
            }
        }

        gridOutput.setInventorySlotContents(0, itemstack);
        serverPlayer.connection.sendPacket(new SSetSlotPacket(asVanillaContainer().windowId, 9, itemstack));
    }

    private class CustomCraftingInventory extends CraftingInventory {

        private final Inventory gridInv;

        public CustomCraftingInventory(Inventory gridInv) {
            super(WorkbenchContainer.this.asVanillaContainer(), GRID_COLUMNS, GRID_ROWS);
            this.gridInv = gridInv;
        }

        private void updateGrid() {
            for (int i = 0; i < GRID_SIZE; i++) {
                setInventorySlotContents(i, gridInv.get(i));
            }
        }

        private void updateInventory() {
            for (int i = 0; i < GRID_SIZE; i++) {
                gridInv.set(i, getStackInSlot(i));
            }
        }

    }

}
