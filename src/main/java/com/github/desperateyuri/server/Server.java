package com.github.desperateyuri.server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server {
    private int port = 8964;
    private final List<Connection> connections = new ArrayList<>();
    public Server(int port){
        this.port = port;
    }
    public void removeConnection(Connection connection) {
        synchronized (connections){
            connections.remove(connection);
        }
    }
    public void addConnection(Connection connection){
        synchronized (connections){
            connections.add(connection);
        }
    }
    public List<Connection> getConnections() { return connections; }
    public void beginListening(){
        ServerSocket ss = null;
        try{
            ss = new ServerSocket(port);
        } catch (Exception e) {
            System.out.println("Creating ServerSocket failed.");
            System.exit(1);
        }
        while(true){
            Socket s = null;
            DataInputStream dataInput = null;
            DataOutputStream dataOutput = null;
            BufferedReader reader = null;
            BufferedWriter writer = null;
            try{
                s = ss.accept();
                System.out.println("Socket created successfully");
                dataInput = new DataInputStream(s.getInputStream());
                dataOutput = new DataOutputStream(s.getOutputStream());
                reader = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
            } catch (Exception e){
                System.out.println("Creating Socket failed.");
                continue;
            }

            // Exchange key with client using ECDH
            byte[] sessionKey = null;
            try{
                sessionKey = Crypt.keyExchange(dataInput,dataOutput);
            }catch (Exception e){
                System.out.println("Sharing SessionKey failed.");
                System.out.println(e);
                try{
                    s.close();
                    continue;
                } catch (IOException ioe){
                    System.exit(1);
                }
            }

            // Creating an object of Connection and append it to the Connection List
            String id = null, name = null, ciphertext = null;
            System.out.println("Start to readLine.");
            try{
                ciphertext = reader.readLine();
                id = Crypt.DecryptAES(ciphertext, sessionKey);
                ciphertext = reader.readLine();
                name = Crypt.DecryptAES(ciphertext, sessionKey);
                writer.write(Crypt.EncryptAES("%tT\nWelcome ".formatted(new Date()) + name + " (" + id + ") " + "join!\n\n", sessionKey) + "\n");
                writer.flush();
            }catch (Exception e){
                System.out.println("Getting ID and name failed.");
                try{
                    s.close();
                    continue;
                }catch (IOException ioe){
                    System.exit(1);
                }
            }

            System.out.println("Connected from " + s.getRemoteSocketAddress());
            Connection connection = new Connection(s, id, name, sessionKey, reader, writer);
            addConnection(connection);

            // Start thread, and start listening and broadcasting
            Thread t = new handler(this, connection);
            t.start();
        }
    }
}

class handler extends Thread{
    private final Server server;
    private final Connection connection;
    public handler(Server server, Connection connection){
        this.server = server;
        this.connection = connection;
    }
    @Override
    public void run(){
        commandOrNot c_temp = new commandOrNot();
        while(true){
            try{
                String msg = connection.read();
                int result = c_temp.check(server, connection, msg);
                System.out.println(msg);
                if(result == commandState.commandNotfound){
                    broadcast(connection.getName() + "(" + connection.getID() +"):\n" + msg);
                }
            }catch (Exception e){
                server.removeConnection(connection);  // Server doesn't need to maintain Socket
                broadcast("%tT\nThe member ".formatted(new Date()) + connection.getName() + " (" + ") " + connection.getID() + " has gone.\n\n");
            }
        }
    }
    public void broadcast(String msg){
        for(Connection item: server.getConnections()){
            if(!item.inBlockList(connection.getID())){
                try{
                    item.write("%tT\n".formatted(new Date())+ msg + "\n\n");
                }catch (Exception e){
                    server.removeConnection(item);
                }
            }
        }
    }
}
