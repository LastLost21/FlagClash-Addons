package com.github.Vaapukkax.fca.mixin;

import java.awt.Color;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.Vaapukkax.fca.FCA;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Mixin(InGameHud.class)
public class HUDMixin {
	
	@Shadow Text overlayMessage;
	@Shadow int scaledHeight, scaledWidth;
	MatrixStack mat;
	
    @Inject(at = @At(value = "INVOKE"), method = "renderExperienceBar", cancellable = true)
    private void renderExperienceBar(MatrixStack matrices, int xx, CallbackInfo ci) {
    	if (!FCA.isFlagClash()) return;
    	FCA.get().actionbar = overlayMessage;
    	ci.cancel();
    	
    	MinecraftClient m = MinecraftClient.getInstance();
		TextRenderer r = m.textRenderer;
		String str = "Mana: "+m.player.experienceLevel+"\uD83D\uDD25";
		
		boolean sb = renderBubbles(matrices);
		float x = (float) (m.getWindow().getScaledWidth()/2+50-r.getWidth(str)/2);
		float y = (float) (m.getWindow().getScaledHeight()-38f);
		r.drawWithShadow(matrices, str, x, y-(sb ? 11 : 0), new Color(0, 138, 255).getRGB());
		
		double time = FCA.get().getCountdown();
		if (time != 0) {
			str = "Ready in "+((int)time)+"s";
			x = m.getWindow().getScaledWidth()-r.getWidth(str)-13;
			y = (float) (m.getWindow().getScaledHeight()/2)+24;
			r.drawWithShadow(matrices, str, x, y, new Color(137, 50, 183).getRGB());
		}
		
    	matrices.translate(0, -6, 0);
    	mat = null;
    }
    
    private int getHeartCount2(LivingEntity entity) {
        if (entity != null && entity.isLiving()) {
           float f = entity.getMaxHealth();
           int i = (int)(f + 0.5F) / 2;
           if (i > 30) {
              i = 30;
           }

           return i;
        } else {
           return 0;
        }
     }
    
    private boolean renderBubbles(MatrixStack matrices) {
    	MinecraftClient c = MinecraftClient.getInstance();
    	c.getProfiler().swap("air");
        boolean b = false;
    	int z = c.player.getMaxAir(), x = getHeartCount2(c.player);
        int aa = Math.min(c.player.getAir(), z), ab;
        int t = scaledHeight - 39;
        int n = scaledWidth / 2 + 91;
        
        if (c.player.isSubmergedIn(FluidTags.WATER) || aa < z) {
           ab = (int)Math.ceil((double)x / 10.0D) - 1;
           t -= ab * 10;
           int ah = MathHelper.ceil((double)(aa - 2) * 10.0D / (double)z);
           int ad = MathHelper.ceil((double)aa * 10.0D / (double)z) - ah;

           for(int aj = 0; aj < ah + ad; ++aj) {
              if (aj < ah) {
                 get().drawTexture(matrices, n - aj * 8 - 9, t, 16, 18, 9, 9);
              } else {
                 get().drawTexture(matrices, n - aj * 8 - 9, t, 25, 18, 9, 9);
              }
           }
           b = true;
        }
        c.getProfiler().pop();
        return b;
    }
    
    @Inject(at = @At(value = "INVOKE"), method = "renderHealthBar")
    private void renderHealthBar(MatrixStack matrices, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
    	if (!FCA.isFlagClash()) return;
    	if (mat == null) {
    		matrices.translate(0, 6, 0);
    		mat = matrices;
    	}
    	matrices = mat;
    }
    @Inject(at = @At(value = "INVOKE", ordinal = 30), method = "renderStatusBars", cancellable = true)
    private void renderStatusBars(MatrixStack matrices, CallbackInfo ci) {
    	if (!FCA.isFlagClash()) return;
    	ci.cancel();
	}
    
    private InGameHud get() {
    	return (InGameHud)((Object)this);
    }
}