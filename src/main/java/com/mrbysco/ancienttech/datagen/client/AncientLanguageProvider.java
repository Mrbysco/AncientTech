package com.mrbysco.ancienttech.datagen.client;

import com.mrbysco.ancienttech.AncientTech;
import com.mrbysco.ancienttech.registry.AncientRegistry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class AncientLanguageProvider extends LanguageProvider {
	public AncientLanguageProvider(PackOutput output) {
		super(output, AncientTech.MOD_ID, "en_us");
	}

	@Override
	protected void addTranslations() {
		add("itemGroup.ancienttech", "Ancient Tech");

		addBlock(AncientRegistry.SCULK_GENERATOR, "Sculk Generator");
		addBlock(AncientRegistry.SCULKIER_GENERATOR, "Sculkier Generator");
		addBlock(AncientRegistry.DISCO_GENERATOR, "Disco Generator");
		addBlock(AncientRegistry.HURT_GENERATOR, "Hurt Generator");
		addBlock(AncientRegistry.PAIN_GENERATOR, "Pain Generator");

		add("ancienttech.fake_player", "Ancient Fake Player");
	}
}
