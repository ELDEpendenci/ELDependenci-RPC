package org.eldependenci.rpc;

import com.ericlam.mc.eld.annotations.Resource;
import com.ericlam.mc.eld.components.Configuration;

@Resource(locate = "config.yml")
public class RPCConfig extends Configuration {


    public int servePort = 8888;
    public long asyncTimeout = 60000L;
    public boolean enabled = true;

    public boolean enableDemo = false;

}
