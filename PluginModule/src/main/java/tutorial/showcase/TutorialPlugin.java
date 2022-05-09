package tutorial.showcase;

import com.ericlam.mc.eld.AddonManager;
import com.ericlam.mc.eld.ELDBukkitAddon;
import com.ericlam.mc.eld.ManagerProvider;
import com.ericlam.mc.eld.ServiceCollection;
import com.ericlam.mc.eld.annotations.ELDPlugin;

@ELDPlugin(
        lifeCycle = TutorialLifeCycle.class,
        registry = TutorialRegistry.class
)
public class TutorialPlugin extends ELDBukkitAddon {

    @Override
    protected void bindServices(ServiceCollection serviceCollection) {
        serviceCollection.bindService(ExampleService.class, ExampleServiceImpl.class);
    }


    @Override
    protected void preAddonInstall(ManagerProvider managerProvider, AddonManager addonManager) {
        // 初始化安裝器
        MyExampleInstallationImpl installation = new MyExampleInstallationImpl();
        // 添加安裝器
        addonManager.customInstallation(MyExampleInstallation.class, installation);
        // 安裝 guice module
        addonManager.installModule(new MyExampleModule(installation));
    }
}
