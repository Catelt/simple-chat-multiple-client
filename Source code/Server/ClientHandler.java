import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements  Runnable{
    public static  ArrayList<ClientHandler>  listClient =  new ArrayList<>();
    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;
    private String nameClient;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String nameDirectory = "";

    public  ClientHandler(Socket socket,String user){
        try {
            this.socket = socket;
            this.bw = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(),StandardCharsets.UTF_8));
            this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
            this.dis = new DataInputStream(this.socket.getInputStream());
            this.dos = new DataOutputStream(this.socket.getOutputStream());
            this.nameClient = user;
            listClient.add(this);
            TimeUnit.SECONDS.sleep(1);
            sendListOnline(this.nameClient);
            sendMessengeAll("SERVER-ADD:"+this.nameClient);
            System.out.println(nameClient + " Connect");

        }catch (Exception e){
            System.out.println("Error:" + e);
            e.printStackTrace();
        }

    }


    public static int checkUser(String user){
        for(ClientHandler clientHandler : listClient) {
            if (clientHandler.nameClient.equals(user)){
                return 1;
            }
        }
        return 0;
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
                if(msgFromChat.contains("LOAD-FILE:")){
                    String receiver = msgFromChat.substring(0,msgFromChat.indexOf("-"));
                    String nameFile = msgFromChat.substring(msgFromChat.indexOf(":")+1);
                    String[] content = loadMessenge(nameFile);
                    for(String line: content){
                        sendMessenge("LOADCHAT:"+line,receiver);
                    }
                }
                else if(msgFromChat.contains("&&CLIENTCHAT:")){
                    String sender = msgFromChat.substring(0,msgFromChat.indexOf(":"));
                    String receiver = msgFromChat.substring(msgFromChat.indexOf("&&CLIENTCHAT:")+13);
                    String messenge = msgFromChat.substring(0,msgFromChat.indexOf("&&CLIENTCHAT:"));
                    saveMessenge(messenge + "\n",nameDirectory+receiver+"-"+sender+".txt");
                    saveMessenge(messenge + "\n",nameDirectory+sender+"-"+receiver+".txt");
                    sendMessenge(messenge,receiver);
                }
            }catch (Exception e){
                System.out.println("Error:" + e);
            }
        }
    }

    public static void saveMessenge(String messenger , String nameFile){
        try{
            FileOutputStream os = new FileOutputStream(nameFile,true);
            OutputStreamWriter osw = new OutputStreamWriter(os,StandardCharsets.UTF_8);
            BufferedWriter br = new BufferedWriter(osw);
            br.append(messenger);
            br.close();
        }
        catch (Exception e){
            System.out.println("Error:" + e);
        }
    }

    public static String[] loadMessenge(String nameFile){
        ArrayList<String> result = new ArrayList<String>();
        try{
            File f = new File(nameFile);
            if(f.exists()){
                InputStream is = new FileInputStream(nameFile);
                InputStreamReader isr = new InputStreamReader(is,StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                while(true){
                    String str = br.readLine();
                    if(str == null || str.equals("")){
                        break;
                    }
                    result.add(str);
                }
                br.close();
            }
            
        } catch (Exception e){
            System.out.println("Error:" + e);
        }
        return result.toArray(new String[0]);
    }

    synchronized void sendFile(byte[] fileNameBytes,byte[] fileContentBytes,String receive){
        for(ClientHandler clientHandler : listClient){
            try{
                if(clientHandler.nameClient.equals(receive)){
                    clientHandler.bw.write("SERVER&FILE");
                    clientHandler.bw.newLine();
                    clientHandler.bw.flush();
                    TimeUnit.SECONDS.sleep(1);

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
