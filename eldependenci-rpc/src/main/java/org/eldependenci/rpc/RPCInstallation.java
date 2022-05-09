package org.eldependenci.rpc;

public interface RPCInstallation {

    void retrofits(Class<?>... services);

    void remotes(Class<?>... services);

    void serves(Class<?>... serves);

}
