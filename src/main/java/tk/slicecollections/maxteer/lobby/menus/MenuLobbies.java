package tk.slicecollections.maxteer.lobby.menus;

import static tk.slicecollections.maxteer.lobby.lobby.Lobby.CONFIG;
import static tk.slicecollections.maxteer.lobby.lobby.Lobby.listLobbies;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import tk.slicecollections.maxteer.Core;
import tk.slicecollections.maxteer.lobby.Main;
import tk.slicecollections.maxteer.lobby.lobby.Lobby;
import tk.slicecollections.maxteer.menus.PlayerMenuInventory;
import tk.slicecollections.maxteer.player.Profile;
import tk.slicecollections.maxteer.shaded.inventoryapi.editor.InventoryEditor;
import tk.slicecollections.maxteer.shaded.inventoryapi.viewer.Viewer;
import tk.slicecollections.maxteer.utils.BukkitUtils;
import tk.slicecollections.maxteer.utils.StringUtils;

/** Lobby selector backed by the inventory API bundled by mCore. */
public class MenuLobbies extends PlayerMenuInventory implements Listener {

  private Inventory inventory;
  private BukkitTask updateTask;

  public MenuLobbies(Profile profile) {
    super(profile.getPlayer(), "mlobby.lobbies", CONFIG.getString("title"), CONFIG.getInt("rows"));
    Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    openForPlayer();
    updateTask = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
      if (inventory != null && player.getOpenInventory().getTopInventory().equals(inventory)) {
        updateInventory(player);
      } else {
        cancel();
      }
    }, 20L, 20L);
  }

  @Override
  protected void configureInventory(Viewer viewer, InventoryEditor editor) {
    inventory = editor.getInventory();
  }

  @Override
  protected void update(Viewer viewer, InventoryEditor editor) {
    for (int slot = 0; slot < editor.getInventory().getSize(); slot++) {
      editor.setEmptyItem(slot);
    }

    for (Lobby lobby : listLobbies()) {
      ItemStack icon = BukkitUtils.deserializeItemStack(
        lobby.getIcon()
          .replace("{players}", StringUtils.formatNumber(lobby.getPlayers()))
          .replace("{maxplayers}", StringUtils.formatNumber(lobby.getMaxPlayers()))
          .replace("{description}", StringUtils.formatColors(CONFIG.getString(
            "messages.description." + (isCurrentServer(lobby) ? "current" : "connect")))));

      if (isCurrentServer(lobby) && icon != null && icon.getType() != Material.AIR) {
        BukkitUtils.putGlowEnchantment(icon);
      }
      setMenuItem(editor, lobby.getSlot(), icon);
    }
  }

  @Override
  protected void onClick(int slot) {
    Lobby selected = null;
    for (Lobby lobby : listLobbies()) {
      if (lobby.getSlot() == slot) {
        selected = lobby;
        break;
      }
    }
    if (selected == null || isCurrentServer(selected) || selected.getPlayers() >= selected.getMaxPlayers()) {
      return;
    }

    Profile profile = Profile.getProfile(player.getName());
    if (profile == null) {
      player.closeInventory();
      return;
    }

    Core.sendServer(profile, selected.getServerName());
    player.closeInventory();
  }

  private boolean isCurrentServer(Lobby lobby) {
    return Main.currentServerName != null && Main.currentServerName.equalsIgnoreCase(lobby.getServerName());
  }

  private void cancel() {
    if (updateTask != null) {
      updateTask.cancel();
      updateTask = null;
    }
    HandlerList.unregisterAll(this);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    if (event.getPlayer().equals(player)) {
      cancel();
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (inventory != null && event.getPlayer().equals(player) && event.getInventory().equals(inventory)) {
      cancel();
    }
  }
}
