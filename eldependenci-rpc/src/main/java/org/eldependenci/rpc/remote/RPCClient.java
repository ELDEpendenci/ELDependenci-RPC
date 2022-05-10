package org.eldependenci.rpc.remote;

import org.eldependenci.rpc.protocol.ProtocolType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RPCClient {

    String host();

    String protocol() default ProtocolType.HTTP;

    boolean useTLS() default false;

}
