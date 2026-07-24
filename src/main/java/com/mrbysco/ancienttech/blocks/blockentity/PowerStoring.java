package com.mrbysco.ancienttech.blocks.blockentity;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jetbrains.annotations.Nullable;

public interface PowerStoring {
	default EnergyHandler getEnergyStorage(@Nullable Direction facing) {
		return null;
	}
}
