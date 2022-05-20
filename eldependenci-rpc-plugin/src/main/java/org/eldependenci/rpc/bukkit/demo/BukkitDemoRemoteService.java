package org.eldependenci.rpc.bukkit.demo;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.eldependenci.rpc.bungee.demo.DemoRemoteService;

public interface BukkitDemoRemoteService extends DemoRemoteService {

    ItemStack createItem(Material material, int amount, String displayName);

    ItemStack getItem();

    Location getLocation();

    Location plusLocation(Location location, double x, double y, double z);


}
