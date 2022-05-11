package org.eldependenci.rpc.command;

import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.components.CommandNode;
import org.bukkit.command.CommandSender;

@Commander(
        name = "rpc",
        description = "eldependenci rpc 指令"
)
public class ELDRPCCommand implements CommandNode {
    @Override
    public void execute(CommandSender commandSender) {

    }
}
