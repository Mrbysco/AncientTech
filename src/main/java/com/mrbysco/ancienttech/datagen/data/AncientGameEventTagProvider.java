package com.mrbysco.ancienttech.datagen.data;

import com.mrbysco.ancienttech.AncientTech;
import com.mrbysco.ancienttech.api.AncientTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.concurrent.CompletableFuture;

public class AncientGameEventTagProvider extends GameEventTagsProvider {
	public AncientGameEventTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(output, completableFuture, AncientTech.MOD_ID);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(AncientTags.GameEvents.IS_HURT).add(GameEvent.ENTITY_DAMAGE.key(), GameEvent.ENTITY_DIE.key());
	}
}
