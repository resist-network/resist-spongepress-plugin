package net.resist.wordpress.sponge.commands;
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
public class setpassCMD implements CommandExecutor{
    private final Main plugin;
    public setpassCMD(Main instance){
        plugin=instance;
    }
    @Override
    public CommandResult execute(CommandSource src,CommandContext args) throws CommandException{
        Player player=(Player)src;
        String playerName=player.getName();
        //plugin.sendMessage(player,Config.chatPrefix+Config.preLoginMsg);
        try{
            Optional<String> firstPass=args.<String>getOne("password");
            Optional<String> secondPass=args.<String>getOne("passwordAgain");
            String password=firstPass.get();
            String passwordAgain=secondPass.get();
            String wordpressID=plugin.getDataStore().getWordpressID(playerName);
            plugin.sendMessage(src,Config.chatPrefix+"Wordpress User ID: "+wordpressID);
            if(password.matches(passwordAgain)){
                // start new
                OkHttpClient client=new OkHttpClient();
                MediaType mediaType=MediaType.parse(
                    "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
                RequestBody body=RequestBody.create(mediaType,
                    "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"password\"\r\n\r\n"+passwordAgain+"\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--");
                Request request=new Request.Builder().url("https://resist.network/wp-json/wp/v2/users/"+wordpressID).post(body)
                    .addHeader("content-type","multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                    .addHeader("Authorization","Bearer "+plugin.wordpressToken).addHeader("cache-control","no-cache")
                    .build();
                Response response=client.newCall(request).execute();
                // end news
                plugin.sendMessage(src,Config.chatPrefix+"Set Pass Result: "+response.body().string());
                plugin.sendMessage(src,Config.chatPrefix+"Wordpress Token Used: "+plugin.wordpressToken);
            }else{
                plugin.sendMessage(src,Config.chatPrefix+Config.passwordNoMatch);
            }
        }catch(Exception e){
            plugin.sendMessage(src,Config.chatPrefix+"Set Pass Error: "+e);
        }
        return CommandResult.success();
    }
}