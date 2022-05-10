package org.eldependenci.rpc.demo.remote;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.eldependenci.rpc.remote.RPCClient;
import org.eldependenci.rpc.retrofit.BaseURL;

import java.util.List;

@RPCClient(host = "localhost:8888")
public interface DemoService {

    String sayHelloTo(String name);

    ItemStack createItem(Material material, int amount, String displayName);

    ItemStack getItem();

    Location getLocation();

    Location plusLocation(Location location, double x, double y, double z);

    String response(String text, int seconds);

    String responseWithPromise(String text, int seconds);

    List<String> testGeneric(List<Integer> list);

    org.eldependenci.rpc.demo.serve.DemoService.Book getBookFromAuthor(org.eldependenci.rpc.demo.serve.DemoService.Author author);
}
