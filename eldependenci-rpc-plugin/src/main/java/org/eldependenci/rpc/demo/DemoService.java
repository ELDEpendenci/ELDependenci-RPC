package org.eldependenci.rpc.demo;

import com.ericlam.mc.eld.services.ItemStackService;
import com.ericlam.mc.eld.services.ScheduleService;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.eldependenci.rpc.ELDependenciRPC;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class DemoService {

    @Inject
    private ItemStackService itemStackService;

    @Inject
    private ELDependenciRPC rpc;

    @Inject
    private ScheduleService scheduleService;


    public String sayHelloTo(String name) {
        return String.format("hello, %s!", name);
    }


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

    public String response(String text, int seconds) throws Exception {
        Thread.sleep(seconds * 1000L);
        return text;
    }

    public ScheduleService.BukkitPromise<String> responseWithPromise(String text, int seconds) {
        return scheduleService.callAsync(rpc, () -> {
            Thread.sleep(seconds * 1000L);
            return text;
        });
    }


    public List<String> testGeneric(List<Integer> list) {
        return list.stream().map(i -> String.valueOf(i * 2)).collect(Collectors.toList());
    }


    public Book getBookFromAuthor(Author author) {
        var b = new Book();
        b.name = String.format("%s's book", author.name);
        b.pages = author.age * 2;
        return b;
    }

    public void testVoidMethod(String say){
        LoggerFactory.getLogger(DemoService.class).info(say);
    }


    public static class Author {
        public String name;
        public int age;
    }

    public static class Book {
        public String name;
        public int pages;


        @Override
        public String toString() {
            return "Book{" +
                    "name='" + name + '\'' +
                    ", pages=" + pages +
                    '}';
        }
    }
}
