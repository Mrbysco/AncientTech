package com.mrbysco.ancienttech.api;

import com.mrbysco.ancienttech.AncientTech;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.gameevent.GameEvent;

public class AncientTags {
	public static class GameEvents {
		public static final TagKey<GameEvent> IS_HURT = create("is_hurt");

		private static TagKey<GameEvent> create(String path) {
			return TagKey.create(Registries.GAME_EVENT, AncientTech.modLoc(path));
		}
	}
}
