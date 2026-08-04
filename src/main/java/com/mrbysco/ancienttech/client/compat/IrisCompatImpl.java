package com.mrbysco.ancienttech.client.compat;

import net.irisshaders.iris.api.v0.IrisApi;

public class IrisCompatImpl {
	static boolean shaderPackActive() {
		return IrisApi.getInstance().isShaderPackInUse();
	}

	static boolean shadowPassActive() {
		return IrisApi.getInstance().isRenderingShadowPass();
	}
}
