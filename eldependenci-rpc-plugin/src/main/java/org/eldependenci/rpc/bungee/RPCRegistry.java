package org.eldependenci.rpc.bungee;

import com.ericlam.mc.eld.BungeeRegistry;
import com.ericlam.mc.eld.components.BungeeCommand;
import com.ericlam.mc.eld.registration.CommandRegistry;
import com.ericlam.mc.eld.registration.ListenerRegistry;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Listener;
import org.eldependenci.rpc.bungee.demo.DemoRemoteService;
import org.eldependenci.rpc.bungee.demo.DemoService;
import org.eldependenci.rpc.command.ELDRPCCommand;
import org.eldependenci.rpc.command.RPCTestCommand;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RPCRegistry implements BungeeRegistry {

    @Override
    public void registerCommand(CommandRegistry<BungeeCommand> commandRegistry) {
        commandRegistry.command(ELDRPCCommandBungee.class, c -> {
            c.command(RPCTestCommandBungee.class);
        });
    }

    @Override
    public void registerListeners(ListenerRegistry<Listener> listenerRegistry) {

    }

    public static class ELDRPCCommandBungee extends ELDRPCCommand<CommandSender> implements BungeeCommand {
    }


    public static class RPCTestCommandBungee extends RPCTestCommand<CommandSender> implements BungeeCommand {

        @Override
        public void execute(CommandSender sender) {
            if (!config.enableDemo) {
                sender.sendMessage( "Demo 沒有被啟用。");
                return;
            }

            var demo = remoteManager.getRemoteService(DemoRemoteService.class);

            CompletableFuture.runAsync(() -> {

                sender.sendMessage( TextComponent.fromLegacyText("開始測試..."));

                var greeting = demo.sayHelloTo(sender.getName());

                sender.sendMessage( TextComponent.fromLegacyText("sayHelloTo 返回: " + greeting));

                var res = demo.response("Hello", 2);

                sender.sendMessage( TextComponent.fromLegacyText("response 返回: " + res));

                var gen = demo.testGeneric(List.of(3, 4, 5));

                sender.sendMessage( TextComponent.fromLegacyText("testGeneric 返回: " + gen));

                var author = new DemoService.Author();
                author.name = sender.getName();
                author.age = 18;

                var book = demo.getBookFromAuthor(author);

                sender.sendMessage( TextComponent.fromLegacyText("getBookFromAuthor 返回: " + book));

                demo.testVoidMethod("this is a void method test");

                sender.sendMessage( TextComponent.fromLegacyText("testVoidMethod 執行完成"));


            }).whenComplete((v, ex) -> {
                if (ex != null) {
                    sender.sendMessage( TextComponent.fromLegacyText("測試失敗: " + ex.getMessage()));
                    ex.printStackTrace();
                } else {
                    sender.sendMessage( TextComponent.fromLegacyText("測試完成。"));
                }
            });
        }
    }
}
