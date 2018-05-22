import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author treishy
 */


class ClientToServer extends Thread {
    
    Socket client, server;
    InputStream dIn;
    OutputStream dOut;
    String serverID;
    TabelaEstado stateTable;
    
    public ClientToServer(Socket client, Socket server, String serverID, TabelaEstado stateTable) throws IOException {
        
        this.stateTable = stateTable;
        this.client = client;
        this.server = server;
        this.dIn = client.getInputStream();
        this.dOut = server.getOutputStream();
        this.serverID = serverID;
    }
    
    @Override
    public void run() {
        
        String fromClient;
        
        try {
            InputStreamReader in = new InputStreamReader(dIn);
            BufferedReader br = new BufferedReader(in);
            
            PrintWriter pw = new PrintWriter(dOut, false);
            
            while ((fromClient = br.readLine()) != null) {
            
                pw.println(fromClient);
                //System.out.println(fromClient);
                stateTable.addBytes(serverID, fromClient.getBytes().length);
                pw.flush();
                
            }
            
        } catch (IOException ex) {
            try {
                client.close();
                server.close();
            } catch (IOException ex1) {
                Logger.getLogger(ClientToServer.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }
}

class ServerToClient extends Thread {
    
    InputStream dIn;
    OutputStream dOut;
    String serverID;
    TabelaEstado stateTable;
    
    public ServerToClient(Socket client, Socket server, String serverID, TabelaEstado stateTable) throws IOException {
        
        this.stateTable = stateTable;
        this.dIn = server.getInputStream();
        this.dOut = client.getOutputStream();
        this.serverID = serverID;
    }
    
    @Override
    public void run() {
        
        String fromServer;
        
        try {
            InputStreamReader in = new InputStreamReader(dIn);
            BufferedReader br = new BufferedReader(in);
            
            PrintWriter pw = new PrintWriter(dOut, false);
            
            while ((fromServer = br.readLine()) != null) {
            
                pw.println(fromServer);
                //System.out.println(fromServer);
                stateTable.addBytes(serverID, fromServer.getBytes().length);
                pw.flush();
                
            }
            
        } catch (IOException ex) {
                Logger.getLogger(ClientToServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

public class ReverseProxy {
    
    public static Socket getServerSocket(String ID) throws UnknownHostException, IOException {
        
        String[] serverID = ID.split(":");
                
        InetAddress IP = InetAddress.getByName(serverID[0]);
        int port = Integer.parseInt(serverID[1]);
                
        return (new Socket(IP, port));
    }
    
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {

        InetAddress IP;
        TabelaEstado stateTable = new TabelaEstado();
        
        Monitor monitor = new Monitor(stateTable);
        monitor.start("239.8.8.8");

	ServerSocket welcomeSocket = new ServerSocket(80);        
        
        while (true) {
            
            Socket client = welcomeSocket.accept();
            
            //System.out.println("New client!");
            String serverID = stateTable.getBestServer();
            //System.out.println("Best server: " + serverID);
            Socket server = getServerSocket(serverID);
            
            Thread clientToServer = new ClientToServer(client, server, serverID, stateTable);
            Thread serverToClient = new ServerToClient(client, server, serverID, stateTable);
            
            clientToServer.start();
            serverToClient.start();
        }
    }
}
