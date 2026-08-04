package com.mrbysco.ancienttech.mixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSetup.RenderSetupBuilder.class)
public abstract class ItemCutoutIrisSamplerMixin {
	@Shadow
	@Final
	private RenderPipeline pipeline;

	@Inject(method = "createRenderSetup", at = @At("HEAD"))
	private void ancienttech$bindItemOverlayForIris(
			CallbackInfoReturnable<RenderSetup> callback) {
		if (ModList.get().isLoaded("iris")
				&& this.pipeline == RenderPipelines.ITEM_CUTOUT) {
			((RenderSetup.RenderSetupBuilder) (Object) this).useOverlay();
		}
	}
}
