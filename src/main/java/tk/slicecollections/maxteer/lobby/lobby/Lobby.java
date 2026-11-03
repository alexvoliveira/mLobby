package tk.slicecollections.maxteer.lobby.lobby;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;
import tk.slicecollections.maxteer.lobby.Main;
import tk.slicecollections.maxteer.plugin.config.MConfig;
import tk.slicecollections.maxteer.servers.ServerItem;
import tk.slicecollections.maxteer.servers.ServerPing;

public class Lobby {

  private int slot;
  private ServerPing serverPing;
  private int maxPlayers;
  private String icon;
  private String serverName;

  public Lobby(int slot, String icon, int maxPlayers, String ip, String serverName) {
    this.slot = slot;
    this.icon = icon;
    this.serverPing = new ServerPing(
        new InetSocketAddress(ip.split(":")[0], Integer.parseInt(ip.split(":")[1])));
    this.maxPlayers = maxPlayers;
    this.serverName = serverName;
  }

  public void fetch() {
    this.serverPing.fetch();
    ServerItem.SERVER_COUNT.put(this.serverName, this.serverPing.getOnline());
  }

  public int getSlot() {
    return this.slot;
  }

  public String getIcon() {
    return this.icon;
  }

  public int getPlayers() {
    return this.serverName.equals(Main.currentServerName) ? Bukkit.getOnlinePlayers().size()
        : ServerItem.getServerCount(this.serverName);
  }

  public int getMaxPlayers() {
    return this.maxPlayers;
  }

  public String getServerName() {
    return this.serverName;
  }

  private static final List<Lobby> LOBBIES = new ArrayList<>();
  public static final List<Lobby> QUERY = new ArrayList<>();
  public static final MConfig CONFIG = Main.getInstance().getConfig("lobbies");
  public static final List<String> WARNINGS = new ArrayList<>();

  public static void setupLobbies() {
    long refreshInterval = Math.max(20L, CONFIG.getLong("refresh-interval-ticks", 20L * 30L));
    new LobbyEntryTask().runTaskTimerAsynchronously(Main.getInstance(), 0L, refreshInterval);

    for (String key : CONFIG.getSection("items").getKeys(false)) {
      String servername = CONFIG.getString("items." + key + ".servername");
      if (servername == null) {
        WARNINGS.add(" - (" + key + ") servername ausente");
        continue;
      }

      String[] serverParts = servername.split(" ; ", 2);
      if (serverParts.length != 2) {
        WARNINGS.add(" - (" + key + ") " + servername);
        continue;
      }

      String[] address = serverParts[0].split(":", 2);
      if (address.length != 2) {
        WARNINGS.add(" - (" + key + ") endereço inválido: " + serverParts[0]);
        continue;
      }

      try {
        int port = Integer.parseInt(address[1]);
        if (port < 1 || port > 65535 || address[0].trim().isEmpty()) {
          throw new IllegalArgumentException("Endereço ou porta inválida");
        }
        LOBBIES.add(new Lobby(CONFIG.getInt("items." + key + ".slot"),
          CONFIG.getString("items." + key + ".icon"),
          CONFIG.getInt("items." + key + ".max-players"), address[0].trim() + ":" + port,
          serverParts[1].trim()));
      } catch (IllegalArgumentException ex) {
        WARNINGS.add(" - (" + key + ") endereço inválido: " + serverParts[0]);
      }
    }

    for (Lobby lobby : LOBBIES) {
      if (!ServerItem.alreadyQuerying(lobby.getServerName())) {
        QUERY.add(lobby);
      }
    }
  }

  public static Collection<Lobby> listLobbies() {
    return LOBBIES;
  }
}
