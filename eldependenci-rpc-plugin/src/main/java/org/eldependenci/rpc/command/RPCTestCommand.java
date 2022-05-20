package org.eldependenci.rpc.command;

import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.common.CommonCommandNode;
import org.eldependenci.rpc.config.RPCConfig;
import org.eldependenci.rpc.remote.RemoteManager;

import javax.inject.Inject;

@Commander(
        name = "test",
        description = "測試 Demo"
)
public abstract class RPCTestCommand<T> implements CommonCommandNode<T> {

    @Inject
    protected RPCConfig config;

    // 不肯定有無註冊，因此注入 RemoteManager 而非直接注入
    @Inject
    protected RemoteManager remoteManager;

}
