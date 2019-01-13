package net.resist.spongepress.commands;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import net.resist.spongepress.Config;
import net.resist.spongepress.Main;
import java.util.Optional;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
public class setpassCMD implements CommandExecutor{
    private final Main plugin;
    public setpassCMD(Main instance){
        plugin=instance;
    }
    @Override
    public CommandResult execute(CommandSource src,CommandContext args) throws CommandException{
        Player player=(Player)src;
        String playerName=player.getName();
        // We already know they logged in with a Good MOJANG token, so we will just edit this bit out.
		//if (!plugin.getDataStore().getLoggedIn().contains(playerName)) {
            //plugin.sendMessage(src,Config.chatPrefix+Config.mustLoginMsg);
        //} else {        
            try{
                Optional<String> firstPass=args.<String>getOne("password");
				Optional<String> secondPass=args.<String>getOne("passwordAgain");
                String password=firstPass.get();
                String passwordAgain=secondPass.get();
                String wordpressID=plugin.getDataStore().getWordpressID(playerName);
                if(password.matches(passwordAgain)){
                    OkHttpClient client=new OkHttpClient();
                    MediaType mediaType=MediaType.parse(
                        "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
                    RequestBody body=RequestBody.create(mediaType,
                        "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"password\"\r\n\r\n"+passwordAgain+"\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--");
                    Request request=new Request.Builder().url(Config.wordpressURL+"/wp-json/wp/v2/users/"+wordpressID).post(body)
                        .addHeader("content-type","multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                        .addHeader("Authorization","Bearer "+plugin.wordpressToken).addHeader("cache-control","no-cache")
                        .build();
                    Response response=client.newCall(request).execute();
					plugin.sendMessage(src,Config.chatPrefix+Config.setpassSuccess);
                }else{
                    plugin.sendMessage(src,Config.chatPrefix+Config.passwordNoMatch);
                }
            }catch(Exception e){
                plugin.sendMessage(src,Config.chatPrefix+"Set Pass Error: "+e);
            }
        //}
        return CommandResult.success();
    }
}