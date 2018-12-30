package net.resist.wordpress.commands;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import net.resist.wordpress.sponge.Config;
import net.resist.wordpress.sponge.Main;
import java.util.Optional;
import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class loginCMD implements CommandExecutor{
    private final Main plugin;
    public loginCMD(Main instance){
        plugin=instance;
    }
    @Override
    public CommandResult execute(CommandSource src,CommandContext args) throws CommandException{
        Player player=(Player)src;
        String playerName=player.getName();
        try{
            plugin.sendMessage(player,Config.chatPrefix+Config.preLoginMsg);
            Optional<String> firstPass=args.<String>getOne("password");
            String password=firstPass.get();
            if(password!=null&&!password.isEmpty()){
                OkHttpClient client=new OkHttpClient();
                MediaType mediaType=MediaType.parse(
                    "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
                RequestBody body=RequestBody.create(mediaType,
                    "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"username\"\r\n\r\n"
                        +playerName
                        +"\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"password\"\r\n\r\n"
                        +password+"\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--");
                Request request=new Request.Builder().url("https://resist.network/wp-json/jwt-auth/v1/token").post(body)
                    .addHeader("content-type","multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                    .addHeader("cache-control","no-cache").build();
                Response response=client.newCall(request).execute();
                String responseString=response.body().string();
                if(responseString.contains(":403}")){
                    plugin.getDataStore().removePlayer(player.getName());
                    plugin.sendMessage(src,Config.chatPrefix+Config.incorrectPassword);
                }else if(responseString.contains(playerName)){
                    plugin.getDataStore().addPlayer(player.getName());
                    plugin.sendMessage(src,Config.chatPrefix+Config.loginSuccess);
                }
            }else{
                plugin.getDataStore().removePlayer(player.getName());
                plugin.sendMessage(src,Config.chatPrefix+Config.passwordNoMatch);
            }
        }catch(Exception e){
            plugin.getDataStore().removePlayer(player.getName());
            plugin.sendMessage(src,Config.chatPrefix+Config.miscLoginError);
        }
        return CommandResult.success();
    }
}