package com.mrbysco.ancienttech.mixin.client;

import com.mrbysco.ancienttech.client.color_drain.ColorDrainRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
		targets = "net.irisshaders.iris.pipeline.IrisRenderingPipeline",
		remap = false
)
public abstract class IrisRenderingPipelineMixin {
	@Inject(method = "finalizeLevelRendering", at = @At("TAIL"))
	private void ancienttech$renderColorDrainAfterFinal(CallbackInfo ci) {
		ColorDrainRenderer.renderAfterIrisFinal();
	}
}
