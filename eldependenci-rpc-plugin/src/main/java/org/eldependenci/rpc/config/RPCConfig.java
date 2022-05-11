package org.eldependenci.rpc.config;

import com.ericlam.mc.eld.annotations.Resource;
import com.ericlam.mc.eld.components.Configuration;

import java.util.ArrayList;
import java.util.List;

@Resource(locate = "config.yml")
public class RPCConfig extends Configuration {
    public int servePort = 8888;
    public long asyncTimeout = 60000L;
    public boolean enabled = true;
    public boolean enableDemo = false;

    public List<String> enabledProtocols = new ArrayList<>();

    public String token = System.getenv("RPC_TOKEN");

}
