import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author treishy
 */
public class TabelaEstado {
    private Map<String,Estado> tabela;

    public TabelaEstado(){
        tabela= new HashMap<String,Estado>();
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
    
    public String toString(){	
        Iterator iterator = tabela.keySet().iterator();
        StringBuilder str = new StringBuilder();
        while (iterator.hasNext()) {
           String key = iterator.next().toString();
           String value = tabela.get(key).toString();
	   //System.out.println(key);
           str.append("IP: " + key + "\nEstado: " + value +"\n");
        } 
        return str.toString();
    }
    public static void main(String args[]){
        TabelaEstado table = new TabelaEstado();
        Estado estadoum = new Estado();
        Estado estadodois = new Estado();
        table.getTabela().put("200",estadoum);
        table.getTabela().put("201",estadodois);
        System.out.println(table.toString());
    }
}

