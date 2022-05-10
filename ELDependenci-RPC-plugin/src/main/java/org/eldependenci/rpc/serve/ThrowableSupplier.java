package org.eldependenci.rpc.serve;

@FunctionalInterface
public interface ThrowableSupplier<T> {

    T get() throws Exception;

}
