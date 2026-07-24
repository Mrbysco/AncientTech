package com.mrbysco.ancienttech.blocks.blockentity;

import com.mrbysco.ancienttech.AncientTech;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public abstract class VibrationBasedBlockEntity extends BlockEntity implements GameEventListener.Provider<VibrationSystem.Listener>, VibrationSystem {
	public static final int LISTENER_RANGE = 8;

	private VibrationSystem.Data vibrationData;
	private final VibrationSystem.Listener vibrationListener;
	private final VibrationSystem.User vibrationUser = this.createVibrationUser();
	private int lastVibrationFrequency;

	protected VibrationBasedBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
		super(entityType, pos, state);
		this.vibrationData = new VibrationSystem.Data();
		this.vibrationListener = new VibrationSystem.Listener(this);
	}

	public VibrationSystem.User createVibrationUser() {
		return new VibrationBasedBlockEntity.VibrationUser(this.getBlockPos());
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.lastVibrationFrequency = input.getIntOr("last_vibration_frequency", 0);
		input.read("listener", VibrationSystem.Data.CODEC).map(
				data -> this.vibrationData = data
		);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("last_vibration_frequency", this.lastVibrationFrequency);
		output.store("listener", VibrationSystem.Data.CODEC, vibrationData);
	}

	@Override
	public VibrationSystem.Data getVibrationData() {
		return this.vibrationData;
	}

	@Override
	public VibrationSystem.User getVibrationUser() {
		return this.vibrationUser;
	}

	public int getLastVibrationFrequency() {
		return this.lastVibrationFrequency;
	}

	public void setLastVibrationFrequency(int pLastVibrationFrequency) {
		this.lastVibrationFrequency = pLastVibrationFrequency;
	}

	public VibrationSystem.Listener getListener() {
		return this.vibrationListener;
	}

	public int getListenRadius() {
		return LISTENER_RANGE;
	}

	public abstract boolean canReceiveVibration(ServerLevel serverLevel, BlockPos pos, Holder<GameEvent> gameEvent, @Nullable GameEvent.Context context);

	public abstract void onReceiveVibration(ServerLevel serverLevel, BlockPos pos, Holder<GameEvent> gameEvent, @Nullable Entity entity,
	                                        @Nullable Entity playerEntity, float distance);

	@Override
	public void onDataPacket(Connection net, ValueInput valueInput) {
		super.onDataPacket(net, valueInput);

		BlockState state = level.getBlockState(getBlockPos());
		level.sendBlockUpdated(getBlockPos(), state, state, 3);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider lookupProvider) {
		CompoundTag tag = new CompoundTag();
		try (ProblemReporter.ScopedCollector problemreporter$scopedcollector = new ProblemReporter.ScopedCollector(AncientTech.LOGGER)) {
			TagValueOutput output = TagValueOutput.createWithContext(problemreporter$scopedcollector, lookupProvider);
			this.saveAdditional(output);
			tag.merge(output.buildResult());
		}
		return tag;
	}

	@Override
	public CompoundTag getPersistentData() {
		CompoundTag tag = new CompoundTag();
		try (ProblemReporter.ScopedCollector problemreporter$scopedcollector = new ProblemReporter.ScopedCollector(AncientTech.LOGGER)) {
			HolderLookup.Provider lookupProvider = this.level != null ? this.level.registryAccess() : VanillaRegistries.createLookup();
			TagValueOutput output = TagValueOutput.createWithContext(problemreporter$scopedcollector, lookupProvider);
			this.saveAdditional(output);
			tag.merge(output.buildResult());
		}
		return tag;
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	protected class VibrationUser implements VibrationSystem.User {
		protected final BlockPos blockPos;
		private final PositionSource positionSource;

		public VibrationUser(BlockPos pos) {
			this.blockPos = pos;
			this.positionSource = new BlockPositionSource(pos);
		}

		@Override
		public int getListenerRadius() {
			return VibrationBasedBlockEntity.this.getListenRadius();
		}

		@Override
		public PositionSource getPositionSource() {
			return this.positionSource;
		}

		@Override
		public boolean canTriggerAvoidVibration() {
			return true;
		}

		@Override
		public boolean canReceiveVibration(ServerLevel serverLevel, BlockPos pos, Holder<GameEvent> gameEvent, @Nullable GameEvent.Context context) {
			return VibrationBasedBlockEntity.this.canReceiveVibration(serverLevel, pos, gameEvent, context);
		}

		@Override
		public void onReceiveVibration(ServerLevel serverLevel, BlockPos pos, Holder<GameEvent> gameEvent, @Nullable Entity entity,
		                               @Nullable Entity playerEntity, float distance) {
			VibrationBasedBlockEntity.this.onReceiveVibration(serverLevel, pos, gameEvent, entity, playerEntity, distance);
		}

		@Override
		public void onDataChanged() {
			VibrationBasedBlockEntity.this.setChanged();
		}

		@Override
		public boolean requiresAdjacentChunksToBeTicking() {
			return true;
		}
	}
}
