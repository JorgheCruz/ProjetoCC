import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import static java.sql.DriverManager.println;
import java.net.InetAddress;
import java.net.MulticastSocket;
import static java.lang.Thread.sleep;

/**
 *
 * @author treishy
 */
public class Agente {
   
	Estado state = new Estado();

    public static void main (String args[]) throws SocketException, IOException, InterruptedException {
        
	Agente agente = new Agente();

        MulticastSocket socketToReceive = new MulticastSocket(8888);
	InetAddress group = InetAddress.getByName("239.8.8.8"), monitorAddress;
	socketToReceive.joinGroup(group);
	DatagramSocket socketToSend = new DatagramSocket();
       
	boolean moreRequest = true;

       	while (moreRequest) {

        	byte[] msgReceived = new byte[1024]; 
        	DatagramPacket request =  new DatagramPacket(msgReceived, msgReceived.length); 
        	socketToReceive.receive(request);
	
		/** Delay answer */
		sleep((long)Math.random() * 5000);

		agente.state.updateEstado();
		String msg = agente.state.toString();
		monitorAddress = request.getAddress();
		DatagramPacket answer = new DatagramPacket(msg.getBytes(), msg.length(), monitorAddress, request.getPort());
		socketToSend.send(answer);
	
	}
        
    }
}
