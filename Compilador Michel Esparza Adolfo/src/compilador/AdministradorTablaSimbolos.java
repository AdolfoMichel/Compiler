
package compilador;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class AdministradorTablaSimbolos {
    Hashtable<Integer, TablaSimbolos> tabla = new Hashtable();
    int id = 0;
    
    public void Agregar(TablaSimbolos elemento){
        tabla.put(Integer.valueOf(id), elemento);
        id++;
    }
    
    public boolean Existe(String n){
        if(tabla.isEmpty()){
            return false;
        }
        else{
            TablaSimbolos aux;
            int cont;
            Enumeration<Integer> claves = tabla.keys();
            while(claves.hasMoreElements()){
                cont = claves.nextElement();
                aux = tabla.get(Integer.valueOf(cont));
                if( aux.nombre.equals(n) ){ 
                    return true;
                }
            }
        
        }
        return false;
    }
    
    public void mostrar(){
        if(tabla.isEmpty()){
            //System.out.println("No hay elementos");
        }
        else{
            int id;
            TablaSimbolos aux;
            Enumeration<Integer> claves = tabla.keys();
            while(claves.hasMoreElements()){
                id = claves.nextElement();
                aux = tabla.get(Integer.valueOf(id));
                if(aux.dimension != null)
                    System.out.println("|\t" + id + "\t|\t" + aux.nombre + "\t\t|\t" + aux.clase+ "\t\t|\t" + aux.tipo+ "\t\t|\t" + aux.dimension.x+ "\t\t|\t" + aux.dimension.y+ "\t\t|\t" + aux.valor);
                else
                    System.out.println("|\t" + id + "\t|\t" + aux.nombre + "\t\t|\t" + aux.clase+ "\t\t|\t" + aux.tipo+ "\t\t|\t" + "null" + "\t\t|\t" + aux.valor);
            }
        }
    }
    
    public String registrarFuncion(int line, Vector<String> funcion, TablaSimbolos e, AnalizadorSintactico x){
        e.dimension = null;
        e.valor = null;
        e.nombre += "." + funcion.get(0);
        if(funcion.size() == 1) e.nombre += ".void";
        for(int i = 1; i < funcion.size(); i+=2) e.nombre += "." + funcion.elementAt(i);
        if( Existe(e.nombre) ) x.error(line, "semantica", "El identificador <" + funcion.elementAt(0) + "> ya existe");
        else{ 
            Agregar(new TablaSimbolos(e.nombre, e.clase, e.tipo, e.dimension, e.valor));
            x.code.direccionMetodos.put(x.code.metodos.size(), new TablaSimbolos(e.nombre, e.clase, e.tipo, e.dimension, e.valor));
        }
        e.clase = "parametro";
        String base = x.nombreMetodo = e.nombre;
        for(int i = 1; i < funcion.size(); i+=2){
            e.tipo = funcion.elementAt(i);
            e.nombre = base + "." + funcion.elementAt(i+1);
            if( Existe(e.nombre) ) x.error(line, "semantica", "El identificador <" + funcion.elementAt(i+1) + "> ya existe");
            else{ 
                Agregar(new TablaSimbolos(e.nombre, e.clase, e.tipo, e.dimension, e.valor));
                x.code.tablaSimbolos(e);
            }
        }
        for(int i = funcion.size()-1; i > 1; i -= 2){
            x.code.instrucciones("STO", "0", base + "." + funcion.elementAt(i));
        }
        return base;
    }
    
    public String BajarNivel(String nombre){
        int i;
        if( !nombre.equals("") ){
            for(i = nombre.length()-1; i >= 0; i--){
                if( nombre.charAt(i) == '.' ) break;
            }
            return nombre.substring(0, i);
        }
        return null;
    }
    
    public TablaSimbolos Buscar(String n, String c){
        if(tabla.isEmpty()){
            return null;
        }
        else{
            TablaSimbolos aux;
            int cont;
            Enumeration<Integer> claves = tabla.keys();
            while(claves.hasMoreElements()){
                cont = claves.nextElement();
                aux = tabla.get(Integer.valueOf(cont));
                if( aux.nombre.equals(n) && aux.clase.equals(c) )
                    return aux;
            }
        
        }
        return null;
    }
}
