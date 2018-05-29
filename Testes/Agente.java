import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import static java.lang.Thread.sleep;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.net.UnknownHostException;
/**

A classe Agente guarda várias componentes do Estado de um servidor e 
é responsável por responder ao probing Multicast feito pelo Monitor 
com as mesmas.

  @author	Grupo 28
*/
public class Agente {

    /** Percentagem de ram disponível*/
	private double ram;
	/** Percentagem de CPU disponível*/
    private double cpu;
    /** Tempo de delay da mensagem*/
	private long delayTime;
	/** Memória total do servidor*/
	private final double totalMemory;

	/** Construtor da classe*/
    public Agente() throws UnknownHostException {

        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		this.totalMemory = (double) (os.getTotalPhysicalMemorySize() / (1024*1024));
    }	
    /** Método para atualizar Estados*/
    public void updateEstado() {
        
		int MB = 1024*1024;
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    	   
		/** Calculo de CPU livre */
        int cores = os.getAvailableProcessors();
		double loadAvg = (double) os.getSystemLoadAverage();	
		cpu = (loadAvg / cores) * 100;
		cpu = 100 - (Math.round(cpu * 100) / 100.0d);

		/** Calculo da RAM livre*/
        double usedMemory = (double) ((this.totalMemory - (os.getFreePhysicalMemorySize() / MB)));
        ram = (usedMemory / this.totalMemory) * 100;
        ram = 100 - (Math.round(ram * 100) / 100.0d);
    }

    /** 
    Override da função toString
	@param serverID id do servidor
	@return string com id do servidor, ram, cpu e tempo de delay das mensagens
    */
    public String toString(String serverID) {

		String resposta = (serverID + "/" + this.ram + "/" + this.cpu + "/" + this.delayTime);
        return resposta;
    }


    /** Método main da classe*/
    public static void main (String args[]) throws SocketException, IOException, InterruptedException {
        
		Agente agente = new Agente();
        String serverID = args[0] + ":" + args[1];
		Cypher cypher = new Cypher();

		/** Join Multicast Socket in IP 239.8.8.8 and Port 8888 */
        MulticastSocket socketToReceive = new MulticastSocket(8888);
		InetAddress group = InetAddress.getByName("239.8.8.8");
		socketToReceive.joinGroup(group);

		DatagramSocket socketToSend = new DatagramSocket();
	
		byte[] received = new byte[1024];
		DatagramPacket requestPacket = new DatagramPacket(received, received.length);
		DatagramPacket answerPacket; 
       
       	while (true) {

        	socketToReceive.receive(requestPacket);
			String requestMessage = cypher.checkMessage(new String(requestPacket.getData(), 0, requestPacket.getLength()));
		
			/** Verificar autenticação e integrĩdade */
			if (requestMessage != null) {

				/** Atrasar a resposta entre 0 e 10 ms */
				agente.delayTime = (long) (Math.random() * 10);
				sleep(agente.delayTime);

				agente.updateEstado();
				String answer = agente.toString(serverID);
				String answerHash = cypher.encrypt(answer);
				String msg = answer + "\n" + answerHash;

				/** Preparar e enviar o Datagram Packet com a informação do servidor */
				answerPacket = new DatagramPacket(msg.getBytes(), msg.length(), requestPacket.getAddress(), requestPacket.getPort());
				socketToSend.send(answerPacket);
			}
		}
    }
}
