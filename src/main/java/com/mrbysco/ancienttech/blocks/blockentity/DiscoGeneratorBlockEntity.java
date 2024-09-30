package com.mrbysco.ancienttech.blocks.blockentity;

import com.mrbysco.ancienttech.AncientTech;
import com.mrbysco.ancienttech.registry.AncientBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

//TODO: Make the hardcoded values like max storage and generation multiplier configurable
public class DiscoGeneratorBlockEntity extends BlockEntity implements PowerStoring {
	private static final TreeMap<Integer, DyeColor> COLOR_CACHE = new TreeMap<>();

	private final HashMap<DyeColor, BlockState> colorMap = new HashMap<>();

	private final List<BlockPos> positions;
	public final EnergyStorage storage;

	public DiscoGeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(AncientBlockEntities.DISCO_GENERATOR_BLOCK_ENTITY.get(), pos, state);
		this.storage = new EnergyStorage(40000, 160, 160);
		int range = getRange();
		this.positions = BlockPos.betweenClosedStream(
				pos.offset(-range, -range, -range),
				pos.offset(range, range, range)).map(BlockPos::immutable).collect(Collectors.toList());
	}

	@Override
	public IEnergyStorage getEnergyStorage(Direction facing) {
		return this.storage;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, DiscoGeneratorBlockEntity blockEntity) {
		if (level.getGameTime() % 20 == 0) {
			//Check each position to see if the block is a colored block (has a color name in its resource path)
			if (blockEntity.storage.getEnergyStored() < blockEntity.storage.getMaxEnergyStored()) {
				blockEntity.colorMap.clear();
				for (BlockPos checkPos : blockEntity.positions) {
					BlockState checkState = level.getBlockState(checkPos);
					if (!checkState.isEmpty()) {
						int blockID = Block.getId(checkState);
						DyeColor color = COLOR_CACHE.computeIfAbsent(blockID, id -> {
							ResourceLocation blockName = BuiltInRegistries.BLOCK.getKey(checkState.getBlock());
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
					blockEntity.storage.receiveEnergy(blockEntity.colorMap.size() * 10, false);
				}
			}
		}
	}

	public int getRange() {
		return 4;
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);

		if (tag.contains("energy"))
			this.storage.deserializeNBT(registries, tag.get("energy"));
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);

		tag.put("energy", this.storage.serializeNBT(registries));
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
		CompoundTag compoundNBT = pkt.getTag();
		handleUpdateTag(compoundNBT, lookupProvider);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider lookupProvider) {
		CompoundTag nbt = new CompoundTag();
		this.saveAdditional(nbt, lookupProvider);
		return nbt;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
		super.handleUpdateTag(tag, lookupProvider);
	}

	@Override
	public CompoundTag getPersistentData() {
		CompoundTag nbt = new CompoundTag();
		this.saveAdditional(nbt, level != null ? level.registryAccess() : VanillaRegistries.createLookup());
		return nbt;
	}
}
