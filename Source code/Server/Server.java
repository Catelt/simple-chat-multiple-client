
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author hovan
 */
class HandleLogin extends Thread {
    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;
    public void sendMessenge(String messenger) {
        try {
            bw.write(messenger);
            bw.newLine();
            bw.flush();
        } catch (Exception e) {
            System.out.println("Error:" + e);
        }
    }

    public static int checkExisting(ArrayList<Account> accounts, String username){
        for(int i = 0 ; i < accounts.size();i++){
            if(accounts.get(i).getUsername().contains(username)){
                return 1;
            }
        }
        return 0;
    }

    public static int checkPassword(ArrayList<Account> accounts,String username,String password){
        for(int i = 0 ; i < accounts.size();i++){
            if(accounts.get(i).getUsername().equals(username)){
                if(accounts.get(i).getPassword().equals(password)){
                    return 1;
                }
            }
        }
        return 0;
    }

    public static void saveAccount(String nameFile,String username, String password){
        try{
            BufferedWriter br = new BufferedWriter(new FileWriter(nameFile,true));
            br.append(username + "|" + password +"\n");
            br.close();
        }
        catch (Exception e){
            System.out.println("Error:" + e);
        }
    }


    public static Account readLine(String str){
        Account account = new Account();
        int index = str.indexOf('|');
        String user = str.substring(0,index);
        String pass = str.substring(index + 1);
        account.setUsername(user);
        account.setPassword(pass);
        return account;
    }


    public static ArrayList<Account> loadAccount(String nameFile){
        ArrayList<Account> accounts = new ArrayList<Account>();
        try{
            File f = new File(nameFile);
            if(f.exists()){
                BufferedReader br = new BufferedReader(new FileReader(nameFile));
                while(true){
                    String str = br.readLine();
                    if(str == null || str.equals("")){
                        break;
                    }

                    Account account_new = readLine(str);
                    accounts.add(account_new);
                }
                br.close();
            }

        } catch (Exception e){
            System.out.println("Error:" + e);
        }
        return accounts;
    }


    HandleLogin(Socket socket){
        try{
            this.socket = socket;
            this.bw = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        }
        catch(Exception e){
            System.out.println("Error:" + e);
        }

    }
    @Override
    public void run(){
        String msgFromChat;
        while(!socket.isClosed()) {
            try{
            msgFromChat = br.readLine();
            if(msgFromChat.contains("LOGIN&")){
                msgFromChat = msgFromChat.substring(msgFromChat.indexOf("&") + 1);
                String username = msgFromChat.substring(0,msgFromChat.indexOf("|"));
                String password = msgFromChat.substring(msgFromChat.indexOf("|")+1);
                if(checkPassword(loadAccount("account.txt"), username, password) == 1) {
                    if(ClientHandler.checkUser(username) == 0){
                        sendMessenge("LOGIN-SUCCESS");
                        Thread t = Thread.currentThread();
                        ClientHandler clientHandler = new ClientHandler(socket,username);
                        Thread thread = new Thread(clientHandler);
                        thread.start();
                        t.stop();


                    }
                    else{
                        sendMessenge("USER-EXIST");
                    }
                }
                else{
                    sendMessenge("LOGIN-FAIL");
                }
            }
            if(msgFromChat.contains("REGISTER:")){
                msgFromChat = msgFromChat.substring(msgFromChat.indexOf(":") + 1);
                String username = msgFromChat.substring(0,msgFromChat.indexOf("|"));
                String password = msgFromChat.substring(msgFromChat.indexOf("|")+1);
                if(checkExisting(loadAccount("account.txt"), username) == 1){
                    sendMessenge("REGIS-EXISTING");
                }
                else{
                    saveAccount("account.txt",username,password);
                    sendMessenge("REGIS-SUCCESS");
                }
            }
            if(msgFromChat.contains("@OFF")){
                if(socket != null){
                    socket.close();
                }
            }
            } catch (Exception e){
                System.out.println("Error:" + e);
            }
        }
    }
}

public class Server {

    private ServerSocket serverSocket;


    public  Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }


    public void startServer(){
        try{
            while (!serverSocket.isClosed()){
                Socket socketClient = serverSocket.accept();
                if(socketClient!=null){
                    HandleLogin login = new HandleLogin(socketClient);
                    login.start();
                }

            }
        }
        catch (Exception e){
            System.out.println("Error:" + e);
        }
    }

    public void closeServerSocket(){
        try{
            if(serverSocket != null){
                serverSocket.close();;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public  static void main(String[] args) throws  IOException{
        ServerSocket serverSocket =  new ServerSocket(1703);
        Server server = new Server(serverSocket);
        System.out.println("Server have been started");
        server.startServer();

        server.closeServerSocket();
    }
}