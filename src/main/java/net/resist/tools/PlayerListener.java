package net.resist.tools;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
public class PlayerListener {
    private final Main plugin;
    public PlayerListener(Main instance) {
        plugin = instance;
    }
    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event, @Root Player player) throws Exception {
        if (Config.blockBuildBeforeLogin) {
            event.setCancelled(checkForAccepted(player, Config.mustLoginMsg));
       }
    	event.setCancelled(true);
    }
    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event, @Root Player player) throws Exception {
        if (Config.blockBuildBeforeLogin) {
            event.setCancelled(checkForAccepted(player, Config.mustLoginMsg));
        }
    	event.setCancelled(true);
    }
    @Listener
    public void beforeCommand(SendCommandEvent event, @Root Player player) throws Exception {
        if (Config.blockCommandsBeforeLogin) {
            String command = event.getCommand().toLowerCase();
            if (command.equals("login")) {
                return;
            } else {
                event.setCancelled(checkForAccepted(player, Config.mustLoginMsg));
            }
        }
    }
    @Listener
    public void onEntityMove(MoveEntityEvent event, @Root Player player) {
        if (Config.blockMovementBeforeLogin) {
            if (plugin.getDataStore().getLoggedIn().contains(player.getName().toString())) {
            	return;
            } 
            event.setCancelled(true);
            //plugin.sendMessage(player, Config.chatPrefix + Config.mustLoginMsg);
        }
    }
    @Listener
    public void onDamage(DamageEntityEvent event, @Root Player player) {
        if (Config.blockMovementBeforeLogin) {
            if (plugin.getDataStore().getLoggedIn().contains(player.getName().toString())) {
                event.setCancelled(false);
                return;
            } 
            event.setCancelled(true);
        }
    }    
    private boolean checkForAccepted(Player player, String message) throws Exception {
        if (plugin.getDataStore().getLoggedIn().contains(player.getName().toString())) {
            return false;
        }
        plugin.sendMessage(player, Config.chatPrefix + Config.mustLoginMsg);
        return true;			
    }
}