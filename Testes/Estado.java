import java.io.IOException;
import java.lang.management.OperatingSystemMXBean;
import java.lang.Runtime;
import java.lang.management.ManagementFactory;
import java.util.Scanner;
/**
 *
 * @author treishy
 */

public class Estado {

    private long ram;
    private double cpu;
    private long rtt;
    private float lb;
    private boolean available;
   // private OperatingSystemMXBean os;
    
    public Estado(){
        this.lb = (float) 7.5;
        available = true;
        updateEstado();
    }
    
    public Estado(long ram, double cpu, long rtt){
        this.ram = ram;
        this.cpu = cpu;
        this.rtt = rtt;
    }
    public double getCpu() {
        return cpu;
    }

    public float getLb() {
        return lb;
    }

    public long getRam() {
        return ram;
    }

    public long getRtt() {
        return rtt;
    }

    public void setCpu(double cpu) {
        this.cpu = cpu;
    }

    public void setLb(float lb) {
        this.lb = lb;
    }

    public void setRam(long ram) {
        this.ram = ram;
    }

    public void setRtt(long rtt) {
        this.rtt = rtt;
    }
    
    public void updateEstado(){
        int mb= 1024*1024;
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        setCpu(os.getSystemLoadAverage());
        Runtime instance = Runtime.getRuntime();
        long availableRam = instance.freeMemory()/mb;
        setRam(availableRam);
        System.out.println("CPU Usage: " + os.getSystemLoadAverage() + "Free Ram :" + availableRam);

        
    }
    
    

    public String toString(){
        String resposta = ("Ram = " + this.getRam() + "\nCPU Usage: " + this.getCpu() + "\nRTT: " +this.getRtt());
        return resposta;
    } 
}

