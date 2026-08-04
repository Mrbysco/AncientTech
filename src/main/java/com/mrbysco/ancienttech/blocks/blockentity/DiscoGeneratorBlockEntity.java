package com.mrbysco.ancienttech.blocks.blockentity;

import com.mrbysco.ancienttech.client.color_drain.ColorDrainTracker;
import com.mrbysco.ancienttech.registry.AncientBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

//TODO: Make the hardcoded values like max storage and generation multiplier configurable
public class DiscoGeneratorBlockEntity extends BlockEntity implements PowerStoring {
	private static final TreeMap<Integer, DyeColor> COLOR_CACHE = new TreeMap<>();

	private final HashMap<DyeColor, BlockState> colorMap = new HashMap<>();

	private final List<BlockPos> positions;
	public final SimpleEnergyHandler storage;

	public DiscoGeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(AncientBlockEntities.DISCO_GENERATOR_BLOCK_ENTITY.get(), pos, state);
		this.storage = new SimpleEnergyHandler(40000, 160, 160);
		int range = getRange();
		this.positions = BlockPos.betweenClosedStream(
				pos.offset(-range, -range, -range),
				pos.offset(range, range, range)).map(BlockPos::immutable).collect(Collectors.toList());
	}

	@Override
	public EnergyHandler getEnergyStorage(Direction facing) {
		return this.storage;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, DiscoGeneratorBlockEntity blockEntity) {
		if (level.getGameTime() % 20 == 0) {
			//Check each position to see if the block is a colored block (has a color name in its resource path)
			if (blockEntity.storage.getAmountAsInt() < blockEntity.storage.getCapacityAsInt()) {
				blockEntity.colorMap.clear();
				for (BlockPos checkPos : blockEntity.positions) {
					BlockState checkState = level.getBlockState(checkPos);
					if (!checkState.isEmpty()) {
						int blockID = Block.getId(checkState);
						DyeColor color = COLOR_CACHE.computeIfAbsent(blockID, id -> {
							Identifier blockName = BuiltInRegistries.BLOCK.getKey(checkState.getBlock());
							if (blockName != null) {
								String blockPath = blockName.getPath();
								for (DyeColor dyeColor : DyeColor.values()) {
									//Special case for light gray blocks as gray is earlier in the list
									if (dyeColor == DyeColor.GRAY) {
										if (blockPath.contains("light_gray")) {
											return DyeColor.LIGHT_GRAY;
										}
									}
									if (blockPath.contains(dyeColor.getName())) {
										return dyeColor;
									}
								}
							}
							return null;
						});
						if (color != null) {
							blockEntity.colorMap.putIfAbsent(color, checkState);
						}
					}
				}

				if (!blockEntity.colorMap.isEmpty()) {
//					AncientTech.LOGGER.info("Color map {}", blockEntity.colorMap);
//					AncientTech.LOGGER.info("Generating {} from {} colors", blockEntity.colorMap.size() * 10, blockEntity.colorMap.size());
					try (var tx = Transaction.openRoot()) {
						int amount = (blockEntity.colorMap.size() * 10);
						if (blockEntity.storage.insert(amount, tx) == amount) {
							tx.commit();
						}
					}
				} else {
					try (var tx = Transaction.openRoot()) {
						int amount = 5;
						if (blockEntity.storage.insert(amount, tx) == amount) {
							tx.commit();
						}
					}
				}
			}
		}
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, DiscoGeneratorBlockEntity blockEntity) {

	}

	public int getRange() {
		return 4;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);

		this.storage.deserialize(input.childOrEmpty("energy"));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);

		this.storage.serialize(output.child("energy"));
	}

	@Override
	public void onLoad() {
		super.onLoad();
		setActive(true);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		setActive(false);
	}

	public void setActive(boolean active) {
		if (this.level != null && this.level.isClientSide()) {
			if (active) {
				ColorDrainTracker.add(this.getBlockPos());
			} else {
				ColorDrainTracker.remove(this.getBlockPos());
			}
		}
	}
}
