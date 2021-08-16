package com.github.Vaapukkax.fca.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.Vaapukkax.fca.FCA;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

@Mixin(HandledScreen.class)
public class FlagMixin {
	
	private double time;
	
	@Shadow protected int playerInventoryTitleX, playerInventoryTitleY;
	@Shadow protected Text playerInventoryTitle;
	@Shadow protected int titleX, titleY;
	
	@Inject(at = @At(value = "INVOKE"), method = "drawForeground", cancellable = true)
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY, CallbackInfo ci) {
		if (get() == null || !FCA.isFlagClash()) return;
		Text title = get().getTitle();
		
		double tt = getUpgradeTime();
		if (tt == -1.0) return;
		ci.cancel();
		
		if (!isHoldingFlag()) time = tt;
		if (time<0) time = 0;
		
		String ts = (time>60) ? FCA.round(time/60d)+"m" : FCA.round(time)+"s";
		
		title = new LiteralText("\u00a78Flag \u00a77Upgrade Time: "+ts);
		
		MinecraftClient m = MinecraftClient.getInstance();
		m.textRenderer.draw(matrices, title, (float)this.titleX, (float)this.titleY, 4210752);
		m.textRenderer.draw(matrices, this.playerInventoryTitle, (float)this.playerInventoryTitleX, (float)this.playerInventoryTitleY, 4210752);
	}

	private double getUpgradeTime() {
		long g = FCA.toRealValue(FCA.getStat("Gold"));
		long upgrade = getUpgradeAmount();
		if (upgrade == -1L) return -1.0;
		return (((upgrade-g)/FCA.getGPS())/FCA.getMultiplier());
	}
	
	private long getUpgradeAmount() {
		try {
			GenericContainerScreen t = get();
			MinecraftClient m = MinecraftClient.getInstance();
			
			DefaultedList<Slot> sl = t.getScreenHandler().slots;
			for (int i = 0; i < sl.size(); i++) {
				ItemStack s = sl.get(i).getStack();
				if (s.getName().getString().contains("Your Flag")) {
					List<Text> l = s.getTooltip(m.player, new TooltipContext() {
						@Override
						public boolean isAdvanced() {
							return false;
						}
					});
					return FCA.toRealValue(l.get(3).getString().split(" ")[2]);
				}
			}
			return -1L;
		} catch (Exception e) {
			System.err.println("Something went wrong with displaying upgrade time");
			e.printStackTrace();
			return -1L;
		}
	}
	
	private boolean isHoldingFlag() {
		ItemStack is = get().getScreenHandler().getCursorStack();
		return (is != null && is.getItem().getName().getString().toLowerCase().contains("banner"));
	}
	
	private GenericContainerScreen get() {
		Object o = (Object)this;
		if (o instanceof GenericContainerScreen) {
			GenericContainerScreen oc = (GenericContainerScreen)o;
			if (oc.getTitle().getString().toLowerCase().contains("flag"))
				return oc;
		}
		return null;
	}
}