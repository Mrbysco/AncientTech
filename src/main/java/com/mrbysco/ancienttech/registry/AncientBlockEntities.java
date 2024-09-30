package com.mrbysco.ancienttech.registry;

import com.mrbysco.ancienttech.AncientTech;
import com.mrbysco.ancienttech.blocks.blockentity.DiscoGeneratorBlockEntity;
import com.mrbysco.ancienttech.blocks.blockentity.PainGeneratorBlockEntity;
import com.mrbysco.ancienttech.blocks.blockentity.PowerStoring;
import com.mrbysco.ancienttech.blocks.blockentity.SculkGeneratorBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class AncientBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AncientTech.MOD_ID);

	public static final Supplier<BlockEntityType<SculkGeneratorBlockEntity>> SCULK_GENERATOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("sculk_generator", () -> BlockEntityType.Builder.of(SculkGeneratorBlockEntity::new,
			AncientRegistry.SCULK_GENERATOR.get(), AncientRegistry.SCULKIER_GENERATOR.get()).build(null));
	public static final Supplier<BlockEntityType<PainGeneratorBlockEntity>> PAIN_GENERATOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("pain_generator", () -> BlockEntityType.Builder.of(PainGeneratorBlockEntity::new,
			AncientRegistry.HURT_GENERATOR.get(), AncientRegistry.PAIN_GENERATOR.get()).build(null));

	public static final Supplier<BlockEntityType<DiscoGeneratorBlockEntity>> DISCO_GENERATOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("disco_generator", () -> BlockEntityType.Builder.of(DiscoGeneratorBlockEntity::new,
			AncientRegistry.DISCO_GENERATOR.get()).build(null));


	public static void registerBlockCapabilities(RegisterCapabilitiesEvent event) {
		List<BlockEntityType<? extends PowerStoring>> powerStoringBlockEntities = List.of(
				SCULK_GENERATOR_BLOCK_ENTITY.get(),
				PAIN_GENERATOR_BLOCK_ENTITY.get(),
				DISCO_GENERATOR_BLOCK_ENTITY.get()
		);
		powerStoringBlockEntities.forEach(type -> {
			event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type, PowerStoring::getEnergyStorage);
		});
	}
}
