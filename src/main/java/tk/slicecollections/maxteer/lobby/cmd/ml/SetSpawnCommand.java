package tk.slicecollections.maxteer.lobby.cmd.ml;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tk.slicecollections.maxteer.Core;
import tk.slicecollections.maxteer.lobby.Main;
import tk.slicecollections.maxteer.lobby.cmd.SubCommand;
import tk.slicecollections.maxteer.utils.BukkitUtils;

import java.util.ArrayList;
import java.util.List;

public class SetSpawnCommand extends SubCommand {

  private static final List<String> BUILDERS = new ArrayList<>();

  public SetSpawnCommand() {
    super("setspawn", "setspawn", "Setar o spawn do servidor.", true);
  }

  @Override
  public void perform(CommandSender sender, String[] args) {}

  @Override
  public void perform(Player player, String[] args) {
    Location location = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
    location.setYaw(player.getLocation().getYaw());
    location.setPitch(player.getLocation().getPitch());
    Main.getInstance().getConfig().set("spawn", BukkitUtils.serializeLocation(location));
    Main.getInstance().saveConfig();
    Core.setLobby(location);
    player.sendMessage("§aSpawn setado.");
  }

  public static void remove(Player player) {
    BUILDERS.remove(player.getName());
  }

  public static boolean isBuilder(Player player) {
    return BUILDERS.contains(player.getName());
  }
}
