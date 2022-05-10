package org.eldependenci.rpc;

import com.ericlam.mc.eld.registrations.CommandRegistry;
import com.ericlam.mc.eld.registrations.ComponentsRegistry;
import com.ericlam.mc.eld.registrations.ListenerRegistry;
import org.eldependenci.rpc.command.ELDRPCCommand;
import org.eldependenci.rpc.command.RPCTestCommand;

public class RPCRegistry implements ComponentsRegistry {

    @Override
    public void registerCommand(CommandRegistry commandRegistry) {
        commandRegistry.command(ELDRPCCommand.class, cc -> {
            cc.command(RPCTestCommand.class);
        });
    }

    @Override
    public void registerListeners(ListenerRegistry listenerRegistry) {

    }
}
