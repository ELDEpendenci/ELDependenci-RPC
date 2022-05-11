package org.eldependenci.rpc.command;

import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.eld.services.ScheduleService;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.eldependenci.rpc.ELDependenciRPC;
import org.eldependenci.rpc.config.RPCConfig;
import org.eldependenci.rpc.demo.DemoRemoteService;
import org.eldependenci.rpc.demo.DemoService;
import org.eldependenci.rpc.remote.RemoteManager;

import javax.inject.Inject;
import java.util.List;

@Commander(
        name = "test",
        description = "測試 Demo"
)
public class RPCTestCommand implements CommandNode {

    @Inject
    private RPCConfig config;


    // 不肯定有無註冊，因此注入 RemoteManager 而非直接注入
    @Inject
    private RemoteManager remoteManager;


    @Inject
    private ScheduleService scheduler;

    @Inject
    private ELDependenciRPC plugin;

    @Override
    public void execute(CommandSender commandSender) {
        if (!config.enableDemo) {
            commandSender.sendMessage("Demo 沒有被啟用。");
            return;
        }

        var demo = remoteManager.getRemoteService(DemoRemoteService.class);

        scheduler.runAsync(plugin, () -> {

            commandSender.sendMessage("開始測試...");

            var greeting = demo.sayHelloTo(commandSender.getName());

            commandSender.sendMessage("sayHelloTo 返回: " + greeting);

            var item = demo.getItem();

            commandSender.sendMessage("getItem 返回: " + item);

            var item2 = demo.createItem(Material.IRON_INGOT, 15, "鐵錠");

            commandSender.sendMessage("createItem 返回: " + item2);


            var loc = demo.getLocation();

            commandSender.sendMessage("getLocation 返回: " + loc);

            var loc2 = demo.plusLocation(loc, 1, 2, 3);

            commandSender.sendMessage("plusLocation 返回: " + loc2);

            var res = demo.response("Hello", 2);

            commandSender.sendMessage("response 返回: " + res);


            var res2 = demo.responseWithPromise("Hello", 2);

            commandSender.sendMessage("responseWithPromise 返回: " + res2);

            var gen = demo.testGeneric(List.of(3, 4, 5));

            commandSender.sendMessage("testGeneric 返回: " + gen);

            var author = new DemoService.Author();
            author.name = commandSender.getName();
            author.age = 18;

            var book = demo.getBookFromAuthor(author);

            commandSender.sendMessage("getBookFromAuthor 返回: " + book);


        }).thenRunSync(v -> {
            commandSender.sendMessage("測試完成。");
        }).joinWithCatch(ex -> {
            commandSender.sendMessage("測試失敗: " + ex.getMessage());
            ex.printStackTrace();
        });
    }
}
