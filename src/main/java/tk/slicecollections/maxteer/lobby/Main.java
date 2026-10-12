
package tk.slicecollections.maxteer.lobby;

import org.bukkit.Bukkit;
import tk.slicecollections.maxteer.Core;
import tk.slicecollections.maxteer.lobby.cmd.Commands;
import tk.slicecollections.maxteer.lobby.hook.LCoreHook;
import tk.slicecollections.maxteer.lobby.listeners.Listeners;
import tk.slicecollections.maxteer.lobby.lobby.DeliveryNPC;
import tk.slicecollections.maxteer.lobby.lobby.Lobby;
import tk.slicecollections.maxteer.lobby.lobby.PlayNPC;
import tk.slicecollections.maxteer.lobby.tagger.TagUtils;
import tk.slicecollections.maxteer.player.Profile;
import tk.slicecollections.maxteer.player.hotbar.Hotbar;
import tk.slicecollections.maxteer.plugin.MPlugin;
import tk.slicecollections.maxteer.utils.BukkitUtils;

import java.io.File;
import java.io.FileInputStream;

public class Main extends MPlugin {

  private static Main instance;
  private static boolean validInit;
  public static String currentServerName;

  @Override
  public void start() {
    instance = this;
  }

  @Override
  public void load() {}

  @Override
  public void enable() {
    saveDefaultConfig();
    currentServerName = getConfig().getString("lobby");
    if (getConfig().getString("spawn") != null) {
      Core.setLobby(BukkitUtils.deserializeLocation(getConfig().getString("spawn")));
    }

    Language.setupLanguage();
    
    LCoreHook.setupHook();
    Lobby.setupLobbies();
    
    PlayNPC.setupNPCs();
    DeliveryNPC.setupNPCs();

    Commands.setupCommands();
    Listeners.setupListeners();

    validInit = true;
    this.getLogger().info("O plugin foi ativado.");
  }

  @Override
  public void disable() {
    if (validInit) {
      PlayNPC.listNPCs().forEach(PlayNPC::destroy);
      TagUtils.reset();
    }

    File update = new File("plugins/mLobby/update","mLobby.jar");
    if (update.exists()) {
      try {
        this.getFileUtils().deleteFile(new File("plugins/" + update.getName()));
        this.getFileUtils().copyFile(new FileInputStream(update), new File("plugins/" + update.getName()));
        this.getFileUtils().deleteFile(update.getParentFile());
        this.getLogger().info("Update do mLobby aplicada.");
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    this.getLogger().info("O plugin foi desativado.");
  }

  public static Main getInstance() {
    return instance;
  }
}
