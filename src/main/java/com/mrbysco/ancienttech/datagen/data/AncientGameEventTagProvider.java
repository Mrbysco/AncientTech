package com.mrbysco.ancienttech.datagen.data;

import com.mrbysco.ancienttech.AncientTech;
import com.mrbysco.ancienttech.api.AncientTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class AncientGameEventTagProvider extends GameEventTagsProvider {
	public AncientGameEventTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture,
	                                   @Nullable ExistingFileHelper fileHelper) {
		super(output, completableFuture, AncientTech.MOD_ID, fileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(AncientTags.GameEvents.IS_HURT).add(GameEvent.ENTITY_DAMAGE.key(), GameEvent.ENTITY_DIE.key());
	}
}
