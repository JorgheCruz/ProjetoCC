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
    private int maxBW;
    private long maxRTT;

    public TabelaEstado() {
        tabela = new HashMap<String,Estado>();
        averageBW = 0;
    }
    public Map<String, Estado> getTabela() {
        return tabela;
    }

    public void setTabela(Map<String, Estado> tabela) {
        this.tabela = tabela;
    }
    
    public void updateEstado(String ID, Estado newState){
        tabela.put(ID,newState);
        if (newState.getBw()>maxBW) maxBW =newState.getBW();
        if (newState.getRtt()>maxRTT) maxRTT =newState.getRtt();

    }
    
    public String toString() {	

        Iterator iterator = tabela.keySet().iterator();
        StringBuilder str = new StringBuilder();
	String ID;
	Estado state;

	//str.append('\r');
	str
		.append("| IP:PORTA              | RAM     | CPU     | RTT |")
		.append('\n')
		.append("---------------------------------------------------");

        
	while (iterator.hasNext()) {
           ID = (String) iterator.next();
           state = tabela.get(ID);
           str
        	   .append('\n')
	   	   .append(String.format("| %s", ID))
		   .append(String.join("", Collections.nCopies( 22 - ID.length() , " ")))
		   .append(String.format("| %.2f %% ", state.getRam()))
		   .append(String.format("| %.2f %% ", state.getCpu()))
		   .append(String.format("| %-3d |", state.getRtt()));
	}

	str.append('\n');

        return str.toString();
    }

    public void updateState(String ID, double ram, double cpu, long rtt) {
    	
	Estado state; 
	if (this.tabela.containsKey(ID)) {
		state = tabela.get(ID);
		long oldrtt = state.getRtt();

		/** Calculate new RTT */
		rtt = (long) (0.125 * rtt + (0.875) * oldrtt);
		
		state.setRam(ram);
		state.setCpu(cpu);
		state.setRtt(rtt);
	}
	else {
		state = new Estado(ram, cpu, rtt);
		this.tabela.put(ID, state);
	}
    }
    public void updateBandwidth(String ID,int EstimatedBandwidth){
        //trocar para valores do stor dados a dividir por slot time
        Estado state= tabela.get(ID);
        int newBandwidth;
        if(state.getBw() == 0) newBandwidth= EstimatedBandwidth;
        else newBandwidth = (int)(0.125* EstimatedBandwidth + 0.875 * state.getBw());
        state.setBw(newBandwidth);
    }
    
    public String getBestServer(int packetSize) {
        Iterator iterator = tabela.keySet().iterator();
        String maxID=null,ID;
        Estado state;
        float maxScore=0,score;
        while (iterator.hasNext()) {
           ID = (String) iterator.next();
           state = tabela.get(ID);
           if(state.isAvailable()){
               score = getScore(state);

                if (score > maxScore) {
                    maxScore = score;
                    maxID = ID;
                }
           }
        }
        return maxID;
    }
    
    public float getScore(Estado state){
        double ram = state.getRam();
        double cpu = state.getRam();
        long rtt =state.getRam();
        int bw = state.getRam();
        int base = 100;
        if (bw == 0) base=200;
        float score = (float) (ram + cpu + 100*(1- (rtt/maxRTT)) + 100 *(bw/maxBW));
        return score;
    }
        //setAvailable(0);
}

