package com.enderio.machines.common.menu;

import com.enderio.machines.common.blockentity.SoulBinderBlockEntity;
import com.enderio.machines.common.init.MachineMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

public class SoulBinderMenu extends MachineMenu<SoulBinderBlockEntity> {

    public static final int BROKEN_SPAWNER_INPUT_SLOT = 0;
    public static final int FILLED_SOUL_VIAL_INPUT_SLOT = 1;
    public static final int BROKEN_SPAWNER_OUTPUT_SLOT = 2;
    public static final int EMPTY_SOUL_VIAL_OUTPUT_SLOT = 3;

    public SoulBinderMenu(@Nullable SoulBinderBlockEntity blockEntity, Inventory inventory, int pContainerId) {
        super(blockEntity, inventory, MachineMenus.SOUL_BINDER.get(), pContainerId);
        if (blockEntity != null) {
            // Capacitor slot
            if (blockEntity.requiresCapacitor()) {
                addSlot(new MachineSlot(blockEntity.getInventory(), 4, 12, 60));
            }

            addSlot(new MachineSlot(blockEntity.getInventory(), BROKEN_SPAWNER_INPUT_SLOT, 38, 34));
            addSlot(new MachineSlot(blockEntity.getInventory(), FILLED_SOUL_VIAL_INPUT_SLOT, 59, 34));
            addSlot(new MachineSlot(blockEntity.getInventory(), BROKEN_SPAWNER_OUTPUT_SLOT, 112, 34));
            addSlot(new MachineSlot(blockEntity.getInventory(), EMPTY_SOUL_VIAL_OUTPUT_SLOT, 134, 34));

        }
        addInventorySlots(8,84);
    }

    public static SoulBinderMenu factory(@Nullable MenuType<SoulBinderMenu> pMenuType, int pContainerId, Inventory inventory, FriendlyByteBuf buf) {
        BlockEntity entity = inventory.player.level.getBlockEntity(buf.readBlockPos());
        if (entity instanceof SoulBinderBlockEntity castBlockEntity)
            return new SoulBinderMenu(castBlockEntity, inventory, pContainerId);
        LogManager.getLogger().warn("couldn't find BlockEntity");
        return new SoulBinderMenu(null, inventory, pContainerId);
    }
}
