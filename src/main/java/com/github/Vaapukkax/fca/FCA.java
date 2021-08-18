package com.github.Vaapukkax.fca;

import java.awt.Color;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.Last;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FCA implements ModInitializer {

	public static final String VERSION = "v10.1";
	
	private static FCA fca;
	public ArrayList<String> friends = new ArrayList<>();
//	public final KeyBinding shard = new KeyBinding(
//			"Quick Shard",
//		    InputUtil.Type.KEYSYM,
//		    GLFW.GLFW_KEY_R,
//		    "key.categories.misc"
//	);
	
	public BlockPos flag;
	public Text actionbar;
	
	@Override
	public void onInitialize() {
		fca = this;
//		KeyBindingHelper.registerKeyBinding(shard);
		
		// Check is FlagClash
		ClientTickEvents.START_CLIENT_TICK.register(new StartTick() {
			@Override public void onStartTick(MinecraftClient c) {
				if (!enabled && c.world != null) {
					Iterator<ScoreboardObjective> it = c.world.getScoreboard().getObjectives().iterator();
					while (it.hasNext())
						if (it.next().getDisplayName().getString().toLowerCase().contains("flagclash"))
							enabled = true;
				}
			}
		});
		
		// Add Friends
		HudRenderCallback.EVENT.register(new HudRenderCallback() {
			private boolean pressed;
			
			@Override
			public void onHudRender(MatrixStack matrix, float delta) {
				MinecraftClient c = MinecraftClient.getInstance();
				if (!isFlagClash()) return;
				
				if (c.targetedEntity instanceof PlayerEntity) {
					String e = c.targetedEntity.getName().getString();
					if (c.mouse.wasMiddleButtonClicked()) {
						if (!pressed) {
							if (friends.contains(e)) friends.remove(e);
							else friends.add(e);
							pressed = true;
						}
					} else pressed = false;
					if (friends.contains(e)) {
						String str = "Friended";
						
						TextRenderer tr = c.textRenderer;
						tr.drawWithShadow(matrix, str, c.getWindow().getScaledWidth()/2-tr.getWidth(str)/2, 40, new Color(85, 255, 85).getRGB());
					}
				}
			}		
		});
		
		// Teleportium Sword Visualize spots
	    WorldRenderEvents.LAST.register(new Last() {
			public void onLast(WorldRenderContext context) {
				if (!isFlagClash()) return;
				
				MinecraftClient m = MinecraftClient.getInstance();
				if (getHolding(m.player).getItem() == Items.NETHERITE_SWORD) {
					
					MatrixStack matrix = context.matrixStack();
					Vec3d p = m.gameRenderer.getCamera().getPos();

					matrix.translate(-p.x, -p.y, -p.z);
					
					for (int x = -2; x < 3; x++) {
						for (int z = -2; z < 3; z++) {
							BlockPos b = m.player.getBlockPos().add(x, 0, z);
							if (context.world().getBlockState(b).getBlock() == Blocks.AIR && context.world().getBlockState(b.add(0, 1, 0)).getBlock() == Blocks.AIR) {
								Vec3d v = new Vec3d(b.getX(), b.getY(), b.getZ());
								drawBox(matrix, new Box(v, v.add(1, 1, 1)));
							}
						}
					}
	    		}
			}
			
			private void drawBox(MatrixStack matrix, Box box) {
				MinecraftClient c = MinecraftClient.getInstance();
				VertexConsumerProvider provider = c.options.fov > 63 && c.options.fov <= 90 ? c.getBufferBuilders().getOutlineVertexConsumers() : c.getBufferBuilders().getEffectVertexConsumers();
				VertexConsumer vertex = provider.getBuffer(RenderLayer.LINES);
				
				box = new Box(box.minX+0.05f, box.minY+0.025f, box.minZ+0.05f, box.maxX-0.025f, box.maxY-0.05f, box.maxZ-0.05f);
				
				float a = c.player.isSneaking() ? 0.5f : 1f;
				
                WorldRenderer.drawBox(matrix, vertex, box, 0.9f, 0f, 0.8f, a);
			}
		});
	    
		//MatterShatter Helper
		HudRenderCallback.EVENT.register(new HudRenderCallback() {

			private World lastWorld;
			private ItemStack lastItem;
			private int lastSlot;
			
			private boolean slotChanged() {
				MinecraftClient m = MinecraftClient.getInstance();
				PlayerInventory inv = m.player.getInventory();
				
				return (lastSlot != inv.getSlotWithStack(lastItem));
			}
			
			@Override
			public void onHudRender(MatrixStack matrix, float delta) {
				MinecraftClient client = MinecraftClient.getInstance();
				if (isFlagClash()) {
					int ps = client.player.getInventory().selectedSlot;
					if (ps<0) ps = 0;
					ItemStack current = client.player.getInventory().getStack(ps);
					if (lastWorld == client.world && !client.player.getAbilities().flying && (lastItem != null && (client.currentScreen == null || client.currentScreen instanceof ChatScreen)
						&& lastItem.getItem() != current.getItem() && lastItem.getItem() != Items.AIR && slotChanged()) && has(lastItem.getItem())) {
								Window w = client.getWindow();
								String str = "Swap back by ["+client.options.keysHotbar[lastSlot].getBoundKeyLocalizedText().getString().toLowerCase()+"]";
								final float y = w.getScaledHeight()/1.55f;
								
								TextRenderer tr = client.textRenderer;
								tr.drawWithShadow(matrix, str, w.getScaledWidth()/2-tr.getWidth(str)/2, y, Color.RED.getRGB());
								client.getItemRenderer().renderInGui(lastItem, w.getScaledWidth()/2-8, (int)(y-18));
					} else {
						lastItem = current;
						lastSlot = client.player.getInventory().getSlotWithStack(lastItem);
					}
					lastWorld = client.world;
				} else lastItem = null;
			}
			
			private boolean has(Item item) {
				MinecraftClient m = MinecraftClient.getInstance();
				final PlayerInventory inv = m.player.getInventory();
				Iterator<ItemStack> it = inv.main.iterator();
				while (it.hasNext()) {
					ItemStack n = it.next();
					if (n.getItem() == item &&
						PlayerInventory.isValidHotbarIndex(inv.getSlotWithStack(n))) return true;
				}
				return false;
			}
        });
		
		// Auto sneak (Only works at Vibrant Vines)
		ClientTickEvents.START_CLIENT_TICK.register(new StartTick() {

			private float yaw, pitch;
			
			@Override
			public void onStartTick(MinecraftClient c) {
				
				if (isFlagClash()) {
					GameOptions o = c.options;
					if (c.player.getYaw() != yaw || c.player.getPitch() != pitch || o.keyLeft.isPressed()||o.keyRight.isPressed()||o.keyForward.isPressed()) {
						if (getAFKTime() >= 10 && nearSpawn()) {
							c.options.keySneak.setPressed(false);
						}
						moved = System.currentTimeMillis();
					}

					if (getAFKTime() >= 10 && !c.player.isSneaking() && nearSpawn())
						c.options.keySneak.setPressed(true);
					yaw = c.player.getYaw();
					pitch = c.player.getPitch();
				}	
			}
			
			private long moved = System.currentTimeMillis();
			public double getAFKTime() {
				return (System.currentTimeMillis()-moved)/1000d;
			}
			public boolean nearSpawn() {
				MinecraftClient m = MinecraftClient.getInstance();
				for (int x = -4; x < 4; x++) {
					for (int y = -2; y < 0; y++) {
						for (int z = -4; z < 4; z++) {
							Block b = m.world.getBlockState(m.player.getBlockPos().add(x, y, z)).getBlock();
							if (b == Blocks.BEACON) return true;
						}
					}
				}
				return false;
			}
			
		});
		
		//Quick Soul Shard # Removed because YES, prob gonna do something alternative
//		ClientTickEvents.START_CLIENT_TICK.register(new StartTick() {
//			ItemStack last;
//			int i = 0;
//			@Override public void onStartTick(MinecraftClient client) {
//				if (isFlagClash()) {
//					PlayerInventory inv = client.player.getInventory();
//					if (last == null) {
//						if (shard.wasPressed() && shard.isPressed()) {
//							last = getHolding(client.player);
//							
//							boolean b = setSlot(Items.FLINT);
//							if (b) {
//								client.options.keyUse.setPressed(false);
//								rotate();
//								i = 1;
//							} else last = null;
//						}
//					} else if (last != null) {
//						if (i == 2) client.options.keyUse.setPressed(true);
//						else if (i >= 3) {
//							int s = inv.getSlotWithStack(last);
//							if (PlayerInventory.isValidHotbarIndex(s)) {
//								inv.selectedSlot = s;
//							} else setRandom(Items.FLINT);
//							last = null;
//							this.i = 0;
//							rotate();
//							client.options.keyUse.setPressed(false);
//						}
//						i++;
//					}
//				}
//			}
//			private void rotate() {
//				MinecraftClient m = MinecraftClient.getInstance();
//				m.player.setYaw(m.player.getYaw()+180);
//				m.player.setPitch(-m.player.getPitch());
//			}
//	    });
	}

	private long countdown;
	public double getCountdown() {
		double d = (countdown-System.currentTimeMillis())/1000d;
		return d < 0 ? 0 : d;
	}
	public void startCountdown(double seconds) {
		countdown = System.currentTimeMillis()+(long)(seconds*1000d);
	}
	
	public ItemStack getHolding(PlayerEntity player) {
		PlayerInventory inv = player.getInventory();
		return inv.getStack(inv.selectedSlot);
	}
	
//	private void setRandom(Item item) {
//		MinecraftClient m = MinecraftClient.getInstance();
//		PlayerInventory inv = m.player.getInventory();
//		Random r = new Random();
//		
//		ItemStack stack = null;
//		int i = 0;
//		while (stack == null || !PlayerInventory.isValidHotbarIndex(inv.getSlotWithStack(stack)) || stack.getItem() == item) {
//			stack = inv.main.get(r.nextInt(PlayerInventory.getHotbarSize()));
//			i++;
//			if (i>1000000) break;
//		}
//		inv.selectedSlot = stack != null ? inv.getSlotWithStack(stack) : 0;
//	}
//	
//	private boolean setSlot(Item item) {
//		MinecraftClient m = MinecraftClient.getInstance();
//		PlayerInventory inv = m.player.getInventory();
//		
//		for (int i = 0; i < inv.main.size(); i++) {
//			ItemStack stack = inv.main.get(i);
//			if (stack.getItem() == item) {
//				int s = inv.getSlotWithStack(stack);
//				if (PlayerInventory.isValidHotbarIndex(s)) {
//					inv.selectedSlot = s;
//					return true;
//				}
//			}
//		}
//		return false;
//	}
	
	private static final String[] ve = {
		"k",
		"m",
		"b",
		"t",
		"q",
		"\u221e"
	};
	public static long toRealValue(String s) {
		try {
			int i = Integer.parseInt(s);
			return i;
		} catch (Exception e) {
			try {
				String suffix = (s.charAt(s.length()-1)+"");
				double d = Double.parseDouble(s.substring(0, s.length()-1));
	
				int io = (Arrays.asList(ve).indexOf(suffix)+1);
				StringBuilder sb = new StringBuilder("1");
				for (int i = 0; i < io; i++) sb.append("000");
				
				
				return (long)(d*Long.parseLong(sb.toString()));
			} catch (Exception e2) {
				System.err.println("toRealValue("+(s == null ? "null" : "\""+s+"\"")+")");
				e2.printStackTrace();
				return 0;
			}
		}
	}
	static String toVisualValue(long i) {
		String is = i+"";
		
		int io = ((is.length()-1)/3)-1;
		String suffix = ve[io];
		
		String p = is.substring(0, is.length()-io*3);
		String prefix = is.substring(0, p.length()/2)+"."+is.substring(p.length()/2, p.length()).substring(0, 2);
		return prefix+suffix;
	}
	
	public static String getStat(String stat) {
		if (!isFlagClash()) return "0";
		ArrayList<String> sb = getScoreboard();
		for (int i = 0; i < sb.size(); i++) {
			String sfs = sb.get(i);
			if (sfs.contains(stat)) {
				String s = sfs.split(stat+": ")[1];
				s = s.substring(0, s.length()-4);
				return s;
			}
		}
		return "0";
	}
	
	public static long getGPS() {
		String m = getStat("Gps");
		if (m == null || m.isEmpty()) return 1;
		if (m.contains(" ")) return toRealValue(m.split(" ")[0]);
		else {
			long l = toRealValue(m);
			return l == 0 ? 1 : l;
		}
	}
	public static double getMultiplier() {
		try {
			String[] m = getStat("Gps").split(" ");
			return Double.parseDouble(m[1].substring(2, m[1].length()-1));
		} catch (Exception e) {
			return 1;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ArrayList<String> getScoreboard() {
		ArrayList<String> strings = new ArrayList<>();
		MinecraftClient m = MinecraftClient.getInstance();
		Scoreboard scoreboard = m.world.getScoreboard();
		if (scoreboard == null) return strings;
		
		Iterator<ScoreboardObjective> os = scoreboard.getObjectives().iterator();
		if (!os.hasNext()) return strings;
		ScoreboardObjective objective = os.next();

	      Collection<ScoreboardPlayerScore> collectionf = scoreboard.getAllPlayerScores(objective);
	      List<ScoreboardPlayerScore> list = (List<ScoreboardPlayerScore>)collectionf.stream().filter((score) -> {
	         return score.getPlayerName() != null && !score.getPlayerName().startsWith("#");
	      }).collect(Collectors.toList());
	      Object collection;
	      if (list.size() > 15) {
	         collection = Lists.newArrayList(Iterables.skip(list, collectionf.size() - 15));
	      } else {
	         collection = list;
	      }

	      List<Pair<ScoreboardPlayerScore, Text>> list2 = Lists.newArrayListWithCapacity(((Collection)collection).size());

	      ScoreboardPlayerScore scoreboardPlayerScore;
	      MutableText text2;
	      for(Iterator var11 = ((Collection)collection).iterator(); var11.hasNext();) {
	         scoreboardPlayerScore = (ScoreboardPlayerScore)var11.next();
	         Team team = scoreboard.getPlayerTeam(scoreboardPlayerScore.getPlayerName());
	         text2 = Team.decorateName(team, new LiteralText(scoreboardPlayerScore.getPlayerName()));
	         list2.add(Pair.of(scoreboardPlayerScore, text2));
	      }
	      Iterator var18 = list2.iterator();

	      while(var18.hasNext()) {
	         Pair<ScoreboardPlayerScore, Text> pair = (Pair)var18.next();
	         Text text3 = (Text)pair.getSecond();
	         strings.add(text3.getString());
	      }
	      return strings;
	}
	
	public boolean enabled;
	public static boolean isFlagClash() {
		MinecraftClient m = MinecraftClient.getInstance();
		if (m.player == null) return false;
//		if (m.getCurrentServerEntry() != null) {
//			String a = m.getCurrentServerEntry().address.toLowerCase();
//			if (a.contains("flagclash")) return true;
//		}
		return get().enabled;
	}

	public static FCA get() {
		return fca;
	}
	public static String round(double value) {
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.DOWN);
		return df.format(value).replace(",", ".");
	}
}
