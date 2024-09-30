package com.mrbysco.ancienttech.datagen;

import com.mrbysco.ancienttech.datagen.client.AncientBlockStateProvider;
import com.mrbysco.ancienttech.datagen.client.AncientLanguageProvider;
import com.mrbysco.ancienttech.datagen.data.AncientGameEventTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class AncientDataGen {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
		ExistingFileHelper helper = event.getExistingFileHelper();

		if (event.includeServer()) {
//			generator.addProvider(event.includeServer(), new AncientLootProvider(packOutput));
//			generator.addProvider(event.includeServer(), new AncientRecipeProvider(packOutput));
//			AncientBlockTagProvider blockTags = new AncientBlockTagProvider(packOutput, lookupProvider, helper);
//			generator.addProvider(event.includeServer(), blockTags);
//			generator.addProvider(event.includeServer(), new AncientItemTagProvider(packOutput, lookupProvider, blockTags, helper));
//			generator.addProvider(event.includeServer(), new AncientBiomeTagProvider(packOutput, lookupProvider, helper));
			generator.addProvider(event.includeServer(), new AncientGameEventTagProvider(packOutput, lookupProvider, helper));
//			generator.addProvider(event.includeServer(), new AncientGLMProvider(packOutput));
//			generator.addProvider(event.includeServer(), new AncientPatchouliProvider(packOutput));
//			generator.addProvider(event.includeServer(), new AncientAdvancementProvider(packOutput, lookupProvider, helper));

//			generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(
//					packOutput, CompletableFuture.supplyAsync(AncientDataGenerator::getProvider), Set.of(Reference.MOD_ID)));
		}
		if (event.includeClient()) {
			generator.addProvider(event.includeClient(), new AncientLanguageProvider(packOutput));
//			generator.addProvider(event.includeClient(), new AncientSoundProvider(packOutput, helper));
			generator.addProvider(event.includeClient(), new AncientBlockStateProvider(packOutput, helper));
//			generator.addProvider(event.includeClient(), new AncientItemModelProvider(packOutput, helper));
		}
	}
}
