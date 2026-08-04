package com.mrbysco.ancienttech.client.color_drain;

import net.minecraft.core.BlockPos;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ColorDrainTracker {
	private static final Set<BlockPos> ACTIVE = ConcurrentHashMap.newKeySet();

	public static void add(BlockPos pos) {
		ACTIVE.add(pos.immutable());
	}

	public static void remove(BlockPos pos) {
		ACTIVE.remove(pos);
	}

	public static Set<BlockPos> getActive() {
		return ACTIVE;
	}
}
