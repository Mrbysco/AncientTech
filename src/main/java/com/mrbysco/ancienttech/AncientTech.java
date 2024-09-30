package com.mrbysco.ancienttech;

import com.mojang.logging.LogUtils;
import com.mrbysco.ancienttech.fakeplayer.AncientFakePlayer;
import com.mrbysco.ancienttech.registry.AncientBlockEntities;
import com.mrbysco.ancienttech.registry.AncientRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.slf4j.Logger;

@Mod(AncientTech.MOD_ID)
public class AncientTech {
	public static final String MOD_ID = "ancienttech";
	public static final Logger LOGGER = LogUtils.getLogger();

	public AncientTech(IEventBus eventBus) {
		AncientRegistry.BLOCKS.register(eventBus);
		AncientRegistry.ITEMS.register(eventBus);
		AncientRegistry.CREATIVE_MODE_TABS.register(eventBus);
		AncientBlockEntities.BLOCK_ENTITIES.register(eventBus);

		NeoForge.EVENT_BUS.register(this);

		eventBus.addListener(AncientBlockEntities::registerBlockCapabilities);
	}

	@SubscribeEvent
	public void onLevelUnload(final LevelEvent.Unload event) {
		if (event.getLevel() instanceof ServerLevel serverLevel) {
			AncientFakePlayer.unload(serverLevel);
		}
	}

	public static ResourceLocation modLoc(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
