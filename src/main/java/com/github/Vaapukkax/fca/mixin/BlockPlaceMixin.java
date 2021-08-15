package com.github.Vaapukkax.fca.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.Vaapukkax.fca.FCA;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

@Mixin(ItemStack.class)
public class BlockPlaceMixin {
	
	@Inject(method = "useOnBlock", at = @At(value = "RETURN"))
	public void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> ci) {
		MinecraftClient c = MinecraftClient.getInstance();
		if (FCA.isFlagClash() && ci.getReturnValue() == ActionResult.SUCCESS) {
			BlockPos pos = context.getBlockPos().add(context.getSide().getVector());
			Block b = c.world.getBlockState(pos).getBlock();
			if (b.getName().getString().toLowerCase().contains("banner")) {
				FCA.get().flag = pos;
				FCA.get().startCountdown(5);
			}
		}
	}
}
