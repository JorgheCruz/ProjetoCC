/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Proxy;

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.io.BufferedReader;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.DatagramSocket;
import java.net.SocketException;
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
    private TabelaEstado stateTable;

    MonitorSend(SharedTimeSent time, TabelaEstado stateTable, DatagramSocket socket, String address) throws IOException {

        try{
            IP = InetAddress.getByName(address);
        }
        catch(UnknownHostException e) {
            IP = null;
            System.err.println(e.getMessage());
        }

        this.time = time;
        this.socket = socket;
        this.stateTable = stateTable;
    }

    public void run() {

        boolean needStates = true;
        int sleeptime = 5000;
        
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
    private TabelaEstado stateTable;

    MonitorReceive(SharedTimeSent time, TabelaEstado stateTable, DatagramSocket socket) {

        this.socket = socket;
        this.time = time;
        this.stateTable = stateTable;

        byte[] aReceber = new byte[1024];
        answer = new DatagramPacket(aReceber, aReceber.length);
    }

    @Override
    public void run() {
        
        boolean moreStates = true;
        BufferedReader reader;
        String msgReceived, ID;
        double ram, cpu;
        long rtt, timeReceived, timeSent, delayTime;
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
                delayTime = Long.parseLong(scanner.nextLine());

                /** Timestamp when request was sent */
                timeSent = this.time.getTimestamp();

                rtt = timeReceived - timeSent - delayTime;

                this.stateTable.updateState(ID, ram, cpu, rtt);
        }
    }
}

public class Monitor {
    
    private final DatagramSocket socket;
    private final SharedTimeSent time;
    private final TabelaEstado stateTable;
    
    public Monitor() throws SocketException {
        this.socket = new DatagramSocket();
        this.time = new SharedTimeSent();
        this.stateTable = new TabelaEstado();
    }

    public void start(String address) throws IOException, InterruptedException {

        Thread send = new MonitorSend(time, stateTable, socket, address);
        Thread receive = new MonitorReceive(time, stateTable, socket);
        send.start();
        receive.start();
    }

}

class SharedTimeSent {

        public volatile long timestamp;

        public void setTimestamp(long ts) { timestamp = ts; }
        public long getTimestamp() { return timestamp; }
}