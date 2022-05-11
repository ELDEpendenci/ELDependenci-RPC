package org.eldependenci.rpc.config;

import com.ericlam.mc.eld.annotations.Resource;
import com.ericlam.mc.eld.components.Configuration;
import org.eldependenci.rpc.context.RPCInfo;

import java.util.List;
import java.util.Map;

@Resource(locate = "remotes.yml")
public class RPCRemoteConfig extends Configuration {

    public List<RPCInfo> remotes;

}
