import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Collections;

/**
 *
 * @author treishy
 */
public class TabelaEstado {

    private Map<String,Estado> tabela;
    private int maxBW = 50;
    private long maxRTT = 1;
    private int slotTime;

    public TabelaEstado() {
        tabela = new HashMap<String,Estado>();
        slotTime=10;
    }
    public Map<String, Estado> getTabela() {
        return tabela;
    }

    public void setTabela(Map<String, Estado> tabela) {
        this.tabela = tabela;
    }
    
    public void updateEstado(String ID, Estado newState) { // TODO: Update Name!
        
        tabela.put(ID,newState);
        if (newState.getBw() > maxBW) maxBW = newState.getBw();
        if (newState.getRtt() > maxRTT) maxRTT = newState.getRtt();

    }
    
    public String toString() {	

        Iterator iterator = tabela.keySet().iterator();
        StringBuilder str = new StringBuilder();
	String ID;
	Estado state;

	//str.append('\r');
	str
		.append("| IP:PORTA              | RAM     | CPU     | RTT | BW  | Availability |")
		.append('\n')
		.append("------------------------------------------------------------------------");

        
	while (iterator.hasNext()) {
            ID = (String) iterator.next();
            state = tabela.get(ID);
            str
                    .append('\n')
                    .append(String.format("| %s", ID))
                    .append(String.join("", Collections.nCopies( 22 - ID.length() , " ")))
                    .append(String.format("| %.2f %% ", state.getRam()))
                    .append(String.format("| %.2f %% ", state.getCpu()))
                    .append(String.format("| %-3d ", state.getRtt()))
                    .append(String.format("| %-3d ", state.getBw()));
            
            if (state.isAvailable()) str.append("|     true     |");
            else str.append("|     false    |");
	}

	str.append('\n');

        return str.toString();
    }
    
    public void incTimeout(){
        
        Iterator iterator = tabela.keySet().iterator();
        Estado state;
        String ID;
        int timeout;
        
	while (iterator.hasNext()) {
           ID = (String) iterator.next();
           state = tabela.get(ID);
           state.increaseTimeout();
           timeout = state.getTimeout();
           if (timeout > 6) tabela.remove(ID);
           else if (timeout > 3) state.setUnavailable();
        }
    }

    public void updateState(String ID, double ram, double cpu, long rtt) {
    	
	Estado state; 
	if (this.tabela.containsKey(ID)) {
		state = tabela.get(ID);
		long oldrtt = state.getRtt();
                int oldbw= state.getBw();
                int bw;
                
		/** Calculate new RTT */
		rtt = (long) (0.125 * rtt + (0.875) * oldrtt);
		bw = (int) (0.125 * ((int) (state.getBytesSent())/this.slotTime)  + 0.875 * oldbw);
		state.setRam(ram);
		state.setCpu(cpu);
		state.setRtt(rtt);
                state.setBw(bw);
                state.resetBytes();
                state.resetTimeout();
                state.resetUsed();
                state.setAvailable();
                this.updateEstado(ID, state);
	}
	else {
		state = new Estado(ram, cpu, rtt);
		this.updateEstado(ID,state);
	}
    }
    public void addBytes(String ID,long bytes){
        
        Estado state;
        
        if (this.tabela.containsKey(ID)) {
		state = tabela.get(ID);
                state.addBytes(bytes);
        }
    }
    
    public String getBestServer() {
        
        Iterator iterator = tabela.keySet().iterator();
        String maxID = null, ID;
        Estado state;
        float maxScore = 0, score;
        
        while (iterator.hasNext()) {
            ID = (String) iterator.next();
            state = tabela.get(ID);
            if (state.isAvailable()) {
                score = getScore(state);

                if (score > maxScore) {
                    maxScore = score;
                    maxID = ID;
                }
            }
        }
        
        state = tabela.get(maxID);
        state.incUsed();
        return maxID;
    }
    
    public float getScore(Estado state){
        
        double ram = state.getRam();
        double cpu = state.getCpu();
        long rtt = state.getRtt();
        int bw = state.getBw();
        int uses = state.getUsed();
        int base = 100;
        
        if (bw == 0) base = 200;
        
        float score = (float) (ram + cpu + 100 * (1 - (rtt/maxRTT)) + 100 * (bw / maxBW) - 20 * uses);
        return score;
    }
}

