import java.io.IOException;
import static java.lang.Thread.sleep;
import java.io.BufferedReader;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import static java.sql.DriverManager.println;
import java.net.DatagramSocket;
import java.util.Scanner;

/**
 *
 * @author treishy
 */

class MonitorSend extends Thread {
    
    private final DatagramSocket socket;
    private DatagramPacket packet;
    private InetAddress IP;
    private final int port = 8888;
    private SharedTimeSent time;

    MonitorSend(SharedTimeSent time, DatagramSocket socket, String address) throws IOException {
    
        try{
            IP = InetAddress.getByName(address);
        }
        catch(UnknownHostException e) {
            IP = null;
            System.err.println(e.getMessage());
        }
        
	this.time = time;
	this.socket = socket;
    }

    public void run() {

	boolean needStates = true;
        int sleeptime = 10000;

	if (IP != null) {
		byte[] buf = new byte[1];
            	this.packet = new DatagramPacket(buf, buf.length, IP, port);
        }

	while (needStates) {
		
		this.time.setTimestamp(System.currentTimeMillis());

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
    private DatagramPacket answer;
    private SharedTimeSent time;
    private TabelaEstado stateTable = new TabelaEstado();

    MonitorReceive(SharedTimeSent time, DatagramSocket socket) {
    	
	this.socket = socket;
	this.time = time;
	
	byte[] aReceber = new byte[1024];
	answer = new DatagramPacket(aReceber, aReceber.length);
    }

    public void run() {
    
	boolean moreStates = true;
	BufferedReader reader;
	String msgReceived, ID;
	double ram, cpu;
	long rtt, timeReceived, timeSent;
	Scanner scanner;
	
	while (moreStates) {

		try {
			socket.receive(answer);
		}
		catch (IOException e) {
            		System.err.println(e.getMessage());
		}

		msgReceived = new String(answer.getData(), 0, answer.getLength());
		reader = new BufferedReader(new StringReader(msgReceived));

		/** Timestamp when answer was received */
		timeReceived = System.currentTimeMillis();
		
		ID = answer.getAddress().getHostAddress() + ":" + answer.getPort();
		
		scanner = new Scanner(msgReceived);

		ram = Double.parseDouble(scanner.nextLine());
		cpu = Double.parseDouble(scanner.nextLine());
		
		/** Timestamp when request was sent */
		timeSent = this.time.getTimestamp();

		rtt = timeReceived - timeSent;

		this.stateTable.updateState(ID, ram, cpu, rtt);
		System.out.print("\033[H\033[2J");
		System.out.print(this.stateTable.toString());
	}
    }
}

public class Monitor {
    
    public static void main(String args[]) throws IOException, InterruptedException {
        
	String address = "239.8.8.8";
	DatagramSocket socket = new DatagramSocket();
	SharedTimeSent time = new SharedTimeSent();
	
	Thread send = new MonitorSend(time, socket, address);
	Thread receive = new MonitorReceive(time, socket);
	send.start();
	receive.start();
    }
        
}

class SharedTimeSent {

	public volatile long timestamp;

	public void setTimestamp(long ts) { timestamp = ts; }
	public long getTimestamp() { return timestamp; }
}
