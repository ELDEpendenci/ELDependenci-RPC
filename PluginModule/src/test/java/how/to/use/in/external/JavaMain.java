package how.to.use.in.external;

import com.ericlam.mc.eld.ELDBukkitPlugin;
import com.ericlam.mc.eld.ManagerProvider;
import com.ericlam.mc.eld.ServiceCollection;
import com.ericlam.mc.eld.annotations.ELDPlugin;
import tutorial.showcase.MyExampleInstallation;

/*
    範例插件 - 使用你的 API

    由於你的服務是以注入為方式，因此只能放能依賴注入的位置
    eg: LifeCycle.class

    需要先掛接 api 再進行注入。
 */
@ELDPlugin(
        lifeCycle = LifeCycle.class,
        registry = Registry.class
)
public class JavaMain extends ELDBukkitPlugin {

    @Override
    protected void bindServices(ServiceCollection serviceCollection) {
        // 獲取 該擴充插件 的註冊器
        MyExampleInstallation installation = serviceCollection.getInstallation(MyExampleInstallation.class);
        installation.putSomeValue("hello", "world");
        installation.putSomeValue("good", "work");
    }

    @Override
    protected void manageProvider(ManagerProvider managerProvider) {
    }

}
