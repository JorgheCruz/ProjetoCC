import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Collections;

/**
 A Tabela de Estados contém informações sobre todos os servidores recentemente conectados e
 ainda o intervalo de tempo, denominado slotTime, em que são atualizados todos os estados referidos na mesma
 além do hostname do Reverse Proxy.
 @author grupo 28
*/
public class TabelaEstado {
    /** Map com o ip do servidor e estado do mesmo */
    private Map<String,Estado> tabela;
    /** Valor máximo obtido da largura de banda no tempo de execução do ReverseProxy*/
    private int maxBW = 50;
    /** Valor máximo de Rount Trip Timeobtido no tempo de execução do ReverseProxy*/
    private long maxRTT = 1;
    /** Valor definido em segundos para verificação do estado dos agentes*/
    private int slotTime;
    /** Hostname do Reverse Proxy*/
    private String proxyHostname;

    /**
     Construtor
     @param ProxyHostname Hostname do Reverse Proxy
    */
    public TabelaEstado(String ProxyHostname) {
        tabela = new HashMap<String,Estado>();
        slotTime = 10;
        this.proxyHostname = ProxyHostname;
    }
    
    /** Método que devolve a tabela*/
    public Map<String, Estado> getTabela() {
        return tabela;
    }
    /** Metodo que altera a tabela*/
    public void setTabela(Map<String, Estado> tabela) {
        this.tabela = tabela;
    }
    
    /** 
    Método que dá update a um estado especifico
    @param ID identificador do servidor
    @param newState novo estado
    */
    public void updateEstado(String ID, Estado newState) {
        tabela.put(ID,newState);
        if (newState.getBw() > maxBW) maxBW = newState.getBw();
        if (newState.getRtt() > maxRTT) maxRTT = newState.getRtt();
    }
    
    /** Override da função toString*/
    public String toString() {	

        Iterator iterator = tabela.keySet().iterator();
        StringBuilder str = new StringBuilder();

        String ID;
        Estado state;
        str
            .append("Reverse Proxy Hostname: ")
            .append(this.proxyHostname)
            .append("\n")
            .append("Reverse Proxy Port: 80")
            .append('\n')

            .append("-----------------------------------------------------------------------------------------------------")
            .append('\n')

            .append("| IP:PORTA              | RAM     | CPU     | RTT |  BW   | Availability | UsedCount | TimeoutCount |")
            .append('\n')

            .append("-----------------------------------------------------------------------------------------------------");
        
        while (iterator.hasNext()) {
            ID = (String) iterator.next();
            state = tabela.get(ID);
            str
                .append('\n')
                .append(String.format("| %s", ID))
                .append(String.join("", Collections.nCopies( 22 - ID.length() , " ")));
            
            if (state.getRam() < 10)
                str.append(String.format("| %.2f %%  ", state.getRam()));
            else 
                str.append(String.format("| %.2f %% ", state.getRam()));
            
            if (state.getCpu() < 10)
                str.append(String.format("| %.2f %%  ", state.getCpu()));
            else 
                str.append(String.format("| %.2f %% ", state.getCpu()));
                            
            str
                .append(String.format("| %-3d ", state.getRtt()))
                .append(String.format("| %-4d  ", state.getBw()));
            
            if (state.isAvailable()) str.append("|     true     ");
            else str.append("|     false    ");

            str
                .append(String.format("|    %-2d     ", state.getUsed()))
                .append(String.format("|      %-2d      |", state.getTimeout()));
        }

        str.append('\n');

        return str.toString();
    }
    
    /** Método que incrementa o timeout*/
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
    /** 
    Método que dá update a um estado
    @param ID identificador do estado
    @param ram novo valor de ram disponivel
    @param cpu novo valor de cpu disponivel
    @param rtt novo valor da rtt
    */
    public void updateState(String ID, double ram, double cpu, long rtt) {
        Estado state; 
        if (this.tabela.containsKey(ID)){
            state = tabela.get(ID);
            long oldrtt = state.getRtt();
            int oldbw = state.getBw();
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
        } else {
            state = new Estado(ram, cpu, rtt);
            this.updateEstado(ID, state);
        }
    }

    /** 
    Método que adiciona bytes enviados
    @param ID identificador do servidor
    @param bytes numero de bytes enviados
    */
    public void addBytes(String ID,long bytes){
        
        Estado state;
        if (this.tabela.containsKey(ID)) {
            state = tabela.get(ID);
            state.addBytes(bytes);
        }
    }
    
    /** 
    Método que devolve o identificador do melhor servidor
    @return identificador do melhor servidor
    */
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
        state.increaseUsed();
        return maxID;
    }
    
    /** Método que calcula o score de um estado
    @param state estado sobre o qual pretendemos calcular o score
    @return score do estado
    */
    public float getScore(Estado state){
        
        double ram = state.getRam();
        double cpu = state.getCpu();
        long rtt = state.getRtt();
        int bw = state.getBw();
        int uses = state.getUsed();
        int base = 100;
        if (bw == 0) base = 200;
        float score;
        if(cpu > 5 && ram > 5)
            score = (float) (ram * 2 + cpu + 100 * (1 - (rtt/maxRTT)) + 100 * (bw / maxBW) - 40 * uses);
        else 
            score = (float) (100 * (1 - (rtt/maxRTT)) + 100 * (bw / maxBW) - 40 * uses);
        return score;
    }
}

