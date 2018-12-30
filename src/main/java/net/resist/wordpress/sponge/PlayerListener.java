package net.resist.wordpress.sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
public class PlayerListener{
    private final Main plugin;
    public PlayerListener(Main instance){
        plugin=instance;
    }
    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event,@Root Player player) throws Exception{
        String playerName = player.getName().toString();        
        if(Config.blockBuildBeforeLogin){
            event.setCancelled(checkForAccepted(player,Config.mustLoginMsg));
        }
    }
    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event,@Root Player player) throws Exception{
        String playerName = player.getName().toString();
        if(Config.blockBuildBeforeLogin){
            event.setCancelled(checkForAccepted(player,Config.mustLoginMsg));
        }
    }
    @Listener
    public void beforeCommand(SendCommandEvent event,@Root Player player) throws Exception{
        if(Config.blockCommandsBeforeLogin){
            String command=event.getCommand().toLowerCase();
            if(command.equals("login")){
                return;
            }
            event.setCancelled(checkForAccepted(player,Config.mustLoginMsg));
        }
    }
    @Listener
    public void onEntityMove(MoveEntityEvent event,@Root Player player){
        String playerName = player.getName().toString();        
        if(Config.blockMovementBeforeLogin){
            if(plugin.getDataStore().getLoggedIn().contains(playerName)){
                return;
            }
            event.setCancelled(true);
        }
    }
    /*@Listener
    public void onDamage(DamageEntityEvent event,@Root Player player){
		try {
			if(Config.blockDamageBeforeLogin){
				event.setCancelled(checkForAccepted(player,Config.mustLoginMsg));
			}
		} catch (Exception e) {}
    }*/
    private boolean checkForAccepted(Player player,String message) throws Exception{
        String playerName = player.getName().toString();                
        if (plugin.getDataStore().getLoggedIn().contains(playerName)) {
            return false;
        }
        plugin.sendMessage(player, Config.chatPrefix + message);
        return true;
    }
}