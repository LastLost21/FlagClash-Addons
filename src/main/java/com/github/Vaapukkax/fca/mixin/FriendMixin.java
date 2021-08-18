package com.github.Vaapukkax.fca.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.Vaapukkax.fca.FCA;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(ClientPlayerInteractionManager.class)
public class FriendMixin {
	
	@Inject(at = @At(value = "INVOKE"), method = "attackEntity", cancellable = true)
	public void attackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
		if (FCA.get().friends.contains(target.getName().getString())) ci.cancel();
	}
}
