import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.lang.String;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

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
    
    public Estado createEstado(String data){
        String s,key;
        long  ram,rtt,oldrtt;
        double cpu;
        Estado newState;
        Scanner scanner = new Scanner(data);
        s=scanner.nextLine();
        key=s; // considerei first line ser o ID
        s=scanner.nextLine();        
        ram= Long.parseLong(s);
        s=scanner.nextLine();
        cpu= Double.parseDouble(s);
        s=scanner.nextLine();
        rtt= Long.parseLong(s);
        if (tabela.containsKey(key)) {
            newState = tabela.get(key);
            oldrtt = newState.getRtt();
            rtt= (long) (0.125 * rtt + (0.875) * oldrtt);}
        
        newState = new Estado(ram,cpu,rtt);
        return newState;
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
        String test = "\n200\n2\n2\n5";
        Estado estadotres = createEstado(test);
        table.getTabela().put("2",estadotres);

        //System.out.println(estadotres.toString());
        System.out.println(table.toString());

    }
}

