package com.mrbysco.ancienttech.client.color_drain;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;

public final class DrainInfoUniform implements AutoCloseable {
	public static final int DRAIN_UBO_SIZE = new Std140SizeCalculator().putFloat().putFloat().get();

	private final GpuBuffer buffer;

	public DrainInfoUniform() {
		this.buffer = RenderSystem.getDevice().createBuffer(() -> "Disco Drain UBO", 130, DRAIN_UBO_SIZE);
	}

	public GpuBufferSlice update(float grayscale, float brightness) {
		try (GpuBuffer.MappedView view = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.buffer, false, true)) {
			Std140Builder.intoBuffer(view.data())
					.putFloat(grayscale)
					.putFloat(brightness);
		}
		return this.buffer.slice(0L, DRAIN_UBO_SIZE);
	}

	@Override
	public void close() {
		this.buffer.close();
	}
}