package tk.slicecollections.maxteer.lobby.hook.nms;

import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import tk.slicecollections.maxteer.deliveries.Delivery;
import tk.slicecollections.maxteer.libraries.holograms.HologramLibrary;
import tk.slicecollections.maxteer.libraries.holograms.api.Hologram;
import tk.slicecollections.maxteer.lobby.Language;
import tk.slicecollections.maxteer.nms.network.NMSPacketAccess;
import tk.slicecollections.maxteer.nms.network.NMSPacketInterceptor;
import tk.slicecollections.maxteer.player.Profile;
import tk.slicecollections.maxteer.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class HologramPacketHandler implements NMSPacketInterceptor.PacketHandler {

  @Override
  public Object onPacketReceiving(Player player, Object packet) {
    return packet;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object onPacketSending(Player player, Object packet) {
    if (!(packet instanceof PacketPlayOutEntityMetadata)) {
      return packet;
    }

    int entityId = (Integer) NMSPacketAccess.get(packet, int.class, 0);
    Entity entity = HologramLibrary.getHologramEntity(entityId);
    if (entity == null || !HologramLibrary.isHologramEntity(entity)) {
      return packet;
    }

    Hologram hologram = HologramLibrary.getHologram(entity);
    if (hologram == null) {
      return packet;
    }

    List<DataWatcher.WatchableObject> watchables =
        (List<DataWatcher.WatchableObject>) NMSPacketAccess.get(packet, List.class, 0);
    DataWatcher.WatchableObject customName = null;
    List<DataWatcher.WatchableObject> changed = new ArrayList<>(watchables.size());

    for (DataWatcher.WatchableObject watchable : watchables) {
      if (watchable.a() == 2) {
        customName = watchable;
      } else if (watchable.a() != 3) {
        changed.add(watchable);
      }
    }

    if (customName == null || !(customName.b() instanceof String)) {
      return packet;
    }

    String name = (String) customName.b();
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

    changed.add(new DataWatcher.WatchableObject(4, 2, name));
    changed.add(new DataWatcher.WatchableObject(0, 3, (byte) (name.isEmpty() ? 0 : 1)));
    watchables.clear();
    watchables.addAll(changed);
    return packet;
  }
}
