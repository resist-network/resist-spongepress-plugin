package net.resist.tools;


import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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

    public static String chatPrefix = "&8[&4&lResist&8&l.&4&lNetwork&r&8]&r ";
    public static String rulesAlias;

    //rules
    public static List<String> ruleList = Lists.newArrayList("Be respectful.", "Be ethical.", "Use common sense.");
    public static boolean informOnLogin = true;
    public static String rulesTitle = "Rules";
    public static List<String> playerCommands;
    public static List<String> consoleCommands;
    public static String listHeader;
    public static String listHeaderURL;
    public static String listHeaderHover;

    //messages
    public static String preLoginMsg = "&cPlease wait while we verify your login...";
    public static String mustLoginMsg = "&cYou must login before doing things!";

    //restrictions
    public static boolean blockBuildBeforeAccept = false;
    public static boolean blockMovementBeforeAccept = false;
    public static boolean blockCommandsBeforeAccept = false;
    public static boolean vanishBeforeAccept = false;


    //database
    public static String mysqlHost;
    public static int mysqlPort;
    public static String mysqlDatabase;
    public static String mysqlUser;
    public static String mysqlPass;
    public static String mysqlPrefix;
    public static String server;
	
	
	//api endpoints
	public static String APIendpoint;
	public static String wordpressURL;

    public void configCheck() throws IOException, ObjectMappingException {
        if (!plugin.defaultConfFile.exists()) {
            plugin.defaultConfFile.createNewFile();
        }

        //messages
		preLoginMsg = check(config.getNode("messages", "preLogin"), preLoginMsg).getString();
		mustLoginMsg = check(config.getNode("messages", "mustLogin"), mustLoginMsg).getString();
        chatPrefix = check(config.getNode("messages", "prefix"), chatPrefix, "The prefix of messages sent in chat").getString();

        //restrictions
        blockBuildBeforeAccept = check(config.getNode("restrictions", "blockBuildBeforeAccept"), blockBuildBeforeAccept, "Should players be blocked from placing and breaking blocks before reading the rules?").getBoolean();
        blockMovementBeforeAccept = check(config.getNode("restrictions", "blockMovementBeforeAccept"), blockMovementBeforeAccept, "Should players be blocked from moving before reading the rules?").getBoolean();
        blockCommandsBeforeAccept = check(config.getNode("restrictions", "blockCommandsBeforeAccept"), blockCommandsBeforeAccept, "Should players be blocked from sending commands before reading the rules?").getBoolean();
        vanishBeforeAccept = check(config.getNode("restrictions", "vanishBeforeAccept"), vanishBeforeAccept, "Should players be invisible to all players before reading the rules?").getBoolean();

        //rules
        listHeader = check(config.getNode("rules", "header", "message"), "", "This text is displayed above the rules in /rules").getString();
        listHeaderURL = check(config.getNode("rules", "header", "url"), "", "When players click the text set in message, they will be prompted to this URL (Must have http:// or https:// at the beginning)").getString();
        listHeaderHover = check(config.getNode("rules", "header", "hover"), "", "This message will be displayed when the player hovers over the header message.").getString();

        //server
        server = check(config.getNode("server"), "Global", "Name of the server. Used for indavidual server identification. If a different name is set, It will check if the player has accepted the rules for that specific server instead of globally.  Default: \"Global\"").getString();

        //database
        mysqlHost = check(config.getNode("storage", "mysql", "host"), "localhost", "Host of the MySQL Server").getString();
        mysqlPort = check(config.getNode("storage", "mysql", "port"), "3306", "Port of the MySQL server. Default: 3306").getInt();
        mysqlDatabase = check(config.getNode("storage", "mysql", "database"), "resist", "The database to store in").getString();
        mysqlUser = check(config.getNode("storage", "mysql", "user"), "root", "The user for the database").getString();
        mysqlPass = check(config.getNode("storage", "mysql", "password"), "pass", "Password for that user").getString();
        mysqlPrefix = check(config.getNode("storage", "mysql", "table-prefix"), "resist_", "Prefix for the plugin tables").getString();

		//api endpoints
		APIendpoint = check(config.getNode("api", "endpoints", "type"), "wordpress", "Wordpress, NodeBB, XenForo, PHPBB (Currently only Wordpress)").getString();
		wordpressURL = check(config.getNode("api", "endpoints", "wordpress", "url"), "https://resist.network", "The Wordpress Site URL (Wordpress Dashboard >> Settings >> General >> Site URL)").getString();

        if (config.getNode("rules", "list").hasListChildren()) {
            ruleList = Lists.newArrayList(config.getNode("rules", "list").getList(TypeToken.of(String.class)));
        } else {
            ruleList = config.getNode("rules", "list").setValue(ruleList).setComment("Commands to be run after the player accepts the rules, These commands are sent by the console ({player} will be replaced by the player's name)").getList(TypeToken.of(String.class));
        }


        informOnLogin = check(config.getNode("rules", "informOnLogin"), informOnLogin, "Do you want the player to be sent the above 'Inform' message after logging in?").getBoolean();
        rulesTitle = check(config.getNode("rules", "title"), rulesTitle, "The tile for the /rules").getString();
        if (config.getNode("rules", "onAccept", "playerCommands").hasListChildren()) {
            playerCommands = check(config.getNode("rules", "onAccept", "playerCommands"), Collections.emptyList(), "Commands to be run after the player accepts the rules, These commands are sent by the player").getList(TypeToken.of(String.class));
        } else {
            playerCommands = config.getNode("rules", "onAccept", "playerCommands").setValue(Collections.emptyList()).setComment("Commands to be run after the player accepts the rules, These commands are sent by the player").getList(TypeToken.of(String.class));
        }
        if (config.getNode("rules", "onAccept", "consoleCommands").hasListChildren()) {
            consoleCommands = check(config.getNode("rules", "onAccept", "consoleCommands"), Collections.emptyList(), "Commands to be run after the player accepts the rules, These commands are sent by the console ({player} will be replaced by the player's name)").getList(TypeToken.of(String.class));
        } else {
            consoleCommands = config.getNode("rules", "onAccept", "consoleCommands").setValue(Collections.emptyList()).setComment("Commands to be run after the player accepts the rules, These commands are sent by the console ({player} will be replaced by the player's name)").getList(TypeToken.of(String.class));
        }
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
