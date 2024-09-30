package com.mrbysco.ancienttech.blocks.blockentity;

import com.mrbysco.ancienttech.registry.AncientBlockEntities;
import com.mrbysco.ancienttech.registry.AncientRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.jetbrains.annotations.Nullable;

//TODO: Make the hardcoded values like max storage and generation multiplier configurable
public class SculkGeneratorBlockEntity extends VibrationBasedBlockEntity implements PowerStoring {
	public final EnergyStorage storage;

	public SculkGeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(AncientBlockEntities.SCULK_GENERATOR_BLOCK_ENTITY.get(), pos, state);
		this.storage = new EnergyStorage(getMaxStorage(state), 100, 100);
	}

	@Override
	public IEnergyStorage getEnergyStorage(Direction facing) {
		return this.storage;
	}

	private int getMaxStorage(BlockState state) {
		if (state.is(AncientRegistry.SCULK_GENERATOR.get()))
			return 30000;
		else
			return 60000;
	}

	private boolean allowEntityVibration() {
		return this.getBlockState().is(AncientRegistry.SCULK_GENERATOR.get());
	}

	private int generationMultiplier() {
		if (this.getBlockState().is(AncientRegistry.SCULKIER_GENERATOR.get()))
			return 4;
		else
			return 3;
	}

	@Override
	public boolean canReceiveVibration(ServerLevel serverLevel, BlockPos pos, Holder<GameEvent> gameEvent, @Nullable GameEvent.Context context) {
		boolean flag = !pos.equals(this.worldPosition);
		if (context != null && context.sourceEntity() == null) {
			return flag;
		} else {
			return allowEntityVibration() && flag;
		}
	}

	@Override
	public void onReceiveVibration(ServerLevel serverLevel, BlockPos pos, Holder<GameEvent> gameEvent, @Nullable Entity entity, @Nullable Entity playerEntity, float distance) {
		this.setLastVibrationFrequency(VibrationSystem.getGameEventFrequency(gameEvent));

		if (this.storage.getEnergyStored() < this.storage.getMaxEnergyStored()) {
			int frequency = gameEvent.getData(NeoForgeDataMaps.VIBRATION_FREQUENCIES).frequency();
			int generating = 5 * (Math.round((float) (frequency * generationMultiplier()) / 5));
			this.storage.receiveEnergy(generating, false);
			this.setChanged();
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 2);
		}
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
