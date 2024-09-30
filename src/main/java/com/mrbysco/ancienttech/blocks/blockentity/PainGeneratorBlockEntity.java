package com.mrbysco.ancienttech.blocks.blockentity;

import com.mrbysco.ancienttech.api.AncientTags;
import com.mrbysco.ancienttech.capability.EditableEnergyStorage;
import com.mrbysco.ancienttech.fakeplayer.AncientFakePlayer;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

//TODO: Make the hardcoded values like max storage and generation multiplier configurable
public class PainGeneratorBlockEntity extends VibrationBasedBlockEntity implements PowerStoring {
	public final EditableEnergyStorage storage;
	private final AABB aabb;
	private UUID placer = null;

	public PainGeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(AncientBlockEntities.PAIN_GENERATOR_BLOCK_ENTITY.get(), pos, state);
		this.storage = new EditableEnergyStorage(getMaxStorage(state), 100, 100);

		if (state.is(AncientRegistry.PAIN_GENERATOR.get()))
			this.aabb = new AABB(pos).inflate(2.0D);
		else
			this.aabb = null;
	}

	//
	public static void serverTick(Level level, BlockPos pos, BlockState state, PainGeneratorBlockEntity blockEntity) {
		if (level.getGameTime() % 20 == 0 && level.hasNeighborSignal(pos) && blockEntity.hurtsEntities() && blockEntity.canKillEntities()) {
			AncientFakePlayer.useFakePlayer((ServerLevel) level, blockEntity.placer, (fakePlayer -> {
				DamageSource source = level.damageSources().playerAttack(fakePlayer);
				level.getEntitiesOfClass(LivingEntity.class, blockEntity.aabb, entity -> entity.isAlive() &&
						!entity.isInvulnerableTo(source)).forEach(entity -> {
					ItemStack tempSword = new ItemStack(Items.WOODEN_SWORD, 1);
					fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, tempSword);
					entity.hurt(source, entity.getMaxHealth());
				});
				blockEntity.storage.setEnergyStored(0);
				return true;
			}));
		}
	}

	private boolean canKillEntities() {
		return aabb != null && storage.getEnergyStored() == storage.getMaxEnergyStored();
	}

	@Override
	public IEnergyStorage getEnergyStorage(Direction facing) {
		return this.storage;
	}

	private int getMaxStorage(BlockState state) {
		if (state.is(AncientRegistry.HURT_GENERATOR.get()))
			return 30000;
		else
			return 60000;
	}

	private boolean hurtsEntities() {
		return this.getBlockState().is(AncientRegistry.HURT_GENERATOR.get());
	}

	@Override
	public boolean canReceiveVibration(ServerLevel serverLevel, BlockPos pos, Holder<GameEvent> gameEvent, @Nullable GameEvent.Context context) {
		return !pos.equals(this.worldPosition) && gameEvent.is(AncientTags.GameEvents.IS_HURT);
	}

	@Override
	public void onReceiveVibration(ServerLevel serverLevel, BlockPos pos, Holder<GameEvent> gameEvent, @Nullable Entity entity, @Nullable Entity playerEntity, float distance) {
		this.setLastVibrationFrequency(VibrationSystem.getGameEventFrequency(gameEvent));

		if (this.storage.getEnergyStored() < this.storage.getMaxEnergyStored()) {
			int frequency = gameEvent.getData(NeoForgeDataMaps.VIBRATION_FREQUENCIES).frequency();
			int generating = 5 * (Math.round((float) (frequency * 4) / 5));
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

		placer = tag.hasUUID("placer") ? tag.getUUID("placer") : null;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);

		tag.put("energy", this.storage.serializeNBT(registries));

		if (placer != null) tag.putUUID("placer", placer);
	}

	public void setPlacer(Player player) {
		placer = player.getUUID();
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
