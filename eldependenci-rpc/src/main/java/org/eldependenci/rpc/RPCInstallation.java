package org.eldependenci.rpc;

import org.eldependenci.rpc.protocol.RPCRequester;
import org.eldependenci.rpc.protocol.RPCServiceable;

public interface RPCInstallation {

    void retrofits(Class<?>... services);

    void remotes(Class<?>... services);

    void serves(Class<?>... serves);

    void registerProtocol(String protocolName, Class<? extends RPCServiceable> serviceable, Class<? extends RPCRequester> requester);


}
