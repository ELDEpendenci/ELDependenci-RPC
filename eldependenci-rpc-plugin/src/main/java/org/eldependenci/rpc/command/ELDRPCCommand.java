package org.eldependenci.rpc.command;

import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.common.CommonCommandNode;

@Commander(
        name = "rpc",
        description = "eldependenci rpc 指令"
)
public abstract class ELDRPCCommand<T> implements CommonCommandNode<T> {
    @Override
    public void execute(T t) {
    }
}
