package com.github.desperateyuri.server;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Command {
    public int show(Server server, Connection connection) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, IOException, InvalidKeyException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\\show-> This group contains:\n");
        for(Connection c: server.getConnections()){
            stringBuilder.append(String.format("%s:%s\n", c.getID(), c.getName()));
        }
        connection.write(stringBuilder.toString());
        return commandState.commandSuccess;
    }
    public int quit(Server server, Connection connection) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, IOException, InvalidKeyException {
        connection.write("\n\\quit-> You've already quited this chatroom, see you next time.\n");
        connection.getSocket().close();
        server.removeConnection(connection);
        return commandState.commandSuccess;
    }
    public int block(Server server, Connection connection, String msg){
        String id = msg.substring(7);
        System.out.println(id);
        if(id.matches("\\d{5,12}")){  // by now, the length of QQid is below 12.
            return connection.addToBlockList(id);
        }
        return commandState.commandFailed;
    }
    public int unblock(Server server, Connection connection, String msg){
        String id = msg.substring(9);
        System.out.println(id);
        if(id.matches("\\d{5,12}")){  // by now, the length of QQid is below 12.
            return connection.removeFromBlockList(id);
        }
        return commandState.commandFailed;
    }
}
