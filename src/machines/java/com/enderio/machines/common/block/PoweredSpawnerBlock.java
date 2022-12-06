package com.enderio.machines.common.block;

import com.enderio.EnderIO;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.machines.common.blockentity.PoweredSpawnerBlockEntity;
import com.enderio.machines.common.blockentity.base.MachineBlockEntity;
import com.enderio.machines.common.init.MachineBlockEntities;
import com.enderio.machines.common.init.MachineBlocks;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PoweredSpawnerBlock extends ProgressMachineBlock implements EntityBlock {
    public PoweredSpawnerBlock(Properties properties, BlockEntityEntry<? extends MachineBlockEntity> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        EnderIO.LOGGER.debug("<PoweredSpawnerBlock>[playerWillDestroy] ");

        BlockEntity pBlockEntity = pLevel.getBlockEntity(pPos);
        if (pBlockEntity instanceof PoweredSpawnerBlockEntity spawner) {
            EnderIO.LOGGER.debug("<PoweredSpawnerBlock>[playerWillDestroy] entityStorage: {}", spawner.getEntityData().getEntityType());
            ItemStack itemstack = new ItemStack(MachineBlocks.POWERED_SPAWNER.get());

            itemstack.getCapability(EIOCapabilities.ENTITY_STORAGE).ifPresent(s -> {
                EnderIO.LOGGER.debug("<PoweredSpawnerBlock>[playerWillDestroy] setting entity data: {}", spawner.getEntityData().getEntityType());
                s.setStoredEntityData(spawner.getEntityData());
            });

            pBlockEntity.saveToItem(itemstack);

            ItemEntity itementity = new ItemEntity(pLevel, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, itemstack);
            itementity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(itementity);
        }

        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        if (pLevel.getBlockEntity(pPos) instanceof PoweredSpawnerBlockEntity spawner) {
            pStack.getCapability(EIOCapabilities.ENTITY_STORAGE).ifPresent(stackES -> {
                EnderIO.LOGGER.debug("<PoweredSpawnerBlock>[setPlacedBy] entity from stack: {}", stackES.getStoredEntityData().getEntityType());
                spawner.setEntityType(stackES.getStoredEntityData());
                spawner.setChanged();
            });
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return MachineBlockEntities.POWERED_SPAWNER.create(pPos, pState);
    }
}
