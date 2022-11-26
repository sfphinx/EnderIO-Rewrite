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

import javax.json.Json;
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
            build(List.of(EIOItems.BROKEN_SPAWNER.get(), EIOItems.EMPTY_SOUL_VIAL.get()), List.of(
                Ingredient.of(EIOItems.BROKEN_SPAWNER.get()), Ingredient.of(EIOItems.FILLED_SOUL_VIAL.get())
            ), entity,100000, 10, pFinishedRecipeConsumer);
        }
    }

    protected void build(List<Item> outputs, List<Ingredient> inputs, ResourceLocation entity, int energy, int experience, Consumer<FinishedRecipe> finishedRecipeConsumer) {
        // TODO: do not like the replace function being used
        finishedRecipeConsumer.accept(new FinishedSoulBindingRecipe(EnderIO.loc("soul_binding/" + entity.getPath().replace(":", "_")), outputs, inputs, entity, energy, experience));
    }

    protected static class FinishedSoulBindingRecipe extends EnderFinishedRecipe {

        private final List<Item> outputs;
        private final List<Ingredient> inputs;
        private final int energy;
        private final ResourceLocation entityType;
        private final int experience;

        public FinishedSoulBindingRecipe(ResourceLocation id, List<Item> outputs, List<Ingredient> inputs, ResourceLocation entityType, int energy, int experience) {
            super(id);
            this.outputs = outputs;
            this.inputs = inputs;
            this.energy = energy;
            this.entityType = entityType;
            this.experience = experience;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonArray inputsArray = new JsonArray();
            for (Ingredient input : inputs) {
                inputsArray.add(input.toJson());
            }
            json.add("inputs", inputsArray);
            json.addProperty("energy", energy);
            json.addProperty("entityType", entityType.toString());
            json.addProperty("experience", experience);

            JsonArray outputsArray = new JsonArray();
            for (Item output : outputs) {
                JsonObject out = new JsonObject();
                out.addProperty("item", ForgeRegistries.ITEMS.getKey(output).toString());
                outputsArray.add(out);
            }
            json.add("outputs", outputsArray);

            super.serializeRecipeData(json);
        }

        @Override
        protected Set<String> getModDependencies() {
            Set<String> mods = new HashSet<>();
            return mods;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return MachineRecipes.SOUL_BINDING.serializer().get();
        }
    }
}
