package com.enderio.machines.common.blockentity;

import com.enderio.api.capability.StoredEntityData;
import com.enderio.api.capacitor.CapacitorModifier;
import com.enderio.api.capacitor.QuadraticScalable;
import com.enderio.base.common.capability.EntityStorage;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.machines.common.blockentity.base.PoweredTaskMachineEntity;
import com.enderio.machines.common.blockentity.task.SpawnTask;
import com.enderio.machines.common.io.item.MachineInventoryLayout;
import com.enderio.machines.common.menu.PoweredSpawnerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PoweredSpawnerBlockEntity extends PoweredTaskMachineEntity<SpawnTask> {

    public static final QuadraticScalable CAPACITY = new QuadraticScalable(CapacitorModifier.ENERGY_CAPACITY, () -> 100000f);
    public static final QuadraticScalable TRANSFER = new QuadraticScalable(CapacitorModifier.ENERGY_TRANSFER, () -> 120f);
    public static final QuadraticScalable USAGE = new QuadraticScalable(CapacitorModifier.ENERGY_USE, () -> 30f);

    private EntityStorage entityStorage = new EntityStorage();
    private final LazyOptional<EntityStorage> entityStorageLazy = LazyOptional.of(() -> entityStorage);

    private int range = 3;

    public PoweredSpawnerBlockEntity(BlockEntityType type, BlockPos worldPosition, BlockState blockState) {
        super(CAPACITY, TRANSFER, USAGE, type, worldPosition, blockState);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new PoweredSpawnerMenu(this, pPlayerInventory, pContainerId);
    }

    @Nullable
    @Override
    protected SpawnTask getNewTask() {
        return createTask();
    }

    @Nullable
    @Override
    protected SpawnTask loadTask(CompoundTag nbt) {
        SpawnTask task = createTask();
        task.deserializeNBT(nbt);
        return task;
    }

    private SpawnTask createTask() {
        return new SpawnTask(this, this.getEnergyStorage());
    }

    @Override
    public MachineInventoryLayout getInventoryLayout() {
        return MachineInventoryLayout.builder().capacitor().build();
    }

    public AABB getRange() {
        return new AABB(this.getBlockPos()).inflate(range);
    }

    public EntityType<? extends Entity> getEntityType() {
        return Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getValue(entityStorage.getStoredEntityData().getEntityType().orElse(new ResourceLocation("pig"))));
    }

    public void setEntityType(StoredEntityData entity) {
        entityStorage.setStoredEntityData(entity);
        setChanged();
    }

    public StoredEntityData getEntityData() {
        return entityStorage.getStoredEntityData();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        entityStorageLazy.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == EIOCapabilities.ENTITY_STORAGE) {
            return this.entityStorageLazy.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void load(CompoundTag pTag) {
        entityStorage.deserializeNBT(pTag.getCompound(entityStorage.getSerializedName()));
        super.load(pTag);
    }

    @Override
    public void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.put(entityStorage.getSerializedName(), entityStorage.serializeNBT());
    }

    // region Networking

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        var tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    @Override
    public void saveToItem(ItemStack pStack) {
        pStack.getCapability(EIOCapabilities.ENTITY_STORAGE).ifPresent(s -> {
            s.setStoredEntityData(getEntityData());
        });

        super.saveToItem(pStack);


    }
}
