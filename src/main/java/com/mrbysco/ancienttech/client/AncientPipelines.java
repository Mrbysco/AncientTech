package com.mrbysco.ancienttech.client;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mrbysco.ancienttech.AncientTech;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(Dist.CLIENT)
public class AncientPipelines {
	public static final RenderPipeline COLOR_DRAIN = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
			.withLocation(AncientTech.modLoc("pipeline/color_drain"))
			.withVertexShader("core/position_tex")
			.withFragmentShader(AncientTech.modLoc("core/color_drain"))
			.withSampler("GrabSampler")
			.withUniform("DrainInfo", UniformType.UNIFORM_BUFFER)
			.withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
			.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false, -1.0F, -10.0F))
			.build();

	public static final RenderPipeline COLOR_DRAIN_FULLSCREEN = RenderPipeline.builder()
			.withLocation(AncientTech.modLoc("pipeline/color_drain_fullscreen"))
			.withVertexShader("core/screenquad")
			.withFragmentShader(AncientTech.modLoc("core/color_drain_fullscreen"))
			.withSampler("GrabSampler")
			.withUniform("DrainInfo", UniformType.UNIFORM_BUFFER)
			.withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
			.build();

	@SubscribeEvent
	public static void onRegisterPipeline(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(COLOR_DRAIN);
		event.registerPipeline(COLOR_DRAIN_FULLSCREEN);
	}
}
