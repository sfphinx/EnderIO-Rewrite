package com.enderio.machines.common.recipe;

import com.enderio.EnderIO;
import com.enderio.api.capability.StoredEntityData;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.init.EIOItems;
import com.enderio.core.common.recipes.CountedIngredient;
import com.enderio.core.common.recipes.OutputStack;
import com.enderio.machines.common.init.MachineRecipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SoulBindingRecipe implements MachineRecipe<SoulBindingRecipe.Container> {
    private final ResourceLocation id;
    private final Item output;
    private final List<Ingredient> inputs;
    private final int energy;
    private final ResourceLocation entity;

    public SoulBindingRecipe(ResourceLocation id, Item output, List<Ingredient> inputs, ResourceLocation entity, int energy) {
        this.id = id;
        this.output = output;
        this.inputs = inputs;
        this.energy = energy;
        this.entity = entity;
    }

    @Override
    public int getEnergyCost(Container container) {
        return energy;
    }

    public List<OutputStack> craft(SoulBindingRecipe.Container container) {
        return getResultStacks();
    }

    @Override
    public List<OutputStack> getResultStacks() {
        ItemStack result = new ItemStack(output, 1);

        EnderIO.LOGGER.info("getResultStacks(): entity: {}", entity.toString());

        result
            .getCapability(EIOCapabilities.ENTITY_STORAGE)
            .ifPresent(storage ->
                storage.setStoredEntityData(StoredEntityData.of(entity))
            );

        return List.of(OutputStack.of(result));
    }

    @Override
    public boolean matches(Container pContainer, Level pLevel) {
        for (int i = 0; i < inputs.size(); i++) {
            if (!inputs.get(i).test(pContainer.getItem(i)))
                return false;
        }
        return true;
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
        private int inputsTaken;

        public Container(IItemHandlerModifiable inv) {
            super(inv);
        }

        public int getInputsTaken() {
            return inputsTaken;
        }

        public void setInputsTaken(int inputsTaken) {
            this.inputsTaken = inputsTaken;
        }
    }

    public static class Serializer implements RecipeSerializer<SoulBindingRecipe> {
        @Override
        public SoulBindingRecipe fromJson(ResourceLocation recipeId, JsonObject serializedRecipe) {
            EnderIO.LOGGER.info("recipeID: {}", recipeId.toString());
            ResourceLocation id = new ResourceLocation(serializedRecipe.get("output").getAsString());
            Item output = ForgeRegistries.ITEMS.getValue(id);
            if (output == null) {
                EnderIO.LOGGER.error("Soul Binding recipe {} tried to load missing item {}", recipeId, id);
                throw new ResourceLocationException("Item not found for soul binding recipe.");
            }

            List<Ingredient> inputs = new ArrayList<>();
            JsonArray inputsJson = serializedRecipe.getAsJsonArray("inputs");
            for (JsonElement itemJson : inputsJson) {
                inputs.add(Ingredient.fromJson(itemJson));
            }

            int energy = serializedRecipe.get("energy").getAsInt();

            ResourceLocation entity = new ResourceLocation(serializedRecipe.get("entity").getAsString());

            EnderIO.LOGGER.info("fromJson: entity: {}", entity.toString());

            return new SoulBindingRecipe(recipeId, output, inputs, entity, energy);
        }

        @Nullable
        @Override
        public SoulBindingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            try {
                EnderIO.LOGGER.info("fromNetwork: recipeId: {}", recipeId);
                ResourceLocation outputId = buffer.readResourceLocation();
                Item output = ForgeRegistries.ITEMS.getValue(outputId);
                if (output == null) {
                    throw new ResourceLocationException("The output of recipe " + recipeId + " does not exist.");
                }

                List<Ingredient> inputs = buffer.readCollection(ArrayList::new, Ingredient::fromNetwork);

                int energy = buffer.readInt();

                ResourceLocation entity = buffer.readResourceLocation();

                return new SoulBindingRecipe(recipeId, output, inputs, entity, energy);
            } catch (Exception ex) {
                EnderIO.LOGGER.error("Error reading soul binding recipe from packet.", ex);
                throw ex;
            }
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SoulBindingRecipe recipe) {
            try {
                buffer.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(recipe.output)));
                buffer.writeCollection(recipe.inputs, (buf, ing) -> ing.toNetwork(buf));
                buffer.writeInt(recipe.energy);
                buffer.writeResourceLocation(recipe.entity);
            } catch (Exception ex) {
                EnderIO.LOGGER.error("Error writing soul binding recipe to packet.", ex);
                throw ex;
            }
        }
    }
}
