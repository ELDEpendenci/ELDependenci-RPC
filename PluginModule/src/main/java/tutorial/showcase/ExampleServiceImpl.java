package tutorial.showcase;

import org.bukkit.Bukkit;

import javax.inject.Inject;

public class ExampleServiceImpl implements ExampleService {

    // 獲取安裝了的實例
    @Inject
    private MyExampleInstallationImpl installation;

    @Override
    public void doSomethingCool() {
        Bukkit.getLogger().info("Hello World!!");
        Bukkit.getLogger().info("Registered: ");
        installation.getStringMap().forEach((k, v) -> {
            Bukkit.getLogger().info(k+": "+v);
        });
    }
}
