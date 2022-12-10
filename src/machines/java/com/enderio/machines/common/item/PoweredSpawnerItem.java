package com.enderio.machines.common.item;

import com.enderio.api.capability.IMultiCapabilityItem;
import com.enderio.api.capability.MultiCapabilityProvider;
import com.enderio.api.capability.StoredEntityData;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.core.client.item.IAdvancedTooltipProvider;
import com.enderio.core.common.util.EntityUtil;
import com.enderio.core.common.util.TooltipUtil;
import com.enderio.machines.common.capability.EntityStorageBlockItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PoweredSpawnerItem extends BlockItem implements IMultiCapabilityItem, IAdvancedTooltipProvider {
    public PoweredSpawnerItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void addCommonTooltips(ItemStack itemStack, @Nullable Player player, List<Component> tooltips) {
        CompoundTag entityData = BlockItem.getBlockEntityData(itemStack);
        if (itemStack.hasTag()) {
            CompoundTag itemTag = itemStack.getTag();
            if (itemTag.contains("BlockEntityTag")) {
                CompoundTag blockEntityTag = itemTag.getCompound("BlockEntityTag");
                if (blockEntityTag.contains("EntityStorage")) {
                    StoredEntityData newEntity = StoredEntityData.empty();
                    newEntity.deserializeNBT(blockEntityTag.getCompound("EntityStorage"));
                    itemStack.getCapability(EIOCapabilities.ENTITY_STORAGE)
                        .ifPresent(cap -> cap.setStoredEntityData(newEntity));
                }
            }
        }

        itemStack
            .getCapability(EIOCapabilities.ENTITY_STORAGE)
            .ifPresent(entityStorage -> entityStorage
                .getStoredEntityData()
                .getEntityType()
                .ifPresent(entityType -> tooltips.add(TooltipUtil.style(Component.translatable(EntityUtil.getEntityDescriptionId(entityType))))));
    }

    @Override
    public @Nullable MultiCapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt, MultiCapabilityProvider provider) {
        provider.addSerialized(EIOCapabilities.ENTITY_STORAGE, LazyOptional.of(() -> new EntityStorageBlockItemStack(stack)));
        return provider;
    }
}