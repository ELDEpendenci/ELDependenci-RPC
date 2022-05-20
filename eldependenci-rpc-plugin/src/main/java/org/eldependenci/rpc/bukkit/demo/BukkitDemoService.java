package org.eldependenci.rpc.bukkit.demo;

import com.ericlam.mc.eld.services.ItemStackService;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.eldependenci.rpc.bungee.demo.DemoService;

public class BukkitDemoService extends DemoService {

    @Inject
    private ItemStackService itemStackService;


    public ItemStack createItem(Material material, int amount, String displayName) {
        return itemStackService.build(material).amount(amount).display(displayName).getItem();
    }


    public ItemStack getItem() {
        return itemStackService
                .build(Material.STONE_SWORD)
                .enchant(Enchantment.DAMAGE_ALL, 1)
                .getItem();
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld("world"), 1.0, 1.0, 1.0, 1.0f, 2.0f);
    }


    public Location plusLocation(Location location, double x, double y, double z) {
        location.add(x, y, z);
        return location;
    }


}
