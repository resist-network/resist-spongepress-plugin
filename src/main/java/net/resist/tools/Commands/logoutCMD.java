package net.resist.tools.Commands;
import net.resist.tools.Config;
import net.resist.tools.Main;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
public class logoutCMD implements CommandExecutor {
    private final Main plugin;
    public logoutCMD(Main instance) {
        plugin = instance;
    }
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Player player = (Player) src;
		String playerName = player.getName();
		plugin.getDataStore().removePlayer(playerName);
        plugin.sendMessage(player, Config.chatPrefix + Config.logoutSuccess);
		return CommandResult.success();
    }
}