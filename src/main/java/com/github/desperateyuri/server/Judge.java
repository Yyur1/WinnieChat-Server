package com.github.desperateyuri.server;

public class Judge {
    static boolean judgeIP(String ip){
        String[] splitNums = ip.split("\\.");
        int temp;
        if(splitNums.length != 4){
            return false;
        }
        for(int i = 0; i < splitNums.length; i++){
            try{
                temp = Integer.parseInt(splitNums[i]);
            } catch (NumberFormatException nfe){
                return false;
            }
            if(((temp == 0 || temp == 255) && (i == 0 || i == 3)) || (temp >= 224 && i == 0)){
                return false;
            }
            if(temp < 0 || temp > 255){
                return false;
            }
        }
        return true;
    }
    static boolean judgePort(String port){
        int temp;
        try {
            temp = Integer.parseInt(port);
        }catch (NumberFormatException nfe){
            return false;
        }
        return temp > 1024 && temp < 65535 && temp != 5000;  // login_and_Register.java is using port 5000
    }
}
