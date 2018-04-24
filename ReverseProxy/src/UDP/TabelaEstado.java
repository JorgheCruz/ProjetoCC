/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UDP;

import java.util.HashMap;
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
}
