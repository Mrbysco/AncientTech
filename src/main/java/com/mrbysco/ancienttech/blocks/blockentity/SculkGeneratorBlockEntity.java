package com.mrbysco.ancienttech.blocks.blockentity;

import com.mrbysco.ancienttech.registry.AncientBlockEntities;
import com.mrbysco.ancienttech.registry.AncientRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

//TODO: Make the hardcoded values like max storage and generation multiplier configurable
public class SculkGeneratorBlockEntity extends VibrationBasedBlockEntity implements PowerStoring {
	public final SimpleEnergyHandler storage;

	public SculkGeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(AncientBlockEntities.SCULK_GENERATOR_BLOCK_ENTITY.get(), pos, state);
		this.storage = new SimpleEnergyHandler(getMaxStorage(state), 100, 100);
	}

	@Override
	public EnergyHandler getEnergyStorage(Direction facing) {
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

		if (this.storage.getAmountAsInt() < this.storage.getCapacityAsInt()) {
			int frequency = gameEvent.getData(NeoForgeDataMaps.VIBRATION_FREQUENCIES).frequency();
			int generating = 5 * (Math.round((float) (frequency * generationMultiplier()) / 5));
			try (var tx = Transaction.openRoot()) {
				if (this.storage.insert(generating, tx) == generating) {
					tx.commit();
				}
			}
			this.setChanged();
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 2);
		}
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
}
