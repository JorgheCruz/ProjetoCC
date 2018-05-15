/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import static java.lang.Thread.sleep;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

/**
 *
 * @author treishy
 */
public class Agente {

        private double ram;
        private double cpu;
        private long delayTime;
        private final double totalMemory;

    public Agente () {
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.totalMemory = (double) (os.getTotalPhysicalMemorySize() / (1024*1024));
    }

    public void updateEstado(){

        int MB = 1024*1024;
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        int cores = os.getAvailableProcessors();
        double loadAvg = (double) os.getSystemLoadAverage();
        cpu = (loadAvg / cores) * 100;
        cpu = Math.round(cpu * 100) / 100.0d;

        double usedMemory = (double) ((this.totalMemory - (os.getFreePhysicalMemorySize() / MB)));
        ram = (usedMemory / this.totalMemory) * 100;
        ram = Math.round(ram * 100) / 100.0d;
    }
    
    @Override
    public String toString(){

        String resposta = (this.ram + "\n" + this.cpu + "\n" + this.delayTime + "\n");
        return resposta;
    }
    
    @SuppressWarnings("SleepWhileInLoop")
    public static void main (String args[]) throws SocketException, IOException, InterruptedException {

        Agente agente = new Agente();

        MulticastSocket socketToReceive = new MulticastSocket(8888);
        InetAddress group = InetAddress.getByName("239.8.8.8"), monitorAddress;
        socketToReceive.joinGroup(group);
        DatagramSocket socketToSend = new DatagramSocket();

        byte[] aReceber = new byte[1024];
        DatagramPacket request = new DatagramPacket(aReceber, aReceber.length);

        boolean moreRequest = true;

        while (moreRequest) {

                socketToReceive.receive(request);
                String msgReceived = new String(request.getData(), 0, request.getLength());

                /** Delay answer between 0 and 10 ms */
                agente.delayTime = (long) (Math.random() * 10);
                sleep(agente.delayTime);

                agente.updateEstado();
                String msg = agente.toString();

                monitorAddress = request.getAddress();
                DatagramPacket answer = new DatagramPacket(msg.getBytes(), msg.length(), monitorAddress, request.getPort());
                socketToSend.send(answer);
        }

    }
}