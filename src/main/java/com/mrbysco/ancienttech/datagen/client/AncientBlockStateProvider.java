package com.mrbysco.ancienttech.datagen.client;

import com.mrbysco.ancienttech.AncientTech;
import com.mrbysco.ancienttech.registry.AncientRegistry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class AncientBlockStateProvider extends BlockStateProvider {
	public AncientBlockStateProvider(PackOutput output, ExistingFileHelper fileHelper) {
		super(output, AncientTech.MOD_ID, fileHelper);
	}

	@Override
	protected void registerStatesAndModels() {
		ModelFile sculkModel = models().getExistingFile(modLoc("block/sculk_generator"));
		getVariantBuilder(AncientRegistry.SCULK_GENERATOR.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(sculkModel).build());
		itemModels().withExistingParent(AncientRegistry.SCULK_GENERATOR.getId().getPath(), modLoc("block/sculk_generator"));

		getVariantBuilder(AncientRegistry.SCULKIER_GENERATOR.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(sculkModel).build());
		itemModels().withExistingParent(AncientRegistry.SCULKIER_GENERATOR.getId().getPath(), modLoc("block/sculk_generator"));

		ModelFile model3 = models().cubeAll(AncientRegistry.DISCO_GENERATOR.getId().getPath(), mcLoc("block/jukebox_side"));
		getVariantBuilder(AncientRegistry.DISCO_GENERATOR.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(model3).build());
		itemModels().withExistingParent(AncientRegistry.DISCO_GENERATOR.getId().getPath(), modLoc("block/" + AncientRegistry.DISCO_GENERATOR.getId().getPath()));

		ModelFile hurtModel = models().cubeAll(AncientRegistry.HURT_GENERATOR.getId().getPath(), mcLoc("block/target_side"));
		getVariantBuilder(AncientRegistry.HURT_GENERATOR.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(hurtModel).build());
		itemModels().withExistingParent(AncientRegistry.HURT_GENERATOR.getId().getPath(), modLoc("block/hurt_generator"));

		getVariantBuilder(AncientRegistry.PAIN_GENERATOR.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(hurtModel).build());
		itemModels().withExistingParent(AncientRegistry.PAIN_GENERATOR.getId().getPath(), modLoc("block/hurt_generator"));
	}
}
