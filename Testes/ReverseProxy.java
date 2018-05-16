import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author treishy
 */

class dealClient extends Thread {
    
    Socket connectionClient;
    DataInputStream dIn;
    DataOutputStream dOut;
    TabelaEstado stateTable;
    
    public dealClient(Socket connection) throws IOException {
        this.connectionClient = connection;
        dIn = new DataInputStream(connection.getInputStream());
    }
    
    public int sendDataToServer(Socket connectionServer, byte[] message, int length) throws IOException {
        
        this.dOut = new DataOutputStream(connectionServer.getOutputStream());
            
        this.dOut.writeInt(message.length);
                
        long start = System.currentTimeMillis();
        this.dOut.write(message);
        long end = System.currentTimeMillis();
                
        return (int) (1000 * (length / (end - start))); 
    }
    
    public void confirmTransfer(Socket connectionClient) throws IOException {
        
        String msg = "Transfer completed!";
        DataOutputStream dOut = new DataOutputStream(connectionClient.getOutputStream());
        
        dOut.writeChars(msg);
    }
    
    @Override
    public void run() {
        
        int length, port;
        String[] serverID;
        InetAddress IP;
        
        try {
            length = dIn.readInt();
            
            if (length > 0) {
                byte[] message = new byte[length];
                dIn.readFully(message, 0, message.length);
            
                String ID = this.stateTable.getBestServer(length);

                serverID = ID.split(":");
                
                IP = InetAddress.getByName(serverID[0]);
                port = Integer.parseInt(serverID[1]);
                
                Socket connectionServer = new Socket(IP, port);
                
                int bandwidth = this.sendDataToServer(connectionServer, message, length);
                this.stateTable.updateBandwidth(ID, bandwidth);
                
                this.confirmTransfer(connectionClient);

                connectionServer.close();
            }
        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(dealClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.connectionClient.close();
    }
}

public class ReverseProxy {
    
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {

        Monitor monitor = new Monitor();
        monitor.start("239.8.8.8");

	ServerSocket welcomeSocket = new ServerSocket(80);
        
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            Thread dealClient = new dealClient(connectionSocket);
        }
    }
}
