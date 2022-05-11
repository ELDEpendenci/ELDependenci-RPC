package org.eldependenci.rpc.protocol;

public interface RPCServiceable {

    void StartService(ServiceHandler handler);

    void StopService();

}
