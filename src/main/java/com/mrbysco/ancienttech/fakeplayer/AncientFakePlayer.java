package com.mrbysco.ancienttech.fakeplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.function.Function;

public class AncientFakePlayer extends FakePlayer {
	public static final GameProfile GAME_PROFILE = new GameProfile(UUID.nameUUIDFromBytes("ancienttech.fake_player".getBytes()), Component.translatable("ancienttech.fake_player").getString());

	public AncientFakePlayer(ServerLevel serverLevel, GameProfile profile) {
		super(serverLevel, profile);
	}

	private static WeakReference<AncientFakePlayer> INSTANCE;

	public static <R> R useFakePlayer(ServerLevel serverLevel, @Nullable UUID placer, Function<AncientFakePlayer, R> fakePlayerConsumer) {
		AncientFakePlayer actual = INSTANCE == null ? null : INSTANCE.get();
		if (actual == null) {
			if (placer != null)
				actual = new AncientFakePlayer(serverLevel, new GameProfile(placer, Component.translatable("ancienttech.fake_player").getString()));
			else
				actual = new AncientFakePlayer(serverLevel, GAME_PROFILE); //Shouldn't happen but just in case

			INSTANCE = new WeakReference<>(actual);
		}
		AncientFakePlayer player = actual;
		player.setServerLevel(serverLevel);
		R result = fakePlayerConsumer.apply(player);

		//don't keep reference to the World, note we set it to the overworld to avoid any potential null pointers
		player.setServerLevel(serverLevel.getServer().overworld());
		return result;
	}

	@Override
	public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
		return false;
	}

	public static void unload(ServerLevel serverLevel) {
		AncientFakePlayer actual = INSTANCE == null ? null : INSTANCE.get();
		if (actual != null && actual.level() == serverLevel) {
			//don't keep reference to the World, note we set it to the overworld to avoid any potential null pointers
			actual.setServerLevel(serverLevel.getServer().overworld());
		}
	}

	@Override
	public boolean isAlliedTo(Team team) {
		return false;
	}

	@Nullable
	@Override
	public PlayerTeam getTeam() {
		return null;
	}
}
