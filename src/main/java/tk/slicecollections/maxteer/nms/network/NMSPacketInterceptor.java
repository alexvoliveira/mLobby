package tk.slicecollections.maxteer.nms.network;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Compile-time API contract for the newer mCore packet interceptor.
 * Gradle excludes this class and its nested interface from mLobby.jar;
 * mCore supplies the implementation at runtime.
 */
public final class NMSPacketInterceptor {

  public interface PacketHandler {
    Object onPacketSending(Player player, Object packet);
    Object onPacketReceiving(Player player, Object packet);
  }

  private NMSPacketInterceptor() {}

  public static void registerListener(Plugin owner, PacketHandler handler) {
    throw new UnsupportedOperationException("Compile-time contract only");
  }

  public static void unregisterListeners(Plugin owner) {
    throw new UnsupportedOperationException("Compile-time contract only");
  }
}
