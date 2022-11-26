package com.enderio.machines.common.recipe;

import com.enderio.EnderIO;
import com.enderio.api.capability.IEntityStorage;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.init.EIOItems;
import com.enderio.base.common.item.misc.BrokenSpawnerItem;
import com.enderio.core.common.recipes.OutputStack;
import com.enderio.machines.common.init.MachineRecipes;
import com.enderio.machines.common.menu.SoulBinderMenu;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import javax.json.Json;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SoulBindingRecipe implements MachineRecipe<SoulBindingRecipe.Container> {
    private final ResourceLocation id;
    private final List<Item> outputs;
    private final List<Ingredient> inputs;
    private final int energy;
    private final ResourceLocation entityType;
    private final int experience;

    public SoulBindingRecipe(ResourceLocation id, List<Item> outputs, List<Ingredient> inputs, ResourceLocation entityType, int energy, int experience) {
        this.id = id;
        this.outputs = outputs;
        this.inputs = inputs;
        this.energy = energy;
        this.entityType = entityType;
        this.experience = experience;
    }

    @Override
    public int getEnergyCost(Container container) {
        return energy;
    }

    public int getExperienceCost() { return experience; }

    public List<OutputStack> craft(SoulBindingRecipe.Container container) {
        ItemStack filledSoulVial = container.getItem(SoulBinderMenu.FILLED_SOUL_VIAL_INPUT_SLOT);

        List<OutputStack> results = getResultStacks();

        for (int i = 0; i < results.size(); i++) {
            int finalI = i;

            if (results.get(finalI).getItem().is(EIOItems.BROKEN_SPAWNER.get())) {
                filledSoulVial.getCapability(EIOCapabilities.ENTITY_STORAGE).ifPresent(inputEntity -> {
                    results.get(finalI).getItem().getCapability(EIOCapabilities.ENTITY_STORAGE).ifPresent(resultEntity -> {
                        resultEntity.setStoredEntityData(inputEntity.getStoredEntityData());
                    });
                });
            }
        }

        return results;
    }

    @Override
    public List<OutputStack> getResultStacks() {
        List<OutputStack> guaranteedOutputs = new ArrayList<>();
        for (Item item : outputs) {
            guaranteedOutputs.add(OutputStack.of(new ItemStack(item, 1)));
        }
        return guaranteedOutputs;
    }

    @Override
    public boolean matches(Container container, Level level) {
        // Test Items in Slots
        for (int i = 0; i < inputs.size(); i++) {
            if (!inputs.get(i).test(container.getItem(i))) {
                return false;
            }
        }

        // Test if there's enough experience
        // TODO: re-enable once the XP is figured out
        /*
        if (container.getFluidTank().drain(experience, IFluidHandler.FluidAction.SIMULATE).getAmount() < experience) {
            EnderIO.LOGGER.debug("soul binding - not enough experience - FALSE");
            return false;
        }
        */


        LazyOptional<IEntityStorage> capability = container.getItem(SoulBinderMenu.FILLED_SOUL_VIAL_INPUT_SLOT).getCapability(EIOCapabilities.ENTITY_STORAGE);
        if (!capability.isPresent()) {
            return false;
        }

        // TODO: address get warnings?
        IEntityStorage storage = capability.resolve().get();
        if (storage.hasStoredEntity() && storage.getStoredEntityData().getEntityType().get().equals(entityType)) {
            return true;
        }

        return false;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MachineRecipes.SOUL_BINDING.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return MachineRecipes.SOUL_BINDING.type().get();
    }

    /**
     * The recipe container.
     * This acts as additional context.
     */
    public static class Container extends RecipeWrapper {
        public final FluidTank fluidTank;
        public Container(IItemHandlerModifiable inv, FluidTank fluidTank) {
            super(inv);
            this.fluidTank = fluidTank;
        }

        public FluidTank getFluidTank() { return fluidTank; }
    }

    public static class Serializer implements RecipeSerializer<SoulBindingRecipe> {
        @Override
        public SoulBindingRecipe fromJson(ResourceLocation recipeId, JsonObject serializedRecipe) {
            EnderIO.LOGGER.info("recipeID: {}", recipeId.toString());

            List<Item> outputs = new ArrayList<>();
            JsonArray outputsJson = serializedRecipe.getAsJsonArray("outputs");
            for (int i = 0; i < outputsJson.size(); i++) {
                JsonObject obj = outputsJson.get(i).getAsJsonObject();

                ResourceLocation id = new ResourceLocation(obj.get("item").getAsString());
                Item item = ForgeRegistries.ITEMS.getValue(id);

                // Check that the required item exists.
                if (item == null) {
                    EnderIO.LOGGER.error("Soul Binding recipe {} is missing a required output item {}", recipeId, id);
                    throw new RuntimeException("Soul Binding recipe is missing a required output item.");
                }

                outputs.add(item);
            }

            List<Ingredient> inputs = new ArrayList<>();
            JsonArray inputsJson = serializedRecipe.getAsJsonArray("inputs");
            for (JsonElement itemJson : inputsJson) {
                inputs.add(Ingredient.fromJson(itemJson));
            }

            int energy = serializedRecipe.get("energy").getAsInt();
            int experience = serializedRecipe.get("experience").getAsInt();

            ResourceLocation entityType = new ResourceLocation(serializedRecipe.get("entityType").getAsString());

            return new SoulBindingRecipe(recipeId, outputs, inputs, entityType, energy, experience);
        }

        @Nullable
        @Override
        public SoulBindingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            try {
                EnderIO.LOGGER.info("fromNetwork: recipeId: {}", recipeId);

                List<Ingredient> inputs = buffer.readCollection(ArrayList::new, Ingredient::fromNetwork);

                int energy = buffer.readInt();
                int experience = buffer.readInt();

                ResourceLocation entityType = buffer.readResourceLocation();

                List<Item> outputs = new ArrayList<>();
                int outputCount = buffer.readInt();
                for (int i = 0; i < outputCount; i++) {
                    ResourceLocation id = buffer.readResourceLocation();

                    Item item = ForgeRegistries.ITEMS.getValue(id);

                    // Check the required items are present.
                    if (item == null) {
                        EnderIO.LOGGER.error("Soul Binding recipe {} is missing a required output item {}", recipeId, id);
                        throw new RuntimeException("Soul Binding recipe is missing a required output item.");
                    }

                    outputs.add(item);
                }

                return new SoulBindingRecipe(recipeId, outputs, inputs, entityType, energy, experience);
            } catch (Exception ex) {
                EnderIO.LOGGER.error("Error reading soul binding recipe from packet.", ex);
                throw ex;
            }
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SoulBindingRecipe recipe) {
            try {
                buffer.writeCollection(recipe.inputs, (buf, ing) -> ing.toNetwork(buf));
                buffer.writeInt(recipe.energy);
                buffer.writeInt(recipe.experience);
                buffer.writeResourceLocation(recipe.entityType);

                buffer.writeInt(recipe.outputs.size());
                for (Item item : recipe.outputs) {
                    buffer.writeResourceLocation(ForgeRegistries.ITEMS.getKey(item));
                }
            } catch (Exception ex) {
                EnderIO.LOGGER.error("Error writing soul binding recipe to packet.", ex);
                throw ex;
            }
        }
    }
}
