package com.github.desperateyuri.server;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

// This module is scalable
public class commandOrNot {

    public int check(Server server, Connection connection, String msg) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Command command = new Command();
        if(msg.equals("\\show")){
            return command.show(server, connection);
        }else if(msg.equals("\\quit")){
            return command.quit(server, connection);
        }else if(msg.startsWith("\\block ")){
            int result = command.block(server, connection, msg);
            if(result == commandState.commandFailed){
                connection.write("\n\\block-> Command failed.\n");
            }else if(result == commandState.commandSuccess){
                connection.write("\n\\block-> You've already blocked this guy.\n");
            }
            return result;
        }else if(msg.startsWith("\\unblock ")){
            int result = command.unblock(server, connection, msg);
            if(result == commandState.commandFailed){
                connection.write("\n\\block-> Command failed.\n");
            }else if(result == commandState.commandSuccess){
                connection.write("\n\\unblock-> You've already unblocked this guy.\n");
            }
            return result;
        }
        return commandState.commandNotfound;
    }
}
