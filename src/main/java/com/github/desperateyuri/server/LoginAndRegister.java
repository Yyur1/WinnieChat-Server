package com.github.desperateyuri.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.sql.Connection;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


public class LoginAndRegister {
    private ServerSocket ss;
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/winniechat?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    static final String USER = "root";
    static final String PASSWD = "root";
    private Socket s = null;
    BufferedReader reader = null;
    BufferedWriter writer = null;
    DataInputStream dataInput = null;
    DataOutputStream dataOutput = null;
    byte[] sessionKey = null;
    private final ObjectMapper objectMapper = new ObjectMapper();
    Connection sqlConnection = null;
    Statement statement = null;

    public LoginAndRegister() throws IOException {
        ss = new ServerSocket(5000);
    }
    private void register(clientMessage clientmsg) throws SQLException, NoSuchAlgorithmException, IOException,
            InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException {
        String name = (String) clientmsg.map().get("name");
        String password = (String) clientmsg.map().get("password");
        MessageDigest md = MessageDigest.getInstance("md5");
        byte[] bytesPasswd = md.digest(password.getBytes());
        String sql = String.format("insert into user (name, password) values (\"%s\",\"%s\")", name, Base64.getEncoder().encodeToString(bytesPasswd));
        int num = statement.executeUpdate(sql);
        serverMessage servermsg;
        HashMap<String, Object> map = new HashMap<>();
        if(num != 1){
            servermsg = new serverMessage(serverMessage.Command.REGISTER, serverMessage.Status.ERROR, map);
            writer.write(Crypt.EncryptAES(objectMapper.writeValueAsString(servermsg), sessionKey) + "\n");
            writer.flush();
            return;
        }
        ResultSet rs = statement.executeQuery("select max(id) from user;");
        rs.next();
        String ID = String.valueOf(rs.getString("max(id)"));
        map.put("ID", ID);
        servermsg = new serverMessage(serverMessage.Command.REGISTER, serverMessage.Status.OK, map);
        writer.write(Crypt.EncryptAES(objectMapper.writeValueAsString(servermsg), sessionKey) + "\n");
        writer.flush();
    }
    private void login(clientMessage clientmsg) throws SQLException, NoSuchAlgorithmException, IOException,
            InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException {
        Map<String, Object> map = clientmsg.map();
        MessageDigest md = MessageDigest.getInstance("md5");
        String ID = (String) map.get("ID");
        String Password = (String) map.get("Password");
        byte[] bytesPasswd = md.digest(Password.getBytes());
        String sql = String.format("select name from user where id = %s and password = \"%s\";",ID, Base64.getEncoder().encodeToString(bytesPasswd));
        ResultSet rs = statement.executeQuery(sql);
        serverMessage servermsg;
        HashMap<String, Object> remap = null;
        if(!rs.next()){
            // Login failed
            servermsg = new serverMessage(serverMessage.Command.LOGIN, serverMessage.Status.ERROR, remap);
            writer.write(Crypt.EncryptAES(objectMapper.writeValueAsString(servermsg), sessionKey) + "\n");
            writer.flush();
            return;
        }
        // Login success
        remap = new HashMap<>();
        remap.put("Name", rs.getString("name"));
        servermsg = new serverMessage(serverMessage.Command.LOGIN, serverMessage.Status.OK, remap);
        writer.write(Crypt.EncryptAES(objectMapper.writeValueAsString(servermsg), sessionKey) + "\n");
        writer.flush();
    }

    public void beginListening() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
            InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException, SQLException {
        try{
            Class.forName(JDBC_DRIVER);
            sqlConnection = DriverManager.getConnection(DB_URL, USER, PASSWD);
            statement = sqlConnection.createStatement();
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        while(true){
            s = ss.accept();
            System.out.println("Socket s ok");
            reader = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
            dataInput = new DataInputStream(s.getInputStream());
            dataOutput = new DataOutputStream(s.getOutputStream());
            sessionKey = Crypt.keyExchange(dataInput, dataOutput);

            clientMessage clientmsg = objectMapper.readValue(Crypt.DecryptAES(reader.readLine(), sessionKey), clientMessage.class);
            System.out.println("Recv client msg ok");
            System.out.println(objectMapper.writeValueAsString(clientmsg));
            switch (clientmsg.command()) {
                case REGISTER -> {
                    register(clientmsg);
                }
                case LOGIN -> {
                    login(clientmsg);
                }
            }
            s.close();
        }
    }
}
