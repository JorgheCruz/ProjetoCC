import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
    DataInputStream dIn;
    DataOutputStream dOut;
    String serverID;
    TabelaEstado stateTable;
    
    public ClientToServer(Socket client, Socket server, String serverID, TabelaEstado stateTable) throws IOException {
        
        this.stateTable = stateTable;
        this.client = client;
        this.server = server;
        this.dIn = new DataInputStream(client.getInputStream());
        this.dOut = new DataOutputStream(server.getOutputStream());
        this.serverID = serverID;
    }
    
    @Override
    public void run() {
        
        int length;
        
        try {
            length = dIn.readInt();
            
            while (length > 0) {
                
                byte[] message = new byte[length];
                dIn.readFully(message, 0, message.length);
            
                dOut.writeInt(message.length);
                dOut.write(message);    
                stateTable.addBytes(serverID, (long) length);
                
                length = dIn.readInt();
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
    
    DataInputStream dIn;
    DataOutputStream dOut;
    
    public ServerToClient(Socket client, Socket server) throws IOException {
        
        this.dIn = new DataInputStream(server.getInputStream());
        this.dOut = new DataOutputStream(client.getOutputStream());
    }
    
    @Override
    public void run() {
        
        int length;
        
        try {
            length = dIn.readInt();
            
            while (length > 0) {
                
                byte[] message = new byte[length];
                dIn.readFully(message, 0, message.length);
            
                dOut.writeInt(message.length);
                dOut.write(message);
                
                length = dIn.readInt();
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
                        
            String serverID = stateTable.getBestServer();
            Socket server = getServerSocket(serverID);
            
            Thread clientToServer = new ClientToServer(client, server, serverID, stateTable);
            Thread serverToClient = new ServerToClient(client, server);
        }
    }
}
