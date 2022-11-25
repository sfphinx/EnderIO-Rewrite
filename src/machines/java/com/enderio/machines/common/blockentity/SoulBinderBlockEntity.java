package com.enderio.machines.common.blockentity;

import com.enderio.EnderIO;
import com.enderio.api.capacitor.CapacitorModifier;
import com.enderio.api.capacitor.QuadraticScalable;
import com.enderio.base.common.item.misc.BrokenSpawnerItem;
import com.enderio.base.common.item.tool.SoulVialItem;
import com.enderio.base.common.tag.EIOTags;
import com.enderio.machines.common.blockentity.base.PoweredCraftingMachine;
import com.enderio.machines.common.blockentity.task.PoweredCraftingTask;
import com.enderio.machines.common.init.MachineRecipes;
import com.enderio.machines.common.io.item.MachineInventory;
import com.enderio.machines.common.io.item.MachineInventoryLayout;
import com.enderio.machines.common.menu.SoulBinderMenu;
import com.enderio.machines.common.recipe.SoulBindingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SoulBinderBlockEntity extends PoweredCraftingMachine<SoulBindingRecipe, SoulBindingRecipe.Container> {
    public static final QuadraticScalable CAPACITY = new QuadraticScalable(CapacitorModifier.ENERGY_CAPACITY, () -> 100000f);
    public static final QuadraticScalable TRANSFER = new QuadraticScalable(CapacitorModifier.ENERGY_TRANSFER, () -> 120f);
    public static final QuadraticScalable USAGE = new QuadraticScalable(CapacitorModifier.ENERGY_USE, () -> 30f);

    private final SoulBindingRecipe.Container container;
    private final FluidTank fluidTank;
    private int neededXP;

    public SoulBinderBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(MachineRecipes.SOUL_BINDING.type().get(), CAPACITY, TRANSFER, USAGE, pType, pWorldPosition, pBlockState);

        // Create the crafting inventory. Used for context in the vanilla recipe wrapper.
        fluidTank = new FluidTank(1000, f -> f.getFluid().is(EIOTags.Fluids.EXPERIENCE));
        container = new SoulBindingRecipe.Container(getInventory());
    }

    @Override
    public MachineInventoryLayout getInventoryLayout() {
        return MachineInventoryLayout.builder().inputSlot(this::validBrokenSpawner).inputSlot(this::validFilledSoulVial).outputSlot(2).capacitor().build();
    }

    private boolean validBrokenSpawner(int slot, ItemStack stack) {
        if (stack.getItem() instanceof BrokenSpawnerItem) {
            return true;
        }
        return false;
    }

    private boolean validFilledSoulVial(int slot, ItemStack stack) {
        if (stack.getItem() instanceof SoulVialItem) {
            return stack.getItem().isFoil(stack);
        }
        return false;
    }

    protected boolean acceptSlotInput(int slot, ItemStack stack) {
        // Ensure we don't break automation by inserting items that'll break the current recipe.
        var currentTask = getCurrentTask();
        if (currentTask != null) {
            var currentRecipe = currentTask.getRecipe();
            if (currentRecipe != null) {
                MachineInventory inventory = getInventory();
                ItemStack currentContents = inventory.getStackInSlot(slot);
                inventory.setStackInSlot(slot, stack);
                boolean accept = currentRecipe.matches(container, level);
                inventory.setStackInSlot(slot, currentContents);
                return accept;
            }
        }
        return true;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    public int getNeededXP() {
        return neededXP;
    }

    @Override
    protected Optional<SoulBindingRecipe> findRecipe() {
        var recipe = super.findRecipe();
        EnderIO.LOGGER.info("findRecipe: recipe: {}", recipe.toString());
        if (recipe.isPresent())
            return recipe;

        return Optional.empty();
    }

    @Override
    protected PoweredCraftingTask<SoulBindingRecipe, SoulBindingRecipe.Container> createTask(@Nullable SoulBindingRecipe recipe) {
        return new PoweredCraftingTask<>(this, getContainer(), 3, recipe) {
            @Override
            protected void takeInputs(SoulBindingRecipe recipe) {
                // Deduct ingredients
                MachineInventory inv = getInventory();
                for (int i = 0; i < 2; i++) {
                    inv.getStackInSlot(i).shrink(1);
                }
            }
        };
    }

    @Override
    protected SoulBindingRecipe.Container getContainer() {
        return container;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new SoulBinderMenu(this, pPlayerInventory, pContainerId);
    }
}
