package com.mrbysco.ancienttech.capability;

import net.neoforged.neoforge.energy.EnergyStorage;

public class EditableEnergyStorage extends EnergyStorage {

	public EditableEnergyStorage(int capacity, int maxReceive, int maxExtract) {
		super(capacity, maxReceive, maxExtract);
	}

	public void setEnergyStored(int energy) {
		this.energy = energy;
	}
}
