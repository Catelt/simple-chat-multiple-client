import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements  Runnable{
    public static  ArrayList<ClientHandler>  listClient =  new ArrayList<>();
    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;
    private String nameClient;
    private DataInputStream dis;
    private DataOutputStream dos;

    public  ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.bw = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(),StandardCharsets.UTF_8));
            this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
            this.dis = new DataInputStream(this.socket.getInputStream());
            this.dos = new DataOutputStream(this.socket.getOutputStream());
            this.nameClient = br.readLine();
            listClient.add(this);
            sendListOnline(this.nameClient);
            sendMessengeAll("SERVER-ADD:"+this.nameClient);
            System.out.println(nameClient + " Connect");

        }catch (Exception e){
            System.out.println("Error:" + e);
            e.printStackTrace();
        }

    }

    @Override
    public void run(){
        String msgFromChat;
        int flag = 0;
        String nameReceiver = "";
        while(!socket.isClosed()){
            try{
                if(flag == 1){
                    int fileNameLength = dis.readInt();
                    if (fileNameLength > 0) {
                        byte[] fileNameBytes = new byte[fileNameLength];
                        dis.readFully(fileNameBytes, 0, fileNameBytes.length);

                        int fileContentLength = dis.readInt();
                        if (fileContentLength > 0) {
                            byte[] fileContentBytes = new byte[fileContentLength];
                            dis.readFully(fileContentBytes, 0, fileContentLength);
                            sendFile(fileNameBytes,fileContentBytes,nameReceiver);

                            flag = 0;
                        }
                    }
                }

                msgFromChat = br.readLine();
                if(msgFromChat.contains("CLIENT&FILE:")) {
                    nameReceiver = msgFromChat.substring(msgFromChat.indexOf(":")+1);
                    flag = 1;
                }
                if(msgFromChat.contains("CLIENT-OFF:")){
                    String name = msgFromChat.substring(msgFromChat.indexOf(":")+1);
                    
                    int index = -1;
                    for(int i = 0; i < listClient.size();i++){
                        if(listClient.get(i).nameClient.equals(name)){
                            index = i;
                            break;
                        }
                    }
                    if(index!= -1){
                        listClient.get(index).close(socket,bw,br);
                        if(listClient.size() > 0){
                            sendMessengeAll("SERVER-REMOVE:"+name);
                        }
                        System.out.println(name + " disconnect");
                    }
                }
                else if(msgFromChat.contains("&&CLIENTCHAT:")){
                    String receiver = msgFromChat.substring(msgFromChat.indexOf("&&CLIENTCHAT:")+13);
                    String messenge = msgFromChat.substring(0,msgFromChat.indexOf("&&CLIENTCHAT:"));
                    sendMessenge(messenge,receiver);
                }
            }catch (Exception e){
                System.out.println("Error2:" + e);
            }
        }
    }
    synchronized void sendFile(byte[] fileNameBytes,byte[] fileContentBytes,String receive){
        for(ClientHandler clientHandler : listClient){
            try{
                if(clientHandler.nameClient.equals(receive)){
                    clientHandler.bw.write("SERVER&FILE");
                    clientHandler.bw.newLine();
                    clientHandler.bw.flush();
                    TimeUnit.SECONDS.sleep(2);

                    clientHandler.dos.writeInt(fileNameBytes.length);
                    clientHandler.dos.write(fileNameBytes);

                    clientHandler.dos.writeInt(fileContentBytes.length);
                    clientHandler.dos.write(fileContentBytes);
                    break;
                }
            } catch (Exception e){
                System.out.println("Error:" + e);
            }
        }
    }

    synchronized void sendMessenge(String messenge,String receive){
        for(ClientHandler clientHandler : listClient){
            try{
                if(clientHandler.nameClient.equals(receive)){
                    clientHandler.bw.write(messenge);
                    clientHandler.bw.newLine();
                    clientHandler.bw.flush();
                    break;
                }
            } catch (Exception e){
                System.out.println("Error:" + e);
            }
        }
    }

    synchronized public void sendMessengeAll(String messenge){
        for(ClientHandler clientHandler : listClient){
            try{
                if(!clientHandler.nameClient.equals(nameClient)){
                    clientHandler.bw.write(messenge);
                    clientHandler.bw.newLine();
                    clientHandler.bw.flush();
                }
            } catch (Exception e){
                System.out.println("Error:" + e);
            }
        }
    }
    public void removeClient(){
        listClient.remove(this);
    }

    public void close(Socket socket, BufferedWriter bw , BufferedReader br){
        removeClient();;
        try{
            if(bw != null){
                bw.close();;
            }
            if(br != null){
                br.close();;
            }
            if(socket != null){
                socket.close();;
            }
        }catch (IOException e){
            System.out.println("Error:" + e);
        }
    }

    public void sendListOnline(String receive){
        for(ClientHandler clientHandler : listClient){
            sendMessenge("SERVER-ADD:"+ clientHandler.nameClient,receive);
        }
    }

}
