package tk.slicecollections.maxteer.lobby.cmd.ml;

import org.bukkit.entity.Player;
import tk.slicecollections.maxteer.lobby.cmd.SubCommand;
import tk.slicecollections.maxteer.lobby.lobby.LobbyEntryTask;

public class UpdateCommand extends SubCommand {

  public UpdateCommand() {
    super("atualizar", "atualizar", "Atualizar o mLobby.", true);
  }

  @Override
  public void perform(Player player, String[] args) {
    player.sendMessage("§aO plugin já se encontra em sua última versão.");
  }
}
