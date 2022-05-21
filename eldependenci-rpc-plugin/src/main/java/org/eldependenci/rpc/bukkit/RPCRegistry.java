package org.eldependenci.rpc.bukkit;

import com.ericlam.mc.eld.bukkit.CommandNode;
import com.ericlam.mc.eld.bukkit.ComponentsRegistry;
import com.ericlam.mc.eld.registration.CommandRegistry;
import com.ericlam.mc.eld.registration.ListenerRegistry;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.eldependenci.rpc.bukkit.demo.BukkitDemoRemoteService;
import org.eldependenci.rpc.bungee.demo.DemoService;
import org.eldependenci.rpc.command.ELDRPCCommand;
import org.eldependenci.rpc.command.RPCTestCommand;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RPCRegistry implements ComponentsRegistry {


    @Override
    public void registerCommand(CommandRegistry<CommandNode> commandRegistry) {
        commandRegistry.command(ELDPRCCommandBukkit.class, cc -> {
            cc.command(RPCTestCommandBukkit.class);
        });
    }

    @Override
    public void registerListeners(ListenerRegistry<Listener> listenerRegistry) {

    }


    public static class ELDPRCCommandBukkit extends ELDRPCCommand<CommandSender> implements CommandNode {
    }

    public static class RPCTestCommandBukkit extends RPCTestCommand<CommandSender> implements CommandNode {


        @Override
        public void execute(CommandSender sender) {
            if (!config.enableDemo) {
                sender.sendMessage("Demo 沒有被啟用。");
                return;
            }

            var demo = remoteManager.getRemoteService(BukkitDemoRemoteService.class);

            CompletableFuture.runAsync(() -> {

                sender.sendMessage("開始測試...");

                var greeting = demo.sayHelloTo(sender.getName());

                sender.sendMessage("sayHelloTo 返回: " + greeting);

                var item = demo.getItem();

                sender.sendMessage("getItem 返回: " + item);

                var item2 = demo.createItem(Material.IRON_INGOT, 15, "鐵錠");

                sender.sendMessage("createItem 返回: " + item2);


                var loc = demo.getLocation();

                sender.sendMessage("getLocation 返回: " + loc);

                var loc2 = demo.plusLocation(loc, 1, 2, 3);

                sender.sendMessage("plusLocation 返回: " + loc2);

                var res = demo.response("Hello", 2);

                sender.sendMessage("response 返回: " + res);

                var gen = demo.testGeneric(List.of(3, 4, 5));

                sender.sendMessage("testGeneric 返回: " + gen);

                var author = new DemoService.Author();
                author.name = sender.getName();
                author.age = 18;

                var book = demo.getBookFromAuthor(author);

                sender.sendMessage("getBookFromAuthor 返回: " + book);

                demo.testVoidMethod("this is a void method test");

                sender.sendMessage("testVoidMethod 執行完成");


            }).whenComplete((v, ex) -> {
                if (ex != null) {
                    sender.sendMessage("測試失敗: " + ex.getMessage());
                    ex.printStackTrace();
                } else {
                    sender.sendMessage("測試完成。");
                }
            });
        }
    }
}
