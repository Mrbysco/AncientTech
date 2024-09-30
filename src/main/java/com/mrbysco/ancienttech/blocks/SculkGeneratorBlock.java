package com.mrbysco.ancienttech.blocks;

import com.mojang.serialization.MapCodec;
import com.mrbysco.ancienttech.blocks.blockentity.SculkGeneratorBlockEntity;
import com.mrbysco.ancienttech.registry.AncientBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.jetbrains.annotations.Nullable;

public class SculkGeneratorBlock extends BaseEntityBlock {
	public static final MapCodec<SculkGeneratorBlock> CODEC = simpleCodec(SculkGeneratorBlock::new);

	public SculkGeneratorBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<SculkGeneratorBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SculkGeneratorBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return level.isClientSide ? null : createTickerHelper(
				blockEntityType, AncientBlockEntities.SCULK_GENERATOR_BLOCK_ENTITY.get(),
				(blockLevel, blockPos, blockState, blockEntity) -> VibrationSystem.Ticker.tick(
						blockLevel, blockEntity.getVibrationData(), blockEntity.getVibrationUser()
				)
		);
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}
}
