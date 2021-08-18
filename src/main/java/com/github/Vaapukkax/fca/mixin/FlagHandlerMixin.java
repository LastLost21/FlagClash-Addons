package com.github.Vaapukkax.fca.mixin;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.Vaapukkax.fca.FCA;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

@Mixin(ScreenHandler.class)
public class FlagHandlerMixin {
	
	@Inject(at = @At(value = "INVOKE"), method = "onSlotClick")
	public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
		if (!FCA.isFlagClash()) return;
		if (slotIndex >= 0 && actionType == SlotActionType.PICKUP) {
			ItemStack stack = get().getSlot(slotIndex).getStack();
			Entry<Long, Integer> info = getUpgradeInfo(stack);
			long current = FCA.toRealValue(FCA.getStat("Gold"));
			
			if (current >= info.getKey()) {
				FCA.get().startCountdown(info.getValue()+0.5);
			}
		}
	}
	
	private Entry<Long, Integer> getUpgradeInfo(ItemStack stack) {
		MinecraftClient m = MinecraftClient.getInstance();
		List<Text> l = stack.getTooltip(m.player, new TooltipContext() {
			@Override
			public boolean isAdvanced() {
				return false;
			}
		});
		
		String gold = "0";
		int time = 0;
		
		for (int i = 0; i < l.size(); i++) {
			String s = l.get(i).getString();
			if (s.contains(" Upgrade Time")) time = Integer.parseInt(s.substring(15, s.length()-1));
			if (s.contains(" Upgrade Cost")) gold = s.substring(15, s.length()-5);
		}
		
		return new SimpleEntry<>(FCA.toRealValue(gold), time);
	}

	private ScreenHandler get() {
		return (ScreenHandler)((Object)this);
	}
}