package com.github.Vaapukkax.fca.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.Vaapukkax.fca.FCA;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

@Mixin(PlayerEntity.class)
public class BladeJumpMixin {

	@Inject(method = "jump", at = @At(value = "INVOKE"), cancellable = true)
	public void jump(CallbackInfo ci) {
		PlayerEntity p = get();
		if (FCA.isFlagClash() && FCA.get().getHolding(p).getItem() == Items.STONE_SWORD)
			ci.cancel();
	}
	
	public PlayerEntity get() {
		return (PlayerEntity)((Object)this);
	}
}
