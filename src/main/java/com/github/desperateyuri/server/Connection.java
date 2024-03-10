package com.github.desperateyuri.server;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

class Connection{
    // It is partly Thread Safety
    private final Socket s;
    private final String id;
    private final String name;
    private final byte[] sessionKey;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    private ArrayList<String> blockList = new ArrayList<>();
    public String getName(){ return name; }
    public String getID(){ return id; }
    public Socket getSocket(){ return s;}
    public boolean inBlockList(String id){
        return blockList.contains(id);
    }
    public int addToBlockList(String id){
        if(blockList.contains(id)){
            return commandState.commandFailed;
        }
        blockList.add(id);
        return commandState.commandSuccess;
    }
    public int removeFromBlockList(String id){
        if(blockList.contains(id)){
            blockList.remove(id);
            return commandState.commandSuccess;
        }
        return commandState.commandFailed;
    }
    public Connection(Socket s, String id, String name, byte[] sessionKey, BufferedReader reader, BufferedWriter writer){
        this.s = s;
        this.id = id;
        this.name = name;
        this.sessionKey = sessionKey;
        this.reader = reader;
        this.writer = writer;
    }
    // The methods of read and write have the functions of crypt and thread-safety spontaneously
    public void write(String msg) throws InvalidAlgorithmParameterException, NoSuchPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        synchronized (writer){   // Need to prevent 2 threads write one Socket at the same time
            writer.write(Crypt.EncryptAES(msg, sessionKey) + "\n");
            writer.flush();
        }
    }
    public String read() throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return Crypt.DecryptAES(reader.readLine(), sessionKey);
    }
}
