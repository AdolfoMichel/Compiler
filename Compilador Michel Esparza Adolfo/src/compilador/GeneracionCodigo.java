
package compilador;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeneracionCodigo {
    boolean bloquePrincipal = false;
    PrintStream out = null;
    String tablaSimbolos = "";
    String etiquetas = "";
    Vector<String> principal = new Vector<String>();
    Vector<String> metodos = new Vector<String>();
    int lPrincipal = 1, numEt = 0;
    Hashtable<Integer, TablaSimbolos> direccionMetodos = new Hashtable<Integer, TablaSimbolos>();
    Hashtable<Integer, Vector<String>> etiquetasPrincipal = new Hashtable<Integer, Vector<String>>();
    Hashtable<Integer, Vector<String>> etiquetasMetodos = new Hashtable<Integer, Vector<String>>();
    
    GeneracionCodigo(String nombre, String ruta){
        System.out.println("Ruta: "+ruta);
        int i;
        for(i = ruta.length()-1; i >= 0; i--){
            if( ruta.charAt(i) == '/') break;
        }
        String archivo = ruta.substring(0, i)+"/"+nombre+".eje";
        System.out.println("Ruta Codigo: "+archivo);
        try {
            out = new PrintStream(new FileOutputStream(archivo));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeneracionCodigo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void tablaSimbolos(TablaSimbolos aux){
        String simbolo = "";
        simbolo += aux.nombre + ",";
        if( aux.clase.equals("global") || aux.clase.equals("local") ) simbolo += "V,";
        else if( aux.clase.equals("parametro") ) simbolo += "P,";
        else if( aux.clase.equals("constante") ) simbolo += "C,";
        else if( aux.clase.equals("funcion") || aux.clase.equals("procedimiento") ) simbolo += "F,";
        if( aux.tipo.equals("entero") ) simbolo += "E,";
        else if( aux.tipo.equals("decimal") ) simbolo += "R,";
        else if( aux.tipo.equals("alfabetico") ) simbolo += "A,";
        else if( aux.tipo.equals("logico") ) simbolo += "L,";
        else simbolo += "E,";
        if( aux.dimension == null ) simbolo += "0,0,";
        else simbolo += aux.dimension.x + "," + aux.dimension.y + ",";
        if( aux.valor == null ) simbolo += "#, ";
        else{
            simbolo += "#, ";
            if( aux.tipo.equals("logico") ){
                if( aux.valor.equals("true") ) inicializacion("LIT", "V", "0");
                else inicializacion("LIT", "F", "0");
            }
            else inicializacion("LIT", aux.valor, "0");
            inicializacion("STO", "0", aux.nombre);
        }
        tablaSimbolos += simbolo + "\n";
    }
    
    public void etiquetas(String nombre, int linea){
        String etiqueta = "";
        etiqueta += "_" + nombre + ",I,I," + linea + ",0,#, ";
        etiquetas += etiqueta + "\n";
    }
    
    public void instrucciones(String instruccion, String op1, String op2){
        String orden = "";
        orden += instruccion + " " + op1 + ", " + op2;
        if(bloquePrincipal) principal.add(orden + "\n");
        else metodos.add(orden + "\n");
    }
    
    public void inicializacion(String instruccion, String op1, String op2){
        String orden = "";
        orden += instruccion + " " + op1 + ", " + op2;
        principal.add(orden + "\n");
    }
    
    public void registrarEtiqueta(int indice, String etiqueta){
        Vector<String> aux;
        if( bloquePrincipal ){
            aux = etiquetasPrincipal.get(indice);
            if( aux != null ){
                aux.add(etiqueta);
            }
            else{
                aux = new Vector<String>();
                aux.add(etiqueta);
                etiquetasPrincipal.put(indice, aux);
            }
        }
        else{
            aux = etiquetasMetodos.get(indice);
            if( aux != null ){
                aux.add(etiqueta);
            }
            else{
                aux = new Vector<String>();
                aux.add(etiqueta);
                etiquetasMetodos.put(indice, aux);
            }
        }
    }
    
    public void generarEjecutable(){
        String programa = "";
        Vector<String> auxEt;
        TablaSimbolos aux = null;
        for(int i = 0; i < metodos.size(); i++){
            aux = direccionMetodos.get(i);
            if( aux != null ){
                aux.dimension = new Dimensiones(lPrincipal);
                tablaSimbolos(aux);
                aux = null;
            }
            auxEt = etiquetasMetodos.get(i);
            if( auxEt != null ){
                for(int j = 0; j < auxEt.size(); j++){
                    etiquetas(auxEt.elementAt(j), lPrincipal);
                }
                auxEt = null;
            }
            programa += lPrincipal + " " + metodos.elementAt(i);
            lPrincipal++;
        }
        for(int i = 0; i < principal.size(); i++){
            auxEt = etiquetasPrincipal.get(i);
            if( auxEt != null ){ 
                for(int j = 0; j < auxEt.size(); j++){
                    etiquetas(auxEt.elementAt(j), lPrincipal);
                }
            }
            programa += lPrincipal + " " + principal.elementAt(i);
            lPrincipal++;
        }
        out.print(tablaSimbolos);
        out.print(etiquetas + "@\n");
        out.print(programa);
        out.close();
    }
}
