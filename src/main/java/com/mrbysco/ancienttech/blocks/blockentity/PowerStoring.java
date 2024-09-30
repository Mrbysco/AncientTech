package com.mrbysco.ancienttech.blocks.blockentity;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public interface PowerStoring {
	default IEnergyStorage getEnergyStorage(@Nullable Direction facing) {
		return null;
	}
}
