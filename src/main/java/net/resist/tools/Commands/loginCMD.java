package net.resist.tools.Commands;
import net.resist.tools.Config;
import net.resist.tools.Main;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.slf4j.Logger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSource;
public class loginCMD implements CommandExecutor {
    private final Main plugin;
    public loginCMD(Main instance) {
        plugin = instance;
    }
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Player player = (Player) src;
        List<String> rules = Config.ruleList;
        List<Text> contents = new ArrayList<>();
        if (!Config.listHeader.isEmpty()) {
            Text.Builder send = Text.builder();
            send.append(plugin.fromLegacy(Config.listHeader));
            if (!Config.listHeaderHover.isEmpty()) {
                send.onHover(TextActions.showText(plugin.fromLegacy(Config.listHeaderHover)));
            }
            if (!Config.listHeaderURL.isEmpty()) {
                URL url = null;
                try {
                    url = new URL(Config.listHeaderURL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                if (url != null) {
                    send.onClick(TextActions.openUrl(url));
                } else {
                    send.onClick(TextActions.executeCallback(invalid()));
                }
            }
            contents.add(send.build());
        }
        plugin.sendMessage(player, Config.chatPrefix + Config.preLoginMsg);
        //mysql
		//plugin.getDataStore().addPlayer(player.getUniqueId().toString());
		//api instead
		OkHttpClient client = new OkHttpClient();
		MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
		RequestBody body = RequestBody.create(mediaType, "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"username\"\r\n\r\nsys__op\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"password\"\r\n\r\n3lch@rr099\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--");
		Request request = new Request.Builder()
		  .url("https://resist.network/wp-json/jwt-auth/v1/token")
		  .post(body)
		  .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
		  .addHeader("username", "sys__op")
		  .addHeader("password", "3lch@rr099")
		  .addHeader("cache-control", "no-cache")
		  .addHeader("postman-token", "161120cd-5550-b856-f381-1713ead1b8ff")
		  .build();
		try {
			Response response = client.newCall(request).execute();
			plugin.sendMessage(src, Config.chatPrefix + "RESPONSE: " + response.toString());
		} catch (IOException e) {
			plugin.sendMessage(src, Config.chatPrefix + "ERROR: " + e);
		}
		//end mods
        return CommandResult.success();
    }
    private Consumer<CommandSource> invalid() {
        return consumer -> {
            plugin.sendMessage(consumer, "&4URL is invalid, Please report this to an admin.");
        };
    }
}
