package me.william278.bungeetowny.command;

import me.william278.bungeetowny.data.DataManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class ClaimCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        DataManager.claimChunk(player);
    }

}