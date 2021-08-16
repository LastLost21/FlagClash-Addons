package com.github.Vaapukkax.fca.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

@Mixin(ChatHud.class)
public class ChatMixin {

	@Inject(method = "addMessage", at = @At(value = "RETURN"), cancellable = true)
	public void addMessage(Text message, CallbackInfo ci) {
		System.out.println(message.getString());
	}
	
}
