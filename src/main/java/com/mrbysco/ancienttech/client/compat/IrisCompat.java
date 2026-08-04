package com.mrbysco.ancienttech.client.compat;

import net.neoforged.fml.ModList;

public class IrisCompat {
	private static final boolean IRIS_LOADED =
			ModList.get().isLoaded("iris");

	public static boolean shaderPackActive() {
		return IRIS_LOADED && IrisCompatImpl.shaderPackActive();
	}

	public static boolean shadowPassActive() {
		return IRIS_LOADED && IrisCompatImpl.shadowPassActive();
	}
}
