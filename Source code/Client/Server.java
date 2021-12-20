
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author hovan
 */
//public class Server {
//    private ServerSocket serverSocket;
//}

public class Server {

    private ServerSocket serverSocket;

    public  Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void startServer(){
        try{
            while (!serverSocket.isClosed()){

                Socket socketClient = serverSocket.accept();
                if(socketClient != null){
                    ClientHandler clientHandler = new ClientHandler(socketClient);
                    Thread thread = new Thread(clientHandler);
                    thread.start();
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