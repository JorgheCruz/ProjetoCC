import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import static java.sql.DriverManager.println;
import java.net.InetAddress;
import java.net.MulticastSocket;
import static java.lang.Thread.sleep;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 *
 * @author treishy
 */
public class Agente {
   
	private long ram;
        private double cpu;
        
        
    public void updateEstado(){
        int mb= 1024*1024;
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        cpu= os.getSystemLoadAverage();
        Runtime instance = Runtime.getRuntime();
        long availableRam = instance.freeMemory()/mb;
        ram= availableRam;
        //System.out.println("CPU Usage: " + os.getSystemLoadAverage() + "Free Ram :" + availableRam);
        
    }
    public String toString(){
        String resposta = ("Ram = " + this.ram + "\nCPU Usage: " + this.cpu);
        return resposta;
    }
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

		agente.updateEstado();
		String msg = agente.toString();
		monitorAddress = request.getAddress();
		DatagramPacket answer = new DatagramPacket(msg.getBytes(), msg.length(), monitorAddress, request.getPort());
		socketToSend.send(answer);
	
	}
        
    }
}
