package com.enderio.machines.data.recipes;

import com.enderio.EnderIO;
import com.enderio.base.common.init.EIOItems;
import com.enderio.base.common.util.EntityCaptureUtils;
import com.enderio.core.data.recipes.EnderRecipeProvider;
import com.enderio.machines.common.init.MachineRecipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class SoulBindingRecipeProvider extends EnderRecipeProvider {
    public SoulBindingRecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> pFinishedRecipeConsumer) {
        for (ResourceLocation entity : EntityCaptureUtils.getCapturableEntities()) {
            build(EIOItems.BROKEN_SPAWNER.get(), List.of(
                Ingredient.of(EIOItems.BROKEN_SPAWNER.get()), Ingredient.of(EIOItems.FILLED_SOUL_VIAL.get())
            ), entity,5000, pFinishedRecipeConsumer);
        }
    }

    protected void build(Item output, List<Ingredient> inputs, ResourceLocation entity, int energy, Consumer<FinishedRecipe> finishedRecipeConsumer) {
        // TODO: do not like the replace function being used
        finishedRecipeConsumer.accept(new FinishedSoulBindingRecipe(EnderIO.loc("soul_binding/" + ForgeRegistries.ITEMS.getKey(output).getPath() + "/" + entity.getPath().replace(":", "_")), output, inputs, entity, energy));
    }

    protected static class FinishedSoulBindingRecipe extends EnderFinishedRecipe {

        private final Item output;
        private final List<Ingredient> inputs;
        private final int energy;
        private final ResourceLocation entity;

        public FinishedSoulBindingRecipe(ResourceLocation id, Item output, List<Ingredient> inputs, ResourceLocation entity, int energy) {
            super(id);
            this.output = output;
            this.inputs = inputs;
            this.energy = energy;
            this.entity = entity;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("output", ForgeRegistries.ITEMS.getKey(output).toString());

            JsonArray inputsArray = new JsonArray();
            for (Ingredient input : inputs) {
                inputsArray.add(input.toJson());
            }
            json.add("inputs", inputsArray);
            json.addProperty("energy", energy);
            json.addProperty("entity", entity.toString());

            super.serializeRecipeData(json);
        }

        @Override
        protected Set<String> getModDependencies() {
            Set<String> mods = new HashSet<>();
            // TODO: 1.19: Ingredient#getItems cannot be called during datagen. Needs a new solution.
            //            inputs.stream().map(ing -> Arrays.stream(ing.getItems()).map(item -> mods.add(ForgeRegistries.ITEMS.getKey(item.getItem()).getNamespace())));
            mods.add(ForgeRegistries.ITEMS.getKey(output).getNamespace());
            return mods;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return MachineRecipes.SOUL_BINDING.serializer().get();
        }
    }
}
