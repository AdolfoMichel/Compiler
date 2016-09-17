
package compilador;

public class TablaSimbolos {
    String nombre, clase, tipo, valor;
    Dimensiones dimension;

    TablaSimbolos(){
        dimension = new Dimensiones(0, 0);
        nombre = "";
        clase = "";
        tipo = "";
        dimension.x = 0;
        dimension.y = 0;
        valor = "";
    }
    
    TablaSimbolos(String n, String c, String t, Dimensiones d, String v){
        nombre = n;
        clase = c;
        tipo = t;
        dimension = d;
        valor = v;
    }
}
