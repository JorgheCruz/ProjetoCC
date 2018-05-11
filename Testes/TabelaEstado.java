import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.lang.String;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;

/**
 *
 * @author treishy
 */
public class TabelaEstado {

    private Map<String,Estado> tabela;

    public TabelaEstado() {
        tabela = new HashMap<String,Estado>();
    }
    public Map<String, Estado> getTabela() {
        return tabela;
    }

    public void setTabela(Map<String, Estado> tabela) {
        this.tabela = tabela;
    }
    
    public void updateEstado(String ID, Estado newState){
        tabela.put(ID,newState);
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
}

