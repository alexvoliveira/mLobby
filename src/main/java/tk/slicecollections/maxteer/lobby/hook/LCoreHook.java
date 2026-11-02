package tk.slicecollections.maxteer.lobby.hook;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import tk.slicecollections.maxteer.deliveries.Delivery;
import tk.slicecollections.maxteer.libraries.holograms.HologramLibrary;
import tk.slicecollections.maxteer.libraries.holograms.api.Hologram;
import tk.slicecollections.maxteer.lobby.Language;
import tk.slicecollections.maxteer.lobby.Main;
import tk.slicecollections.maxteer.lobby.hook.hotbar.LHotbarActionType;
import tk.slicecollections.maxteer.player.Profile;
import tk.slicecollections.maxteer.player.hotbar.Hotbar;
import tk.slicecollections.maxteer.player.hotbar.HotbarAction;
import tk.slicecollections.maxteer.player.hotbar.HotbarActionType;
import tk.slicecollections.maxteer.player.hotbar.HotbarButton;
import tk.slicecollections.maxteer.player.scoreboard.MScoreboard;
import tk.slicecollections.maxteer.player.scoreboard.scroller.ScoreboardScroller;
import tk.slicecollections.maxteer.plugin.config.MConfig;
import tk.slicecollections.maxteer.utils.StringUtils;

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

    ProtocolLibrary.getProtocolManager().addPacketListener(new HologramAdapter());
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

  private static final class HologramAdapter extends PacketAdapter {

    private HologramAdapter() {
      super(params().plugin(HologramLibrary.getPlugin()).types(PacketType.Play.Server.ENTITY_METADATA));
    }

    @Override
    public void onPacketSending(PacketEvent event) {
      PacketContainer packet = event.getPacket();
      if (packet.getType() != PacketType.Play.Server.ENTITY_METADATA) {
        return;
      }

      Player player = event.getPlayer();
      int entityId = packet.getIntegers().read(0);
      Entity entity = HologramLibrary.getHologramEntity(entityId);
      if (entity == null || !HologramLibrary.isHologramEntity(entity)) {
        return;
      }

      Hologram hologram = HologramLibrary.getHologram(entity);
      if (hologram == null) {
        return;
      }

      WrappedWatchableObject customName = null;
      WrappedWatchableObject visible = new WrappedWatchableObject(3, (byte) 1);
      List<WrappedWatchableObject> watchables = new ArrayList<>();
      for (WrappedWatchableObject watchable : packet.getWatchableCollectionModifier().read(0)) {
        if (watchable.getIndex() == 2) {
          customName = new WrappedWatchableObject(2, watchable.getValue());
        } else if (watchable.getIndex() != 3) {
          watchables.add(watchable);
        }
      }

      if (customName == null || !(customName.getValue() instanceof String)) {
        return;
      }

      String name = (String) customName.getValue();
      if (name.contains("{player}") || name.contains("{displayname}")) {
        name = name.replace("{player}", player.getName()).replace("{displayname}", player.getDisplayName());
      }

      Profile profile = Profile.getProfile(player.getName());
      if (profile != null && name.contains("{deliveries}")) {
        long deliveries = Delivery.listDeliveries().stream()
          .filter(delivery -> delivery.hasPermission(player)
            && !profile.getDeliveriesContainer().alreadyClaimed(delivery.getId()))
          .count();
        if (deliveries == 0) {
          name = "";
        } else {
          name = Language.lobby$npc$deliveries$deliveries
            .replace("{deliveries}", StringUtils.formatNumber(deliveries))
            .replace("{s}", deliveries > 1 ? "s" : "");
        }
      }

      customName.setValue(name);
      if (name.isEmpty()) {
        visible.setValue((byte) 0);
      }
      watchables.add(customName);
      watchables.add(visible);

      PacketContainer replacement = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
      replacement.getIntegers().write(0, entityId);
      replacement.getWatchableCollectionModifier().write(0, watchables);
      event.setPacket(replacement);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {}
  }
}
