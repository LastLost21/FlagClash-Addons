package com.github.Vaapukkax.fca.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.Vaapukkax.fca.FCA;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

@Mixin(ChatHud.class)
public class ChatMixin {

	long last;
	
	@Inject(method = "addMessage", at = @At(value = "RETURN"), cancellable = true)
	public void addMessage(Text message, CallbackInfo ci) {
		MinecraftClient c = MinecraftClient.getInstance();
		String msg = message.getString();
		if (sinceLast() > 120 && msg.contains("@"+c.player.getName()) && msg.contains("afk") && FCA.get().nearSpawn()) {
			double afk = FCA.get().getAFKTime();
			if (afk>80) {
				if (afk>60*5) 
					c.player.sendChatMessage("[Automated] Yes sir, I have been AFK "+afk/60d+"m");
				else c.player.sendChatMessage("[Automated] Perhaps... I have been AFK "+afk/60d+"m");
			}
		}
	}
	
	private double sinceLast() {
		return (System.currentTimeMillis()-last)/1000d;
	}
	
}
