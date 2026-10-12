package tk.slicecollections.maxteer.lobby.cmd.ml;

import org.bukkit.entity.Player;
import tk.slicecollections.maxteer.lobby.cmd.SubCommand;

public class UpdateCommand extends SubCommand {

  public UpdateCommand() {
    super("atualizar", "atualizar", "Atualizar o mLobby.", true);
  }

  @Override
  public void perform(Player player, String[] args) {
    player.sendMessage("§eO mLobby não consulta atualizações online. Use um JAR verificado e reinicie o servidor para atualizar.");
  }
}
