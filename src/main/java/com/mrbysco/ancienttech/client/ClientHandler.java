package com.mrbysco.ancienttech.client;

import com.mrbysco.ancienttech.AncientTech;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = AncientTech.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ClientHandler {

	@SubscribeEvent
	public static void setupRenderers(EntityRenderersEvent.RegisterRenderers event) {

	}
}