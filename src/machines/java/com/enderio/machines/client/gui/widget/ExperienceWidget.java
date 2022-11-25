package com.enderio.machines.client.gui.widget;

import com.enderio.core.client.gui.widgets.EIOWidget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.function.Supplier;

// Source: https://github.com/SleepyTrousers/EnderIO-Rewrite/blob/5ef79693ab00b05b05a5d1b7f41e18562e591d5c/src/machines/java/com/enderio/machines/client/gui/widget/ExperienceWidget.java
public class ExperienceWidget extends EIOWidget {

    private final Screen displayOn;
    private final Supplier<FluidTank> getFluid;
    private final Supplier<Integer> maxXP;

    public ExperienceWidget(Screen displayOn, Supplier<FluidTank> getFluid, Supplier<Integer> maxXP, int pX, int pY, int pWidth, int pHeight) {
        super(pX, pY, pWidth, pHeight);
        this.displayOn = displayOn;
        this.getFluid = getFluid;
        this.maxXP = maxXP;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        int k = 1;
        if (maxXP.get() > 0) {
            k = (int) ((getFluid.get().getFluidAmount()/(float)maxXP.get()) * this.width);
        }
        blit(pPoseStack, this.x, this.y, this.displayOn.getBlitOffset(), 0, 64, this.width-1, this.height, 256, 256);
        blit(pPoseStack, this.x + this.width-1, this.y, this.displayOn.getBlitOffset(), 181, 64, 1, this.height, 256, 256);
        blit(pPoseStack, this.x, this.y, this.displayOn.getBlitOffset(), 0, 69, k, this.height, 256, 256);
        blit(pPoseStack, this.x + this.width-1, this.y, this.displayOn.getBlitOffset(), 181, 64, k==this.width? 1 : 0, this.height, 256, 256);


        String s = "" + maxXP.get();
        int i1 = (this.x - Minecraft.getInstance().font.width(s)) / 2;
        Minecraft.getInstance().font.draw(pPoseStack, s, (float)(i1 + 1), (float)this.y, 0);
        Minecraft.getInstance().font.draw(pPoseStack, s, (float)(i1 - 1), (float)this.y, 0);
        Minecraft.getInstance().font.draw(pPoseStack, s, (float)i1, (float)(this.y + 1), 0);
        Minecraft.getInstance().font.draw(pPoseStack, s, (float)i1, (float)(this.y - 1), 0);
        Minecraft.getInstance().font.draw(pPoseStack, s, (float)i1, (float)this.y, 8453920);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

    }
}
