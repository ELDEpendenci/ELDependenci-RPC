package org.eldependenci.rpc.demo;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface DemoRemoteService {

    String sayHelloTo(String name);

    ItemStack createItem(Material material, int amount, String displayName);

    ItemStack getItem();

    Location getLocation();

    Location plusLocation(Location location, double x, double y, double z);

    String response(String text, int seconds);

    String responseWithPromise(String text, int seconds);

    List<String> testGeneric(List<Integer> list);

    DemoService.Book getBookFromAuthor(DemoService.Author author);

    void testVoidMethod(String say);
}
