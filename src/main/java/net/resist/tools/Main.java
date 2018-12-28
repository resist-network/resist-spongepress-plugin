package net.resist.tools;

import com.google.inject.Inject;
import net.resist.tools.Commands.loginCMD;
import net.resist.tools.Commands.logoutCMD;
import net.resist.tools.Commands.setpassCMD;
import net.resist.tools.Database.DataStoreManager;
import net.resist.tools.Database.IDataStore;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Plugin(id = "resist-tools", name = "Resist Tools", version = "1.0.0", description = "Resist.Network Tools Plugin.")
public class Main {

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    public Path defaultConf;

    @Inject
    @DefaultConfig(sharedRoot = false)
    public File defaultConfFile;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;
	
    public File userFile;

    public Config config;

    private DataStoreManager dataStoreManager;

    private CommandManager cmdManager = Sponge.getCommandManager();

    public List<String> readRules = new ArrayList<String>();

    @Listener
    public void Init(GameInitializationEvent event) throws IOException, ObjectMappingException {
        Sponge.getEventManager().registerListeners(this, new PlayerListener(this));
        config = new Config(this);
        loadCommands();
        userFile = new File(configDir.toFile(), "users.dat");
    }

    @Listener
    public void onServerAboutStart(GameAboutToStartServerEvent event) {
        dataStoreManager = new DataStoreManager(this);
        if (dataStoreManager.load()) {
            getLogger().info("Database is Loading...");
        } else {
            getLogger().error("Unable to load a database please check your Console/Config!");
        }
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) throws IOException {
        logger.info("Plugin Loaded!");
    }

    @Listener
    public void onPluginReload(GameReloadEvent event) throws IOException, ObjectMappingException {
        getUsersWhoReadRules().clear();
        this.config = new Config(this);
        loadDataStore();
    }

    private void loadCommands() {


        // /login
        CommandSpec login = CommandSpec.builder()
                .description(Text.of("Login to the Server."))
                .executor(new loginCMD(this))
                .build();
        cmdManager.register(this, login, "login");
        // /logout
        CommandSpec logout = CommandSpec.builder()
                .description(Text.of("Logout of the Server."))
                .executor(new logoutCMD(this))
                .build();
        cmdManager.register(this, logout, "logout");
        // /logout
        CommandSpec setpass = CommandSpec.builder()
                .description(Text.of("Set new password on the Server."))
                .executor(new setpassCMD(this))
                .build();
        cmdManager.register(this, setpass, "setpass");			
    }

    public void loadDataStore() {
        if (dataStoreManager.load()) {
            getLogger().info("Database Loaded!");
        } else {
            getLogger().error("Unable to load a database please check your Console/Config!");
        }
    }

    public List getUsersWhoReadRules() {
        return readRules;
    }

    public IDataStore getDataStore() {
        return dataStoreManager.getDataStore();
    }

    @Listener
    public void onPlayerLogin(ClientConnectionEvent.Join event, @Root Player player) {
        if (Config.vanishBeforeAccept) {
            if (!getDataStore().getAccepted().contains(player.getUniqueId().toString())) {
                Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {

                    public void run() {
                        player.offer(Keys.INVISIBLE, true);
                        player.offer(Keys.VANISH_IGNORES_COLLISION, true);
                        player.offer(Keys.VANISH_PREVENTS_TARGETING, true);
                    }
                }).delay(1, TimeUnit.SECONDS).name("resist-tools-s-setPlayerInvisible").submit(this);
            }
        }

        if (Config.informOnLogin) {
            if (getDataStore().getAccepted().contains(player.getUniqueId().toString())) {
                return;
            }
            Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
                public void run() {
                    sendMessage(player, Config.chatPrefix + Config.informMsg);
                }
            }).delay(10, TimeUnit.SECONDS).name("resist-tools-s-sendInformOnLogin").submit(this);
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public Optional<User> getUser(UUID uuid) {
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        return userStorage.get().get(uuid);
    }

    public void sendMessage(CommandSource sender, String message) {
        sender.sendMessage(fromLegacy(message));
    }

    public Text fromLegacy(String legacy) {
        return TextSerializers.FORMATTING_CODE.deserializeUnchecked(legacy);
    }

}
