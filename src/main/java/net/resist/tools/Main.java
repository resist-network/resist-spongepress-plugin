package net.resist.tools;
import com.google.inject.Inject;
import net.resist.tools.Commands.loginCMD;
import net.resist.tools.Commands.logoutCMD;
import net.resist.tools.Commands.setpassCMD;
import net.resist.tools.Database.DataStoreManager;
import net.resist.tools.Database.IDataStore;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
@Plugin(id = "wordpress-plugin", name = "Wordpress Plugin", version = "1.0.1", description = "Resist.Network Wordpress Plugin")
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
    public String wordpressToken;
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
            getDataStore().clearList();
        } else {
            getLogger().error("Unable to load a database please check your Console/Config!");
        }
    }
    @Listener
    public void onServerStart(GameStartedServerEvent event) throws IOException {
		//Logs in with a Wordpress Admin User Specified in Config, and Retrieves the Token for Operations on the User API
		try {
			MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
			OkHttpClient client = new OkHttpClient();		
			RequestBody bodyGetToken = RequestBody.create(mediaType, "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"username\"\r\n\r\n"+Config.wordpressAdminUser+"\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"password\"\r\n\r\n"+Config.wordpressAdminPass+"\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--");
			Request requestGetToken = new Request.Builder()
				.url(Config.wordpressURL+"/wp-json/jwt-auth/v1/token")
				.post(bodyGetToken)
				.addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
				.addHeader("cache-control", "no-cache")
				.build();
				Response responseGetToken = client.newCall(requestGetToken).execute();
				String tokenString = responseGetToken.body().string();
				List<String> list = new ArrayList<String>(Arrays.asList(tokenString.split("\":\"")));
				wordpressToken = list.get(1).replace("\",\"user_email","");
				//For later use in another function if we need to iterate over a stupid JSON object in Java again. Ew.
				//for(int i=0;i<list.size();i++) {
				logger.info("Wordpress Token: "+wordpressToken);
				logger.info("Wordpress token was created successfully!");
				//}
				//This could add something to a new table, but for now we just will check Wordpress tables.
				//plugin.getDataStore().addPlayer(player.getUniqueId().toString());
		} catch (Exception e) {
			getLogger().error("Wordpress Token Error: "+e);
		}    	
        logger.info("Plugin Loaded Successfully!");
    }
    @Listener
    public void onPluginReload(GameReloadEvent event) throws IOException, ObjectMappingException {
        this.config = new Config(this);
        loadDataStore();
    }
    private void loadCommands() {
        // /login
        CommandSpec login = CommandSpec.builder()
                .description(Text.of("Login to the Server."))
                .arguments(
                		GenericArguments.string(Text.of("password")))
                .executor(new loginCMD(this))
                .build();
        cmdManager.register(this, login, "login");
        // /logout
        CommandSpec logout = CommandSpec.builder()
                .description(Text.of("Logout of the Server."))
                .arguments()
                .executor(new logoutCMD(this))
                .build();
        cmdManager.register(this, logout, "logout");        
        // /setpass
        CommandSpec setpass = CommandSpec.builder()
                .description(Text.of("Login to the Server."))
                .arguments(
                		GenericArguments.string(Text.of("password")),
                		GenericArguments.string(Text.of("passwordAgain")))
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
    public IDataStore getDataStore() {
        return dataStoreManager.getDataStore();
    }
    @Listener
    public void onPlayerLogin(ClientConnectionEvent.Join event, @Root Player player) {
    	String playerName = player.getName();
        if (!getDataStore().getAccepted().contains(player.getName().toString())) {
        	getLogger().info("Player "+playerName+" is a new player to the server!");
        	sendMessage(player, Config.chatPrefix + "Welcome to the server &l&e"+playerName+"&r!");
        try {
            Random rnd = new Random();
            int number = rnd.nextInt(999999);        	
        	String newPass = String.format("%06d", number);
        	OkHttpClient client = new OkHttpClient();
        	MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
        	RequestBody body = RequestBody.create(mediaType, "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"username\"\r\n\r\n"+playerName+"\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"email\"\r\n\r\n"+playerName+"@"+Config.wordpressNewUserEmailDomain+"\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"password\"\r\n\r\n"+newPass+"\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--");
        	Request request = new Request.Builder()
				.url("https://resist.network/wp-json/wp/v2/users")
				.post(body)
				.addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
				.addHeader("authorization", "Bearer "+wordpressToken)
				.addHeader("cache-control", "no-cache")
				.build();
        	@SuppressWarnings("unused")
			Response response = client.newCall(request).execute();
        	sendMessage(player, Config.chatPrefix + "Your new password is: &l&b"+newPass+"&r");
        	sendMessage(player, Config.chatPrefix + "Use &l&b/setpass&r to change!");
        	getDataStore().addPlayer(player.getName().toString());
			} catch (Exception e) {
				getLogger().error("Create User Error: "+e);
			}          	
        } else {
        	//need config messages here maybe, but also need placeholders and just... ew. doing all the messages that dont require a variable placeholder
        	sendMessage(player, Config.chatPrefix + "Welcome back &l&e"+playerName+"&r!");
        	sendMessage(player, Config.chatPrefix + "Use &l&e/login&r before continuing!");
        }
    }
    @Listener
    public void onPlayerDisconnect(ClientConnectionEvent.Disconnect event, @Root Player player) {
    	String playerName = player.getName();
    	getDataStore().removePlayer(playerName);
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