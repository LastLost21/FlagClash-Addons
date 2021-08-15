package com.github.Vaapukkax.fca.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.Vaapukkax.fca.FCA;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;

@Mixin(ClientPlayerInteractionManager.class)
public class BlockBreakMixin {

	@Inject(method = "breakBlock", at = @At(value = "RETURN"))
	public void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
		if (FCA.isFlagClash() && ci.getReturnValue()) {
			if (pos.equals(FCA.get().flag)) {
				FCA.get().startCountdown(15);
				FCA.get().flag = null;
			}
			
		}
	}
}
