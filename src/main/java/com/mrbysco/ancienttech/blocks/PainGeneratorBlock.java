package com.mrbysco.ancienttech.blocks;

import com.mojang.serialization.MapCodec;
import com.mrbysco.ancienttech.blocks.blockentity.PainGeneratorBlockEntity;
import com.mrbysco.ancienttech.registry.AncientBlockEntities;
import com.mrbysco.ancienttech.registry.AncientRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.jetbrains.annotations.Nullable;

public class PainGeneratorBlock extends BaseEntityBlock {
	public static final MapCodec<PainGeneratorBlock> CODEC = simpleCodec(PainGeneratorBlock::new);

	public PainGeneratorBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<PainGeneratorBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PainGeneratorBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return level.isClientSide ? null : createTickerHelper(
				blockEntityType, AncientBlockEntities.PAIN_GENERATOR_BLOCK_ENTITY.get(),
				(blockLevel, blockPos, blockState, blockEntity) -> {
					if (blockState.is(AncientRegistry.PAIN_GENERATOR.get())) {
						PainGeneratorBlockEntity.serverTick(blockLevel, blockPos, blockState, blockEntity);
					}
					VibrationSystem.Ticker.tick(blockLevel, blockEntity.getVibrationData(), blockEntity.getVibrationUser());
				}
		);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (state.is(AncientRegistry.PAIN_GENERATOR.get()) && level.getBlockEntity(pos) instanceof PainGeneratorBlockEntity blockEntity) {
			if (placer instanceof Player player)
				blockEntity.setPlacer(player);
		}
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}
}
