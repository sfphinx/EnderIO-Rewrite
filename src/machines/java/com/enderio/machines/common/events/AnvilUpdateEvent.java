package com.enderio.machines.common.events;

import com.enderio.EnderIO;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.init.EIOItems;
import com.enderio.machines.common.init.MachineBlocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = EnderIO.MODID)
public class AnvilUpdateEvent {
    static class CombineRecipe {
        public final Item left;
        public final Item right;
        public final Item out;
        protected CombineRecipe(Item left, Item right, Item out){
            this.left = left;
            this.right = right;
            this.out = out;
        }
    }

    private static ArrayList<CombineRecipe> combineRecipes = new ArrayList<>();

    public static void initAnvilRecipes(){
        combineRecipes.add(new CombineRecipe(MachineBlocks.POWERED_SPAWNER.get().asItem(), EIOItems.BROKEN_SPAWNER.get(), MachineBlocks.POWERED_SPAWNER.get().asItem()));
    }

    @SubscribeEvent
    public static void handleRepair(net.minecraftforge.event.AnvilUpdateEvent event){
        combineRecipes.forEach((data) -> {
            if (event.getLeft().getItem() == data.left && event.getRight().getItem() == data.right){
                ItemStack outItem = new ItemStack(data.out, event.getLeft().getCount());

                event.getRight().getCapability(EIOCapabilities.ENTITY_STORAGE).ifPresent(brokeSpawnerEntity -> {
                    EnderIO.LOGGER.info("found entity {}", brokeSpawnerEntity.getStoredEntityData().getEntityType());
                    outItem.getCapability(EIOCapabilities.ENTITY_STORAGE).ifPresent(poweredSpawnerEntity -> poweredSpawnerEntity.setStoredEntityData(brokeSpawnerEntity.getStoredEntityData()));
                });

                event.setOutput(outItem);
                event.setCost(event.getLeft().getCount());
                event.setMaterialCost(event.getLeft().getCount());
            }
        });
    }
}
