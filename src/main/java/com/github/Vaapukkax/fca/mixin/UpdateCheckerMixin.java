package com.github.Vaapukkax.fca.mixin;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.Vaapukkax.fca.FCA;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Mixin(MinecraftClient.class)
public class UpdateCheckerMixin {

	@Inject(method = "joinWorld", at = @At(value = "TAIL"))
	public void joinWorld(ClientWorld world, CallbackInfo ci) {
		checkForUpdates();
		FCA.get().enabled = false; //too lazy to create another class
	}
	@Inject(method = "tick", at = @At(value = "TAIL"))
	public void tick(CallbackInfo ci) {
		MinecraftClient m = MinecraftClient.getInstance();
		if (m.player != null && updateC != null) {
			Text text = new LiteralText("\u00a76[FCAddons] \u00a7eNew version of FCA Available at: \u00a7a"+updateC).styled((style) -> {
				return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("\u00a7aClick Here!")))
							.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, updateC));
			});
			m.player.sendMessage(text, false);
			m.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
			updateC = null;
		}
	}
	
	public String updateC;
	private void checkForUpdates() {
		
		new Thread() {
			public void run() {
			    String url = "https://github.com/Vaapukkax/FlagClash-Addons/releases";

			    try {
			        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			        HttpGet request = new HttpGet(url);
			        request.addHeader("content-type", "application/json");
			        HttpResponse result = httpClient.execute(request);
			        String json = EntityUtils.toString(result.getEntity(), "UTF-8");
			
			        String latest = json.split("<span class=\"css-truncate-target\" style=\"max-width: 125px\">")[1].split("<")[0];
			        if (!latest.equalsIgnoreCase(FCA.VERSION))
			        	updateC = "https://github.com/Vaapukkax/FlagClash-Addons/releases/tag/"+latest;
			    } catch (IOException ex) {
			    	if (!(ex instanceof UnknownHostException)) {
			    		ex.printStackTrace();
			    		updateC = url;
			    	}
			    }
			}
		}.start();
	}
}
