package com.enderio.machines.client.gui.screen;

import com.enderio.EnderIO;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.core.client.gui.screen.EIOScreen;
import com.enderio.core.client.gui.widgets.EnumIconWidget;
import com.enderio.core.common.util.Vector2i;
import com.enderio.machines.client.gui.widget.EnergyWidget;
import com.enderio.machines.client.gui.widget.ExperienceWidget;
import com.enderio.machines.client.gui.widget.ProgressWidget;
import com.enderio.machines.common.lang.MachineLang;
import com.enderio.machines.common.menu.SoulBinderMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SoulBinderScreen extends EIOScreen<SoulBinderMenu> {
    public static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/soul_binder.png");

    private static Button usePlayerXP;

    public SoulBinderScreen(SoulBinderMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();

        addRenderableOnly(new EnergyWidget(this, getMenu().getBlockEntity()::getEnergyStorage, 16 + leftPos, 14 + topPos, 9, 42));

        addRenderableOnly(new ProgressWidget.LeftRight(this, () -> menu.getBlockEntity().getProgress(), getGuiLeft() + 81, getGuiTop() + 35, 24, 17, 176, 14));

        addRenderableWidget(new EnumIconWidget<>(this, leftPos + imageWidth - 8 - 12, topPos + 6, () -> menu.getBlockEntity().getRedstoneControl(),
            control -> menu.getBlockEntity().setRedstoneControl(control), EIOLang.REDSTONE_MODE));

        addRenderableOnly(new ExperienceWidget(this, getMenu().getBlockEntity()::getFluidTank, () -> getMenu().getBlockEntity().getNeededXP(), 56 + leftPos, 68 + topPos, 65, 5));

        usePlayerXP = new Button(leftPos + imageWidth - 8 - 40, topPos + 12 + 16 * 3, 20, 20, Component.literal("XP"),
            mouseButton -> usePlayerExperience());
        usePlayerXP.visible = false;

        addRenderableWidget(usePlayerXP);
    }

    protected void usePlayerExperience() {
        int xp = Minecraft.getInstance().player.experienceLevel;
        if (xp > 0 || Minecraft.getInstance().player.isCreative()) {
            // drain XP from player
            menu.getBlockEntity().usePlayerExperience();

            // TODO: how do we send a packet to remove XP from player?
        }
    }

    @Override
    protected ResourceLocation getBackgroundImage() {
        return BG_TEXTURE;
    }

    @Override
    protected Vector2i getBackgroundImageSize() {
        return new Vector2i(176, 166);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        super.renderBg(pPoseStack, pPartialTick, pMouseX, pMouseY);

        usePlayerXP.visible = menu.getBlockEntity().needsXP();
    }
}
