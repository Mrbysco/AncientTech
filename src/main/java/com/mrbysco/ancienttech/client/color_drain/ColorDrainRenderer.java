package com.mrbysco.ancienttech.client.color_drain;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mrbysco.ancienttech.AncientTech;
import com.mrbysco.ancienttech.client.AncientPipelines;
import com.mrbysco.ancienttech.client.compat.IrisCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

@EventBusSubscriber(Dist.CLIENT)
public final class ColorDrainRenderer {
	private static final float DOME_RADIUS = 4.5F;
	private static final float CHECK_RADIUS = 4.6F;

	private static GpuTexture grabTexture;
	private static GpuTextureView grabTextureView;
	private static int grabWidth = -1;
	private static int grabHeight = -1;

	private static DrainInfoUniform drainInfo;

	private static final Matrix4f LAST_LEVEL_POSE = new Matrix4f();
	private static boolean hasLevelPose;

	private static final long FADE_DURATION = 1_000_000_000L; // 1 second
	private static long lastInside = -1L;

	@SubscribeEvent
	public static void onAfterTranslucentBlocks(RenderLevelStageEvent.AfterTranslucentBlocks event) {
		if (ColorDrainTracker.getActive().isEmpty()) return;

		rememberLevelPose(event.getPoseStack());
		if (IrisCompat.shaderPackActive()) return;

		try {
			RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
			renderDomes(event.getPoseStack().last().pose(), cameraPos(), mainTarget);
		} catch (Throwable t) {
			AncientTech.LOGGER.error("Color drain dome render failed, skipping this frame", t);
		}
	}

	@SubscribeEvent
	public static void onAfterLevel(RenderLevelStageEvent.AfterLevel event) {
		if (ColorDrainTracker.getActive().isEmpty()) return;
		if (IrisCompat.shaderPackActive()) return;

		RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
		renderFullscreenIfInside(cameraPos(), mainTarget);
	}

	/**
	 * Renders the color drain effect after Iris's final pass.
	 */
	public static void renderAfterIrisFinal() {
		if (!IrisCompat.shaderPackActive()) return;
		if (IrisCompat.shadowPassActive()) {
			hasLevelPose = false;
			return;
		}
		if (!hasLevelPose) return;

		// Consume the current-frame pose. A missed stage must never reuse it
		// during a later frame.
		Matrix4f levelPose = new Matrix4f(LAST_LEVEL_POSE);
		hasLevelPose = false;
		if (ColorDrainTracker.getActive().isEmpty()) return;

		try {
			RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
			Vec3 camPos = cameraPos();

			renderDomes(levelPose, camPos, target);
			renderFullscreenIfInside(camPos, target);
		} catch (Throwable t) {
			AncientTech.LOGGER.error(
					"Iris color drain render failed, skipping this frame", t);
		}
	}

	/**
	 * Remembers the current level pose from the given pose stack.
	 *
	 * @param stack The pose stack containing the current level pose.
	 */
	private static void rememberLevelPose(PoseStack stack) {
		LAST_LEVEL_POSE.set(stack.last().pose());
		hasLevelPose = true;
	}

	/**
	 * Gets the current camera position.
	 *
	 * @return The current camera position.
	 */
	private static Vec3 cameraPos() {
		return Minecraft.getInstance().gameRenderer.getMainCamera().position();
	}

	/**
	 * Checks if the camera is inside any active dome.
	 *
	 * @param camPos The position of the camera.
	 * @return True if the camera is inside any dome, false otherwise.
	 */
	private static boolean isCameraInsideAnyDome(Vec3 camPos) {
		for (BlockPos pos : ColorDrainTracker.getActive()) {
			if (isInsideDome(camPos, pos, CHECK_RADIUS)) return true;
		}
		return false;
	}

	/**
	 * Gets the fade factor based on the camera's position inside a dome.
	 *
	 * @param camPos The position of the camera.
	 * @return The fade factor, ranging from 0.0 (fully faded) to 1.0 (fully visible).
	 */
	private static float insideFadeFactor(Vec3 camPos) {
		long now = System.nanoTime();

		if (isCameraInsideAnyDome(camPos)) {
			lastInside = now;
			return 1.0F;
		}

		if (lastInside < 0L) return 0.0F;

		long elapsed = now - lastInside;
		if (elapsed >= FADE_DURATION) return 0.0F;

		float fade = 1.0F - (float) elapsed / FADE_DURATION;
		return Mth.clamp(fade, 0.0F, 1.0F);
	}

	/**
	 * Checks if the camera is inside a dome at the specified position.
	 *
	 * @param camPos The camera position.
	 * @param pos    The position of the dome.
	 * @param radius The radius of the dome.
	 * @return True if the camera is inside the dome, false otherwise.
	 */
	private static boolean isInsideDome(Vec3 camPos, BlockPos pos, float radius) {
		Vec3 center = Vec3.atCenterOf(pos);
		return Math.abs(camPos.x - center.x) <= radius
				&& Math.abs(camPos.y - center.y) <= radius
				&& Math.abs(camPos.z - center.z) <= radius;
	}

	/**
	 * Renders all active domes.
	 *
	 * @param levelPose The transformation matrix of the level.
	 * @param camPos    The camera position.
	 * @param target    The render target.
	 */
	private static void renderDomes(Matrix4f levelPose, Vec3 camPos, RenderTarget target) {
		copyToGrabTexture(target);
		for (BlockPos pos : ColorDrainTracker.getActive()) {
			drawDome(levelPose, pos, camPos, target);
		}
	}

	/**
	 * Renders a fullscreen effect if the camera is inside any dome.
	 *
	 * @param camPos The camera position.
	 * @param target The render target.
	 */
	private static void renderFullscreenIfInside(Vec3 camPos, RenderTarget target) {
		float fade = insideFadeFactor(camPos);
		if (fade <= 0.0F) return;

		copyToGrabTexture(target);
		drawFullscreen(target, fade);
	}

	/**
	 * Draws a dome at the specified position.
	 *
	 * @param levelPose The transformation matrix of the level.
	 * @param pos       The position of the dome.
	 * @param camPos    The camera position.
	 * @param target    The render target.
	 */
	private static void drawDome(
			Matrix4f levelPose,
			BlockPos pos,
			Vec3 camPos,
			RenderTarget target) {

		Matrix4f pose = new Matrix4f(levelPose).translate(
				(float) (pos.getX() + 0.5 - camPos.x),
				(float) (pos.getY() + 0.5 - camPos.y),
				(float) (pos.getZ() + 0.5 - camPos.z)
		);

		BufferBuilder buffer = Tesselator.getInstance().begin(
				VertexFormat.Mode.QUADS,
				DefaultVertexFormat.POSITION_TEX
		);
		emitCubeFaces(buffer, pose, DOME_RADIUS);

		MeshData mesh = buffer.build();
		if (mesh != null) drawMesh(mesh, target);
	}

	/**
	 * Emits a simple cube
	 *
	 * @param buffer The vertex buffer to write to.
	 * @param pose   The transformation matrix to apply to the vertices.
	 * @param r      The half-extent of the cube.
	 */
	private static void emitCubeFaces(VertexConsumer buffer, Matrix4f pose, float r) {
		quad(buffer, pose, new float[]{r, -r, -r}, new float[]{r, r, -r}, new float[]{r, r, r}, new float[]{r, -r, r});
		quad(buffer, pose, new float[]{-r, -r, r}, new float[]{-r, r, r}, new float[]{-r, r, -r}, new float[]{-r, -r, -r});
		quad(buffer, pose, new float[]{-r, r, -r}, new float[]{-r, r, r}, new float[]{r, r, r}, new float[]{r, r, -r});
		quad(buffer, pose, new float[]{-r, -r, r}, new float[]{-r, -r, -r}, new float[]{r, -r, -r}, new float[]{r, -r, r});
		quad(buffer, pose, new float[]{-r, -r, r}, new float[]{r, -r, r}, new float[]{r, r, r}, new float[]{-r, r, r});
		quad(buffer, pose, new float[]{r, -r, -r}, new float[]{-r, -r, -r}, new float[]{-r, r, -r}, new float[]{r, r, -r});
	}

	/**
	 * Emits a quad (four vertices) for the given vertex positions.
	 *
	 * @param buffer The vertex buffer to write to.
	 * @param pose   The transformation matrix to apply to the vertices.
	 * @param a      The first vertex position.
	 * @param b      The second vertex position.
	 * @param c      The third vertex position.
	 * @param d      The fourth vertex position.
	 */
	private static void quad(VertexConsumer buffer, Matrix4f pose, float[] a, float[] b, float[] c, float[] d) {
		buffer.addVertex(pose, a[0], a[1], a[2]).setUv(0, 0);
		buffer.addVertex(pose, b[0], b[1], b[2]).setUv(1, 0);
		buffer.addVertex(pose, c[0], c[1], c[2]).setUv(1, 1);
		buffer.addVertex(pose, d[0], d[1], d[2]).setUv(0, 1);
	}

	/**
	 * Draws a mesh using the grab texture.
	 *
	 * @param mesh       The mesh data to draw.
	 * @param mainTarget The render target to draw to.
	 */
	private static void drawMesh(MeshData mesh, RenderTarget mainTarget) {
		RenderPipeline pipeline = AncientPipelines.COLOR_DRAIN;
		GpuBuffer vertices = pipeline.getVertexFormat().uploadImmediateVertexBuffer(mesh.vertexBuffer());

		GpuBuffer indices;
		VertexFormat.IndexType indexType;
		if (mesh.indexBuffer() == null) {
			RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(mesh.drawState().mode());
			indices = autoIndices.getBuffer(mesh.drawState().indexCount());
			indexType = autoIndices.type();
		} else {
			indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(mesh.indexBuffer());
			indexType = mesh.drawState().indexType();
		}

		var drainInfoSlice = drainInfo().update(1.0F, 1.0F);
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
				.writeTransform(RenderSystem.getModelViewMatrix(),
						new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(),
						TextureTransform.DEFAULT_TEXTURING.getMatrix()
				);


		try (RenderPass renderPass = RenderSystem.getDevice()
				.createCommandEncoder()
				.createRenderPass(() -> "Color drain dome", mainTarget.getColorTextureView(), OptionalInt.empty(),
						mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
			renderPass.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);
			renderPass.setVertexBuffer(0, vertices);
			renderPass.bindTexture("GrabSampler", grabTextureView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
			renderPass.setUniform("DrainInfo", drainInfoSlice);
			renderPass.setIndexBuffer(indices, indexType);
			renderPass.drawIndexed(0, 0, mesh.drawState().indexCount(), 1);
		}

		mesh.close();
	}

	/**
	 * Draws a fullscreen quad using the grab texture.
	 *
	 * @param target The render target to draw to.
	 */
	private static void drawFullscreen(RenderTarget target, float grayscale) {
		var drainInfoSlice = drainInfo().update(grayscale, 1.0F);
		try (RenderPass renderPass = RenderSystem.getDevice()
				.createCommandEncoder()
				.createRenderPass(() -> "Color drain fullscreen", target.getColorTextureView(), OptionalInt.empty())) {
			renderPass.setPipeline(AncientPipelines.COLOR_DRAIN_FULLSCREEN);
			renderPass.bindTexture("GrabSampler", grabTextureView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
			renderPass.setUniform("DrainInfo", drainInfoSlice);
			renderPass.draw(0, 3);
		} catch (Throwable t) {
			AncientTech.LOGGER.error("Color drain fullscreen render failed, skipping this frame", t);
		}
	}

	/**
	 * Gets the drain info uniform, creating it if necessary.
	 * This ensures the uniform is not loaded on class load.
	 *
	 * @return The drain info uniform.
	 */
	private static DrainInfoUniform drainInfo() {
		if (drainInfo == null) {
			drainInfo = new DrainInfoUniform();
		}
		return drainInfo;
	}

	/**
	 * Ensures that the grab texture is created and matches the specified dimensions.
	 *
	 * @param width  The width of the grab texture.
	 * @param height The height of the grab texture.
	 */
	private static void ensureGrabTexture(int width, int height) {
		if (grabTexture == null || grabWidth != width || grabHeight != height) {
			if (grabTexture != null) grabTexture.close();
			if (grabTextureView != null) grabTextureView.close();

			grabTexture = RenderSystem.getDevice()
					.createTexture(() -> "Color Drain Grab", 15, TextureFormat.RGBA8, width, height, 1, 1);
			grabTextureView = RenderSystem.getDevice().createTextureView(grabTexture);
			grabWidth = width;
			grabHeight = height;
		}
	}

	/**
	 * Copies the main render target to the grab texture.
	 *
	 * @param mainTarget The main render target.
	 */
	private static void copyToGrabTexture(RenderTarget mainTarget) {
		ensureGrabTexture(mainTarget.width, mainTarget.height);
		RenderSystem.getDevice()
				.createCommandEncoder()
				.copyTextureToTexture(
						mainTarget.getColorTexture(),
						grabTexture,
						0, 0, 0, 0, 0,
						mainTarget.width,
						mainTarget.height
				);
	}
}