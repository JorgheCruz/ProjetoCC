import java.io.BufferedReader;
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
 *  A classe pipe tem como objetivo enviar dados de forma totalmente transparente entre o cliente e o servidor numa conexãoTCP. 
 * @author grupo 28
 */


class Pipe extends Thread {
    /**Sockets de origem e destino*/
    Socket from, dest;
    InputStream in;
    OutputStream out;
    /** IP do servidor*/
    String serverID;
    /** Tabela de estados*/
    TabelaEstado stateTable;
    
    /** 
    Construtor
    @param from socket de onde surgem as mensagens
    @param dest socket de destino das mensagens
    @param serverID identificador do servidor
    @param stateTable Tabela de Estados
    */
    public Pipe(Socket from, Socket dest, String serverID, TabelaEstado stateTable) throws IOException {
        this.stateTable = stateTable;
        this.from = from;
        this.dest = dest;
        this.in = from.getInputStream();
        this.out = dest.getOutputStream();
        this.serverID = serverID;
    }
    
    /**Override da função run*/
    @Override
    public void run() {
        byte[] buffer = new byte[1024 * 1024 * 10];
        int length;
        try {
            while ((length = in.read(buffer)) != -1) {
                stateTable.addBytes(serverID, length);
                out.write(buffer, 0, length);
            }
            from.close();
        } catch (Exception exc) {
            Logger.getLogger(Pipe.class.getName()).log(Level.SEVERE, null, exc);
        }
    }
}

/**
 *   A classe ReverseProxy corresponde a um agente TCP capaz de 
 *   atender pedidos na porta 80, vindos de qualquer cliente.
 */
public class ReverseProxy {

    /**
     Método que devolve o socket do servidor
     @param ID  IP
     @return novo socket do servidor
    */

    public static Socket getServerSocket(String ID) throws UnknownHostException, IOException {
        
        String[] serverID = ID.split(":");
                
        InetAddress IP = InetAddress.getByName(serverID[0]);
        int port = Integer.parseInt(serverID[1]);
                
        return (new Socket(IP, port));
    }

    /** 
    Metodo main do reverse proxy que  inicializa um Monitor com uma TabelaEstado 
    para monitorizar todos osservidores disponíveis e, em cada pedido, poder 
    escolher o melhor servidor para atender
    */
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {

        InetAddress IP = InetAddress.getLocalHost();
        String hostname = IP.getHostName();
        TabelaEstado stateTable = new TabelaEstado(hostname);
        
        Monitor monitor = new Monitor(stateTable);
        monitor.start("239.8.8.8");

        ServerSocket welcomeSocket = new ServerSocket(80);        
        
        while (true) {
            
            Socket client = welcomeSocket.accept();
            
            String serverID = stateTable.getBestServer();
            Socket server = getServerSocket(serverID);
            
            Thread clientToServer = new Pipe(client, server, serverID, stateTable);
            Thread serverToClient = new Pipe(server, client, serverID, stateTable);
            
            clientToServer.start();
            serverToClient.start();
        }
    }
}
