package tutorial.showcase;

import com.google.inject.AbstractModule;

/**
 * 這個是 guice module, 可到 guice wiki 查看
 */
public final class MyExampleModule extends AbstractModule {

    private final MyExampleInstallationImpl installation;

    public MyExampleModule(MyExampleInstallationImpl installation) {
        this.installation = installation;
    }

    @Override
    protected void configure() {
        // 把已被註冊的安裝器注入以在其他實例獲取
        bind(MyExampleInstallationImpl.class).toInstance(installation);
    }
}
