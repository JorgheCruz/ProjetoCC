import java.lang.management.OperatingSystemMXBean;
import java.lang.Runtime;
import java.lang.management.ManagementFactory;
/**
 *
 * @author treishy
 */

public class Estado {

    private long ram;
    private double cpu;
    private int rtt = 7;
    private float lb;
    private boolean available;
   // private OperatingSystemMXBean os;
    
    public Estado(){
        this.lb = (float) 7.5;
        available = true;
        updateEstado();
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

    public int getRtt() {
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

    public void setRtt(int rtt) {
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
        String resposta = ("Ram = " + this.getRam() + " CPU Usage: " + this.getCpu() + " Time: " + System.currentTimeMillis());
        return resposta;
    } 
}

