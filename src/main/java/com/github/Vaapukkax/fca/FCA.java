package com.github.Vaapukkax.fca;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
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
import net.minecraft.util.math.Direction;

public class FCA implements ModInitializer {
	
	private static FCA fca;
	public KeyBinding shard = new KeyBinding(
			"Quick Shard",
		    InputUtil.Type.KEYSYM,
		    GLFW.GLFW_KEY_R,
		    "key.categories.misc"
	);
	
	public BlockPos flag;
	public Text actionbar;
	
	@Override
	public void onInitialize() {
		fca = this;
		KeyBindingHelper.registerKeyBinding(shard);
	
		// Something illegal
		ClientTickEvents.START_CLIENT_TICK.register(new StartTick() {

			private boolean b;
			ArrayList<BlockPos> bps = new ArrayList<>();
			
			@Override
			public void onStartTick(MinecraftClient client) {
				if (actionbar == null) return;
				String om = actionbar.getString();
				if (isFlagClash() && om.contains("Seconds")) {
					if (!b) {
						bps.clear();
					}
					b = true;
					for (int x = -5; x < 5; x++) {
						for (int y = -5; y < 5; y++) {
							for (int z = -5; z < 5; z++) {
								BlockPos p = client.player.getBlockPos().add(x, y, z);
								if (!bps.contains(p) && client.world.getBlockState(p).getBlock() == Blocks.TURTLE_EGG) {
									client.interactionManager.attackBlock(p, Direction.DOWN);
									bps.add(p);
								}
							}
						}
					}
				} else b = false;
			}
			
		});
		
		//Quick Soul Shard
		ClientTickEvents.START_CLIENT_TICK.register(new StartTick() {
			ItemStack last;
			int i = 0;
			@Override public void onStartTick(MinecraftClient client) {
				if (isFlagClash()) {
					PlayerInventory inv = client.player.getInventory();
					if (last == null) {
						if (shard.wasPressed() && shard.isPressed()) {
							last = getHolding(client.player);
							
							boolean b = setSlot(Items.FLINT);
							if (b) {
								client.options.keyUse.setPressed(false);
								rotate();
								i = 1;
							} else last = null;
						}
					} else if (last != null) {
						if (i == 2) client.options.keyUse.setPressed(true);
						else if (i >= 3) {
							int s = inv.getSlotWithStack(last);
							if (PlayerInventory.isValidHotbarIndex(s)) {
								inv.selectedSlot = s;
							} else setRandom(Items.FLINT);
							last = null;
							this.i = 0;
							rotate();
							client.options.keyUse.setPressed(false);
						}
						i++;
					}
				}
			}
			private void rotate() {
				MinecraftClient m = MinecraftClient.getInstance();
				m.player.setYaw(m.player.getYaw()+180);
				m.player.setPitch(-m.player.getPitch());
			}
	    });
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
	
	private void setRandom(Item item) {
		MinecraftClient m = MinecraftClient.getInstance();
		PlayerInventory inv = m.player.getInventory();
		Random r = new Random();
		
		ItemStack stack = null;
		int i = 0;
		while (stack == null || !PlayerInventory.isValidHotbarIndex(inv.getSlotWithStack(stack)) || stack.getItem() == item) {
			stack = inv.main.get(r.nextInt(PlayerInventory.getHotbarSize()));
			i++;
			if (i>1000000) break;
		}
		inv.selectedSlot = stack != null ? inv.getSlotWithStack(stack) : 0;
	}
	
	private boolean setSlot(Item item) {
		MinecraftClient m = MinecraftClient.getInstance();
		PlayerInventory inv = m.player.getInventory();
		
		for (int i = 0; i < inv.main.size(); i++) {
			ItemStack stack = inv.main.get(i);
			if (stack.getItem() == item) {
				int s = inv.getSlotWithStack(stack);
				if (PlayerInventory.isValidHotbarIndex(s)) {
					inv.selectedSlot = s;
					return true;
				}
			}
		}
		return false;
	}

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
	
    public boolean contains(String[] list, String o) {
        for (String s : list) {
            if (o.equalsIgnoreCase(s)) return true;
        }
        return false;
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
//		System.out.println("RAW: "+getStat("Gps"));
		String[] m = getStat("Gps").split(" ");
		if (m.length<2) return 1;
//		System.out.println("Ye: "+m[0]+", "+m[1]);
//		System.out.println("Sem final "+m[1].substring(2, m[1].length()-1));
//		System.out.println("Final: "+Double.parseDouble(m[1].substring(2, m[1].length()-1)));
		return Double.parseDouble(m[1].substring(2, m[1].length()-1));
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
	
	public static boolean isFlagClash() {
		MinecraftClient m = MinecraftClient.getInstance();
		if (m.player == null) return false;
		if (m.getCurrentServerEntry() != null) {
			String a = m.getCurrentServerEntry().address.toLowerCase();
			if (a.contains("minehut")) return true;
		}
		return false;
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
