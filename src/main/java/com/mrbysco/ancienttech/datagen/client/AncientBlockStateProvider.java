package com.mrbysco.ancienttech.datagen.client;

import com.mrbysco.ancienttech.AncientTech;
import com.mrbysco.ancienttech.registry.AncientRegistry;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

public class AncientBlockStateProvider extends ModelProvider {
	public AncientBlockStateProvider(PackOutput output) {
		super(output, AncientTech.MOD_ID);
	}

	@Override
	protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
		// Sculk Generators
		Identifier sculkModelId = Identifier.fromNamespaceAndPath(AncientTech.MOD_ID, "block/sculk_generator");
		blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
				AncientRegistry.SCULK_GENERATOR.get(),
				BlockModelGenerators.plainVariant(sculkModelId)
		));
		blockModels.registerSimpleItemModel(AncientRegistry.SCULK_GENERATOR.get(), sculkModelId);

		blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
				AncientRegistry.SCULKIER_GENERATOR.get(),
				BlockModelGenerators.plainVariant(sculkModelId)
		));
		blockModels.registerSimpleItemModel(AncientRegistry.SCULKIER_GENERATOR.get(), sculkModelId);

		// Disco Generator
		Identifier discoModelId = ModelTemplates.CUBE_ALL.create(
				AncientRegistry.DISCO_GENERATOR.get(),
				TextureMapping.cube(new Material(Identifier.withDefaultNamespace("block/jukebox_side"))),
				blockModels.modelOutput
		);
		blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
				AncientRegistry.DISCO_GENERATOR.get(),
				BlockModelGenerators.plainVariant(discoModelId)
		));
		blockModels.registerSimpleItemModel(AncientRegistry.DISCO_GENERATOR.get(), discoModelId);

		// Hurt Generator
		Identifier hurtModelId = ModelTemplates.CUBE_ALL.create(
				AncientRegistry.HURT_GENERATOR.get(),
				TextureMapping.cube(new Material(Identifier.withDefaultNamespace("block/target_side"))),
				blockModels.modelOutput
		);
		blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
				AncientRegistry.HURT_GENERATOR.get(),
				BlockModelGenerators.plainVariant(hurtModelId)
		));
		blockModels.registerSimpleItemModel(AncientRegistry.HURT_GENERATOR.get(), hurtModelId);

		// Pain Generator reuses Hurt Generator's generated model
		blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
				AncientRegistry.PAIN_GENERATOR.get(),
				BlockModelGenerators.plainVariant(hurtModelId)
		));
		blockModels.registerSimpleItemModel(AncientRegistry.PAIN_GENERATOR.get(), hurtModelId);
	}
}
