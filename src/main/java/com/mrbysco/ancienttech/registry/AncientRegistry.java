package com.mrbysco.ancienttech.registry;

import com.mrbysco.ancienttech.AncientTech;
import com.mrbysco.ancienttech.blocks.DiscoGeneratorBlock;
import com.mrbysco.ancienttech.blocks.PainGeneratorBlock;
import com.mrbysco.ancienttech.blocks.SculkGeneratorBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AncientRegistry {
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AncientTech.MOD_ID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AncientTech.MOD_ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AncientTech.MOD_ID);

	public static final DeferredBlock<SculkGeneratorBlock> SCULK_GENERATOR = registerBlockWithItem("sculk_generator", () -> new SculkGeneratorBlock(blockBuilder()), blockItemBuilder());
	public static final DeferredBlock<SculkGeneratorBlock> SCULKIER_GENERATOR = registerBlockWithItem("sculkier_generator", () -> new SculkGeneratorBlock(blockBuilder()), blockItemBuilder());
	public static final DeferredBlock<DiscoGeneratorBlock> DISCO_GENERATOR = registerBlockWithItem("disco_generator", () -> new DiscoGeneratorBlock(blockBuilder()), blockItemBuilder());
	public static final DeferredBlock<PainGeneratorBlock> HURT_GENERATOR = registerBlockWithItem("hurt_generator", () -> new PainGeneratorBlock(blockBuilder()), blockItemBuilder());
	public static final DeferredBlock<PainGeneratorBlock> PAIN_GENERATOR = registerBlockWithItem("pain_generator", () -> new PainGeneratorBlock(blockBuilder()), blockItemBuilder());

	public static <B extends Block> DeferredBlock<B> registerBlockWithItem(String name, Supplier<? extends B> supplier, Item.Properties properties) {
		DeferredBlock<B> block = AncientRegistry.BLOCKS.register(name, supplier);
		ITEMS.register(name, () -> new BlockItem(block.get(), properties));
		return block;
	}

	private static Block.Properties blockBuilder() {
		return Block.Properties.of().sound(SoundType.SCULK_SENSOR).lightLevel(state -> 1).mapColor(MapColor.COLOR_CYAN);
	}

	private static Item.Properties blockItemBuilder() {
		return itemBuilder();
	}

	private static Item.Properties itemBuilder() {
		return new Item.Properties();
	}

	public static final Supplier<CreativeModeTab> GROUP = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
			.icon(() -> AncientRegistry.SCULK_GENERATOR.get().asItem().getDefaultInstance())
			.withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
			.title(Component.translatable("itemGroup.ancienttech"))
			.displayItems((parameters, output) -> {
				List<ItemStack> stacks = AncientRegistry.ITEMS.getEntries().stream().map(reg -> new ItemStack(reg.get())).collect(Collectors.toList());

				output.acceptAll(stacks);
			}).build());
}
