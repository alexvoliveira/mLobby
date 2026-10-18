package tk.slicecollections.maxteer.lobby.hook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import tk.slicecollections.maxteer.lobby.Language;
import tk.slicecollections.maxteer.lobby.Main;
import tk.slicecollections.maxteer.lobby.hook.hotbar.LHotbarActionType;
import tk.slicecollections.maxteer.lobby.hook.nms.HologramPacketHandler;
import tk.slicecollections.maxteer.nms.network.NMSPacketInterceptor;
import tk.slicecollections.maxteer.player.Profile;
import tk.slicecollections.maxteer.player.hotbar.Hotbar;
import tk.slicecollections.maxteer.player.hotbar.HotbarAction;
import tk.slicecollections.maxteer.player.hotbar.HotbarActionType;
import tk.slicecollections.maxteer.player.hotbar.HotbarButton;
import tk.slicecollections.maxteer.player.scoreboard.MScoreboard;
import tk.slicecollections.maxteer.player.scoreboard.scroller.ScoreboardScroller;
import tk.slicecollections.maxteer.plugin.config.MConfig;

public class LCoreHook {

  public static void setupHook() {
    setupHotbars();
    new BukkitRunnable() {
      @Override
      public void run() {
        Profile.listProfiles().forEach(profile -> {
          if (profile.getScoreboard() != null) {
            profile.getScoreboard().scroll();
          }
        });
      }
    }.runTaskTimer(Main.getInstance(), 0L, Language.scoreboards$scroller$every_tick);

    new BukkitRunnable() {
      @Override
      public void run() {
        Profile.listProfiles().forEach(profile -> {
          if (!profile.playingGame() && profile.getScoreboard() != null) {
            profile.update();
          }
        });
      }
    }.runTaskTimer(Main.getInstance(), 0L, 20L);

    NMSPacketInterceptor.registerListener(Main.getInstance(), new HologramPacketHandler());
  }

  public static void reloadScoreboard(Profile profile) {
    Player player = profile.getPlayer();
    List<String> lines = new ArrayList<>(Language.scoreboards$lobby);
    Collections.reverse(lines);
    profile.setScoreboard(new MScoreboard() {
      @Override
      public void update() {
        this.updateHealth();
        for (int index = 0; index < lines.size(); index++) {
          String line = lines.get(index);
          line = PlaceholderAPI.setPlaceholders(player, line);
          this.add(index + 1, line);
        }
      }
    }.scroller(new ScoreboardScroller(Language.scoreboards$scroller$titles)).to(profile.getPlayer()).build());
    profile.update();
    profile.getScoreboard().scroll();
  }

  private static void setupHotbars() {
    HotbarActionType.addActionType("lobby", new LHotbarActionType());
    MConfig config = Main.getInstance().getConfig("hotbar");
    for (String id : new String[] {"lobby"}) {
      Hotbar hotbar = new Hotbar(id);
      ConfigurationSection section = config.getSection(id);
      for (String button : section.getKeys(false)) {
        try {
          hotbar.getButtons().add(new HotbarButton(section.getInt(button + ".slot"),
            new HotbarAction(section.getString(button + ".execute")), section.getString(button + ".icon")));
        } catch (Exception ex) {
          Main.getInstance().getLogger().log(Level.WARNING,
            "Falha ao carregar o botao \"" + button + "\" da hotbar \"" + id + "\": ", ex);
        }
      }
      Hotbar.addHotbar(hotbar);
    }
  }
}
