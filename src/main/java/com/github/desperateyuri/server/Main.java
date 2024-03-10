package com.github.desperateyuri.server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8964;
        if(args.length != 0){
            if(Judge.judgePort(args[0])){
                port = Integer.parseInt(args[0]);
            }
        }
        Thread thread_LoginAndRegister = new begin_LoginAndRegister();
        thread_LoginAndRegister.start();
        Thread thread_Server = new begin_Server(port);
        thread_Server.start();
    }
}
class begin_LoginAndRegister extends Thread {
    @Override
    public void run(){
        try{
            LoginAndRegister login_and_register = new LoginAndRegister();
            login_and_register.beginListening();
        }catch (Exception e){
            System.out.println("Exception:" + e);
            System.exit(1);
        }
    }
}

class begin_Server extends Thread{
    private final int port;
    public begin_Server(int port){
        this.port = port;
    }
    @Override
    public void run(){
        Server server = new Server(port);
        server.beginListening();
    }
}
