package com.mrbysco.ancienttech.blocks;

import com.mojang.serialization.MapCodec;
import com.mrbysco.ancienttech.blocks.blockentity.DiscoGeneratorBlockEntity;
import com.mrbysco.ancienttech.registry.AncientBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DiscoGeneratorBlock extends BaseEntityBlock {
	public static final MapCodec<DiscoGeneratorBlock> CODEC = simpleCodec(DiscoGeneratorBlock::new);

	public DiscoGeneratorBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<DiscoGeneratorBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DiscoGeneratorBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return createDiscoTicker(level, blockEntityType, AncientBlockEntities.DISCO_GENERATOR_BLOCK_ENTITY.get());
	}

	@Nullable
	protected static <T extends BlockEntity> BlockEntityTicker<T> createDiscoTicker(Level level, BlockEntityType<T> blockEntityType, BlockEntityType<? extends DiscoGeneratorBlockEntity> blockEntityType1) {
		return level.isClientSide ? null : createTickerHelper(blockEntityType, blockEntityType1, DiscoGeneratorBlockEntity::serverTick);
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}
}