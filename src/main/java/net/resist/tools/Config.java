package net.resist.tools;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import java.io.IOException;
public class Config {
    private final Main plugin;
    public static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static CommentedConfigurationNode config;
    public Config(Main main) throws IOException, ObjectMappingException {
        plugin = main;
        loader = HoconConfigurationLoader.builder().setPath(plugin.defaultConf).build();
        config = loader.load();
        configCheck();
    }
    public static int mysqlPort;
    public static boolean blockBuildBeforeLogin = true;
    public static boolean blockMovementBeforeLogin = true;
    public static boolean blockCommandsBeforeLogin = false;    
    public static boolean blockDamageBeforeLogin = true;
    public static boolean vanishBeforeLogin = false;
    public static String chatPrefix = "&8[&4&lResist&8&l.&4&lNetwork&r&8]&r ";
    public static String preLoginMsg = "&ePlease wait, verifying your password...";
    public static String mustLoginMsg = "&cYou must login before doing things!";
    public static String passwordNoMatch = "&cPasswords did not match, try again!";
    public static String miscLoginError = "&cThere was an error, please contact staff!";
    public static String incorrectPassword = "&cWrong password!";
    public static String loginSuccess = "&aYou have successfully logged in!";
    public static String logoutSuccess = "&aYou have successfully logged out!";
    public static String mysqlHost;
    public static String mysqlDatabase;
    public static String mysqlUser;
    public static String mysqlPass;
    public static String mysqlPrefix;
    public static String server;
	public static String APIendpoint;
	public static String wordpressNewUserEmailDomain;
	public static String wordpressURL;
	public static String wordpressAdminUser;
	public static String wordpressAdminPass;	
    public void configCheck() throws IOException, ObjectMappingException {
        if (!plugin.defaultConfFile.exists()) {
            plugin.defaultConfFile.createNewFile();
        }
		preLoginMsg = check(config.getNode("messages", "preLogin"), preLoginMsg).getString();
		mustLoginMsg = check(config.getNode("messages", "mustLogin"), mustLoginMsg).getString();
		passwordNoMatch = check(config.getNode("messages", "passwordNoMatch"), passwordNoMatch).getString();		
		miscLoginError = check(config.getNode("messages", "miscLoginError"), miscLoginError).getString();		
		incorrectPassword = check(config.getNode("messages", "incorrectPassword"), incorrectPassword).getString();		
		loginSuccess = check(config.getNode("messages", "loginSuccess"), loginSuccess).getString();		
		logoutSuccess = check(config.getNode("messages", "logoutSuccess"), logoutSuccess).getString();		
		chatPrefix = check(config.getNode("messages", "prefix"), chatPrefix, "The prefix of messages sent in chat").getString();
        blockBuildBeforeLogin = check(config.getNode("restrictions", "blockBuildBeforeLogin"), blockBuildBeforeLogin, "Should players be blocked from placing and breaking blocks before login?").getBoolean();
        blockMovementBeforeLogin = check(config.getNode("restrictions", "blockMovementBeforeLogin"), blockMovementBeforeLogin, "Should players be blocked from moving before login?").getBoolean();
        blockCommandsBeforeLogin = check(config.getNode("restrictions", "blockCommandsBeforeLogin"), blockCommandsBeforeLogin, "Should players be blocked from sending commands before login?").getBoolean();
        blockDamageBeforeLogin = check(config.getNode("restrictions", "blockDamageBeforeLogin"), blockDamageBeforeLogin, "Should players be blocked from getting damage before login?").getBoolean();
        vanishBeforeLogin = check(config.getNode("restrictions", "vanishBeforeAccept"), vanishBeforeLogin, "Should players be invisible to all players before login?").getBoolean();
        server = check(config.getNode("server"), "Global", "Name of the server. Used for indavidual server identification. If a different name is set, It will check if the player has accepted the rules for that specific server instead of globally.  Default: \"Global\"").getString();
        mysqlHost = check(config.getNode("storage", "mysql", "host"), "localhost", "Host of the MySQL Server").getString();
        mysqlPort = check(config.getNode("storage", "mysql", "port"), "3306", "Port of the MySQL server. Default: 3306").getInt();
        mysqlDatabase = check(config.getNode("storage", "mysql", "database"), "resist", "The database to store in").getString();
        mysqlUser = check(config.getNode("storage", "mysql", "user"), "root", "The user for the database").getString();
        mysqlPass = check(config.getNode("storage", "mysql", "password"), "pass", "Password for that user").getString();
        mysqlPrefix = check(config.getNode("storage", "mysql", "table-prefix"), "resist_", "Prefix for the plugin tables").getString();
		APIendpoint = check(config.getNode("api", "endpoints", "type"), "wordpress", "Wordpress, NodeBB, XenForo, PHPBB (Currently only Wordpress)").getString();
		wordpressURL = check(config.getNode("api", "endpoints", "wordpress", "url"), "https://resist.network", "The Wordpress Site URL (Wordpress Dashboard >> Settings >> General >> Site URL)").getString();
		wordpressAdminUser = check(config.getNode("api", "endpoints", "wordpress", "adminUser"), "", "The Wordpress Site Admin Username (Important: Enclose in Quotes)").getString();
		wordpressAdminPass = check(config.getNode("api", "endpoints", "wordpress", "adminPass"), "", "The Wordpress Site Admin Password (Important: Enclose in Quotes)").getString();
		wordpressNewUserEmailDomain = check(config.getNode("api", "endpoints", "wordpress", "newUserEmailDomain"), "resist.network", "Used when auto-adding players to the Wordpress User Database (Email is Required)").getString();
		loader.save(config);
    }
    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue, String comment) {
        if (node.isVirtual()) {
            node.setValue(defaultValue).setComment(comment);
        }
        return node;
    }
    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue) {
        if (node.isVirtual()) {
            node.setValue(defaultValue);
        }
        return node;
    }
}
