package com.mrbysco.ancienttech.capability;

import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

public class EditableEnergyStorage extends SimpleEnergyHandler {

	public EditableEnergyStorage(int capacity, int maxReceive, int maxExtract) {
		super(capacity, maxReceive, maxExtract);
	}

	@Override
	public void set(int amount) {
		super.set(amount);
	}

	public void setEnergyStored(int energy) {
		this.energy = energy;
	}
}
