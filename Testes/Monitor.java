import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import static java.sql.DriverManager.println;
import java.net.DatagramSocket;

/**
 *
 * @author treishy
 */

class MonitorSend extends Thread {
    
    private final DatagramSocket socket;
    private DatagramPacket packet;
    private InetAddress IP;
    private final int port = 8888;

    MonitorSend(DatagramSocket socket, String address) throws IOException {
    
        try{
            IP = InetAddress.getByName(address);
        }
        catch(UnknownHostException e) {
            IP = null;
            System.err.println(e.getMessage());
        }
        
        if (IP != null) {
            String msg = "Send me information!";
            packet = new DatagramPacket(msg.getBytes(), msg.length(), IP, port);
        }

	this.socket = socket;
    }

    public void run() {

	boolean needStates = true;
        int sleeptime = 10000;

	while (needStates) {
		
		try {
		socket.send(this.packet);
        	
		sleep(sleeptime);
		}
		catch(IOException|InterruptedException e) {	
            		System.err.println(e.getMessage());
		}
	}
    }

}

class MonitorReceive extends Thread {

    private final DatagramSocket socket;
    DatagramPacket resposta;

    MonitorReceive(DatagramSocket socket) {
    	
	this.socket = socket;
	
	byte[] aReceber = new byte[1024];
	resposta = new DatagramPacket(aReceber, aReceber.length);
    }

    public void run() {
    
	boolean moreStates = true;
	
	while (moreStates) {

		try {
			socket.receive(resposta);
		}
		catch (IOException e) {
            		System.err.println(e.getMessage());
		}

		String received = new String(resposta.getData(), 0, resposta.getLength());
		System.out.println(received);
		System.out.println("Porta: " + resposta.getPort());
		System.out.println("Endereço: " + resposta.getAddress().getHostAddress());
	}
    }
}

public class Monitor {
    
    public static void main(String args[]) throws IOException, InterruptedException {
        
	DatagramSocket socket = new DatagramSocket();

	Thread send = new MonitorSend(socket, "239.8.8.8");
	Thread receive = new MonitorReceive(socket);
	send.start();
	receive.start();

	
    }
        
}
