
package compilador;

import java.util.Stack;
import java.util.Vector;

public class AnalizadorSintactico {
    String ruta = "";
    String nivelCiclo = "";
    Stack<String> variablesCiclos = new Stack<String>();
    GeneracionCodigo code;
    Stack<String> interrumpe = new Stack<String>();
    String lex="", tipoRetorno = "", nombrePrograma = "", tipo = "", errores = "", nombreMetodo = "";
    Vector<String> registrar = new Vector<String>();
    Stack<String> pilaAux;
    String tipoVar = "";
    AnalizadorLexico l = new AnalizadorLexico();
    AdministradorTablaSimbolos t = new AdministradorTablaSimbolos();
    TablaSimbolos e = new TablaSimbolos();
    Tipos TiOp = new Tipos();
    boolean declaracionValida = false, llamadaValida = false, retorno, libreria = false, ciclo, mistake = false;
    int valConst = 0; 

    AnalizadorSintactico(String absolutePath) {
        ruta = absolutePath;
    }
    
    public void error(int linea, String tipo, String desc) {
    	errores += linea + "\t" + tipo + "\t\t" + desc + "\n";
    	mistake = true;
    }    
    
    public String parser(String programa) {
        l.idx=0;
        l.token="";
        l.entrada = programa;
        sintactico();
        return errores;
    }
    
    public void sintactico() {
        lex = l.lexico();
        if( !lex.equals("programa") ) librerias();
    	if( !lex.equals("programa") ) error(l.linea, "sintaxis", "Se esperaba <programa> y llegó " + lex);
        lex = l.lexico();
        if( !l.token.equals("Identi") ) error(l.linea, "sintaxis", "Se esperaba un <Identificador> y llegó " + lex);
        else{ 
            nombrePrograma = lex;
            e.nombre += lex;
            code = new GeneracionCodigo(lex, ruta);
        }
        lex = l.lexico();
        if( !lex.equals("principal") ) declaracion();
        if( !lex.equals("principal") ) error(l.linea, "sintaxis", "Se esperaba <principal> y llegó " + lex);
        else{
            code.bloquePrincipal = true;
            code.registrarEtiqueta(0, "P");
        }
        lex = l.lexico();
        t.mostrar();
        if( !lex.equals("fin") ) bloqueInstrucciones();
        if( !lex.equals("fin") ) error(l.linea, "sintaxis", "Se esperaba <fin> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("de") ) error(l.linea, "sintaxis", "Se esperaba <de> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("principal") ) error(l.linea, "sintaxis", "Se esperaba <principal> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals(";") ) error(l.linea, "sintaxis", "Se esperaba < ; > y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("fin") ) error(l.linea, "sintaxis", "Se esperaba <fin> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("de") ) error(l.linea, "sintaxis", "Se esperaba <de> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("programa") ) error(l.linea, "sintaxis", "Se esperaba <principal> y llegó " + lex);
        lex = l.lexico();
        if( !l.token.equals("Identi") ) error(l.linea, "sintaxis", "Se esperaba un <Identificador> y llegó " + lex);
        else{
            if( !lex.equals(e.nombre) ) error(l.linea, "semantica", "Se esperaba el identificador del programa <" + e.nombre + "> y llegó " + lex);
        }
        lex = l.lexico();
        if( !lex.equals(".") ) error(l.linea, "sintaxis", "Se esperaba < . > y llegó " + lex);
        else{
            code.instrucciones("OPR", "0", "0");
            code.generarEjecutable();
        }
        
    }
    
    public boolean tipoDato(){
        TablaSimbolos aux;
        if( !lex.equals("entero") && !lex.equals("decimal") && !lex.equals("alfabetico") && !lex.equals("logico") ){ 
            aux = t.Buscar(nombrePrograma+"."+lex, "tipo");
            if( aux == null ) return false;
            else{
                tipoVar = aux.tipo;
                if(aux.dimension != null){
                    Dimensiones x = new Dimensiones(0);
                    x.x = aux.dimension.x;
                    x.y = aux.dimension.y;
                    e.dimension = x;
                }
                return true;
            }
        }
        tipoVar = lex;
        return true;
    }
    
    public void librerias(){
        if( !lex.equals("usando") ) error(l.linea, "sintaxis", "Se esperaba <usando> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("la") ) error(l.linea, "sintaxis", "Se esperaba <la> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("biblioteca") ) error(l.linea, "sintaxis", "Se esperaba <biblioteca> y llegó " + lex);
        lex = l.lexico();
        if( !l.token.equals("Identi") ) error(l.linea, "sintaxis", "Se esperaba un <Identificador> y llegó " + lex);
        else if( lex.equals("Entrada_Salida") ) libreria = true;
        lex = l.lexico();
        if( !lex.equals(";") ) error(l.linea, "sintaxis", "Se esperaba < ; > y llegó " + lex);
        lex = l.lexico();
    }
    
    public void declaracion(){
        if( lex.equals("constante") ) declaracionConstantes();
        else if( lex.equals("tipo") ) definicionTipos();
        else if( tipoDato() ) declaracionVariables();
        else if( lex.equals("funcion") ) declaracionFunciones();
        else if( lex.equals("procedimiento") ) declaracionProcedimientos();
        else return;
        if( !lex.equals("principal") ) declaracion();
    }
    
    public void declaracionConstantes(){
        declaracionValida = true;
        if( !lex.equals("constante") ){ 
            error(l.linea, "sintaxis", "Se esperaba <constante> y llegó " + lex);
            declaracionValida = false;
        }
        else e.clase = lex;
        lex = l.lexico();
        if( !tipoDato() ){ 
            error(l.linea, "sintaxis", "Se esperaba un <Tipo de Dato> y llegó " + lex);
            declaracionValida = false;
        }
        else e.tipo = tipoVar;
        lex = l.lexico();
        if( !l.token.equals("Identi") ){ 
            error(l.linea, "sintaxis", "Se esperaba un <Identificador> y llegó " + lex);
            declaracionValida = false;
        }
        else{
            e.nombre += "." + lex;
            if( t.Existe(e.nombre) ) error(l.linea, "semantica", "El identificador <" + e.nombre + "> ya existe");
        }
        lex = l.lexico();
        if( !lex.equals(":=") ) error(l.linea, "sintaxis", "Se esperaba < := > y llegó " + lex);
        lex = l.lexico();
        if( !valorConstante() ){ 
            error(l.linea, "sintaxis", "Se esperaba un <Valor para la Constante> y llegó " + lex);
            declaracionValida = false;
        }
        else{
            if( e.tipo.equals("entero") && !l.token.equals("CteEnt") ) error(l.linea, "semantica", "El valor <" + lex + "> no valido para el tipo de dato");
            else if( e.tipo.equals("decimal") && !l.token.equals("CteDec") ) error(l.linea, "semantica", "El valor <" + lex + "> no valido para el tipo de dato");
            else if( e.tipo.equals("alfabetico") && !l.token.equals("CteAlf") ) error(l.linea, "semantica", "El valor <" + lex + "> no valido para el tipo de dato");
            else if( e.tipo.equals("logico") && !l.token.equals("CteLog") ) error(l.linea, "semantica", "El valor <" + lex + "> no valido para el tipo de dato");
            else e.valor = lex;
        }
        lex = l.lexico();
        if( !lex.equals(";") ) error(l.linea, "sintaxis", "Se esperaba < ; > y llegó " + lex);
        else{
            e.dimension = null;
            if( declaracionValida ){ 
                t.Agregar(new TablaSimbolos(e.nombre, e.clase, e.tipo, e.dimension, e.valor));
                code.tablaSimbolos(e);
            }
            e.nombre = t.BajarNivel(e.nombre);
        }
        lex = l.lexico();
    }
    
    public void definicionTipos(){
        declaracionValida = true;
        e.dimension = null;
        if( !lex.equals("tipo") ){ 
            error(l.linea, "sintaxis", "Se esperaba <tipo> y llegó " + lex);
            declaracionValida = false;
        }
        else e.clase = lex;
        lex = l.lexico();
        if( !tipoDato() ){ 
            error(l.linea, "sintaxis", "Se esperaba un <Tipo de Dato> y llegó " + lex);
            declaracionValida = false;
        }
        else e.tipo = tipoVar;
        lex = l.lexico();
        if( !l.token.equals("Identi") ){ 
            error(l.linea, "sintaxis", "Se esperaba un <Identificador> y llegó " + lex);
            declaracionValida = false;
        }
        else e.nombre += "." + lex;
        lex = l.lexico();
        if( lex.equals("arreglo") ) definicionTiposVector();
        if( !lex.equals(";") ){ 
            error(l.linea, "sintaxis", "Se esperaba un < ; > y llegó " + lex);
            declaracionValida = false;
        }
        if( t.Existe(e.nombre) ) error(l.linea, "semantica", "El identificador <" + lex + "> ya existe");
        else{
            e.valor = null;
            if( declaracionValida ) t.Agregar(new TablaSimbolos(e.nombre, e.clase, e.tipo, e.dimension, e.valor));
        }
        e.nombre = t.BajarNivel(e.nombre);
        lex = l.lexico();
    }
    
    public int valorAb(int x, int y){
        if(x < 0 && y < 0){
            x *= -1;
            y *= -1;
        }
        return (y-x)+1;
    }
    
    public void definicionTiposVector(){
        int x=0, y=0;
        Dimensiones aux;
        if( !lex.equals("arreglo") ){ 
            error(l.linea, "sintaxis", "Se esperaba un <arreglo> y llegó " + lex);
            declaracionValida = false;
        }
        lex = l.lexico();
        if( !lex.equals("(") ){ 
            error(l.linea, "sintaxis", "Se esperaba < ( > y llegó " + lex);
            declaracionValida = false;
        }
        lex = l.lexico();
        if( !valorEnteroConstante() ){ 
            error(l.linea, "sintaxis", "Se esperaba <Valor Entero> y llegó " + lex);
            declaracionValida = false;
        }
        else x = valConst;
        lex = l.lexico();
        if( !lex.equals("a") ){ 
            error(l.linea, "sintaxis", "Se esperaba <a> y llegó " + lex);
            declaracionValida = false;
        }
        lex = l.lexico();
        if( !valorEnteroConstante() ){ 
            error(l.linea, "sintaxis", "Se esperaba <Valor Entero> y llegó " + lex);
            declaracionValida = false;
        }
        else y = valConst;
        if( x >= y ){ 
            error(l.linea, "Semantica", "El valor final debe ser mayor al inicial");
            x = 0;
        }
        else x = valorAb(x, y);
        lex = l.lexico();
        if( !lex.equals(")") ){ 
            definicionTiposMatriz();
            aux = new Dimensiones(x, valConst);
        }
        else aux = new Dimensiones(x);
        if( !lex.equals(")") ){ 
            error(l.linea, "sintaxis", "Se esperaba un < ) > y llegó " + lex);
            declaracionValida = false;
        }
        lex = l.lexico();
        e.dimension = aux;
    }
    
    public void definicionTiposMatriz(){
        int x = 0, y = 0;
        if( !lex.equals(",") ){ 
            declaracionValida = false;
            error(l.linea, "sintaxis", "Se esperaba < , > y llegó " + lex);
        }
        lex = l.lexico();
        if( !valorEnteroConstante() ){ 
            declaracionValida = false;
            error(l.linea, "sintaxis", "Se esperaba <Valor Entero Inicial> y llegó " + lex);
        }
        else x = valConst;
        lex = l.lexico();
        if( !lex.equals("a") ){ 
            declaracionValida = false;
            error(l.linea, "sintaxis", "Se esperaba <a> y llegó " + lex);
        }
        lex = l.lexico();
        if( !valorEnteroConstante() ){
            declaracionValida = false;
            error(l.linea, "sintaxis", "Se esperaba <Valor Entero Final> y llegó " + lex);
        }
        else y = valConst;
        if( x >= y ){ 
            error(l.linea, "Semantica", "El valor final debe ser mayor al inicial");
            valConst = 0;
        }
        else valConst = valorAb(x, y);
        lex = l.lexico();
    }
    
    public void declaracionVariables(){
        declaracionValida = true;
        String nombreVariable = "";
        if( nombrePrograma.equals(e.nombre) ) e.clase = "global";
        else e.clase = "local";
        e.dimension = null;
        if( !tipoDato() ){ 
            error(l.linea, "sintaxis", "Se esperaba un <Tipo de Dato> y llegó " + lex);
            declaracionValida = false;
        }
        else e.tipo = tipoVar;
        lex = l.lexico();
        if( !l.token.equals("Identi") ){ 
            error(l.linea, "sintaxis", "Se esperaba un <Identificador> y llegó " + lex);
            declaracionValida = false;
        }
        else{
            e.nombre += "." + lex;
            nombreVariable = lex;
        } 
        lex = l.lexico();
        if( lex.equals("arreglo") ) definicionTiposVector();
        if( t.Existe(e.nombre) ) error(l.linea, "semantica", "El identificador <" + nombreVariable + "> ya existe");
        else{
            e.valor = null;
            if( declaracionValida ){ 
                t.Agregar(new TablaSimbolos(e.nombre, e.clase, e.tipo, e.dimension, e.valor));
                code.tablaSimbolos(e);
            }
        }
        e.nombre = t.BajarNivel(e.nombre);
        if( e.nombre.equals("") ) e.nombre = nombrePrograma;
        if( lex.equals(",") ) declaracionMultiplesVariables();
        if( !lex.equals(";") ) error(l.linea, "sintaxis", "Se esperaba < ; > y llegó " + lex);
        lex = l.lexico();
    }
    
    public void declaracionMultiplesVariables(){
        String nombreVariable = "";
        if( !lex.equals(",") ){ 
            error(l.linea, "sintaxis", "Se esperaba < , > y llegó " + lex);
            declaracionValida = false;
        }
        e.dimension = null;
        lex = l.lexico();
        if( !l.token.equals("Identi") ){ 
            error(l.linea, "sintaxis", "Se esperaba un <Identificador> y llegó " + lex);
            declaracionValida = false;
        }
        else{
            e.nombre += "." + lex;
            nombreVariable = lex;
        }
        lex = l.lexico();
        if( lex.equals("arreglo") ) definicionTiposVector();
        if( t.Existe(e.nombre) ) error(l.linea, "semantica", "El identificador <" + nombreVariable + "> ya existe");
        else{
            if( declaracionValida ){ 
                t.Agregar(new TablaSimbolos(e.nombre, e.clase, e.tipo, e.dimension, e.valor));
                code.tablaSimbolos(e);
            }
        }
        e.nombre = t.BajarNivel(e.nombre);
        if( e.nombre.equals("") ) e.nombre = nombrePrograma;
        if( lex.equals(",") ) declaracionMultiplesVariables();
    }
    
    public void declaracionFunciones(){
        String param = "";
        retorno = false;
        int line = l.linea;
        registrar.clear();
        String nombreFuncion = "";
        declaracionValida = true;
        if( !lex.equals("funcion") ){ 
            error(l.linea, "sintaxis", "Se esperaba <funcion> y llegó " + lex);
            declaracionValida = false;
        }
        else e.clase = lex;
        lex = l.lexico();
        if( !tipoDato() ){ 
            error(l.linea, "sintaxis", "Se esperaba un <Tipo de Dato> y llegó " + lex);
            declaracionValida = false;
        }
        else e.tipo = tipoRetorno = tipoVar;
        lex = l.lexico();
        if( !l.token.equals("Identi") ){ 
            error(l.linea, "sintaxis", "Se esperaba un <Identificador> y llegó " + lex);
            declaracionValida = false;
        }
        else{
            registrar.add(lex);
            nombreFuncion = lex;
        }
        lex = l.lexico();
        if( !lex.equals("(") ){ 
            error(l.linea, "sintaxis", "Se esperaba < ( > y llegó " + lex);
            declaracionValida = false;
        }
        lex = l.lexico();
        if( !lex.equals(")") ){
            if( !tipoDato() ){ 
                error(l.linea, "sintaxis", "Se esperaba un <Tipo de Dato> y llegó " + lex);
                declaracionValida = false;
            }
            else registrar.add(tipoVar);
            lex = l.lexico();
            if( !l.token.equals("Identi") ){ 
                error(l.linea, "sintaxis", "Se esperaba un <Identificador> y llegó " + lex);
                declaracionValida = false;
            }
            else{
                param = lex;
                registrar.add(lex);
            }
            lex = l.lexico();
            if( !lex.equals(")") ) declaracionMultiplesParametros();
        }
        if( !lex.equals(")") ){ 
            error(l.linea, "sintaxis", "Se esperaba < ) > y llegó " + lex);
            declaracionValida = false;
        }
        else{
            if( declaracionValida ){ 
                e.nombre = t.registrarFuncion(line, registrar, new TablaSimbolos(nombrePrograma, e.clase, e.tipo, e.dimension, e.valor), this);
            }
        }
        lex = l.lexico();
        while( tipoDato() ){
            if( tipoDato() ) declaracionVariables();
        }
        if( !lex.equals("inicio") )error(l.linea, "sintaxis", "Se esperaba <inicio> y llegó " + lex);
        //e.nombre = t.BajarNivel( t.BajarNivel( e.nombre ) );
        lex = l.lexico();
        if( !lex.equals("fin") ) bloqueInstrucciones();
        if( !retorno ) error(l.linea, "semantica", "No se ha encontrado intruccion <regresa>");
        if( !lex.equals("fin") ) error(l.linea, "sintaxis", "Se esperaba <fin> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("de") )error(l.linea, "sintaxis", "Se esperaba <de> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("funcion") )error(l.linea, "sintaxis", "Se esperaba <funcion> y llegó " + lex);
        lex = l.lexico();
        if( !l.token.equals("Identi") )error(l.linea, "sintaxis", "Se esperaba un <Identificador> y llegó " + lex);
        else{
            if( !nombreFuncion.equals(lex) )error(l.linea, "semantica", "Se esperaba el identificador de funcion <" + nombreFuncion + "> y llegó " + lex);
        }
        lex = l.lexico();
        if( !lex.equals(";") )error(l.linea, "sintaxis", "Se esperaba < ; > y llegó " + lex);
        lex = l.lexico();
        e.nombre = nombrePrograma;
    }
    
    public void declaracionMultiplesParametros(){
        if( !lex.equals(",") ){ 
            error(l.linea, "sintaxis", "Se esperaba < , > y llegó " + lex);
            declaracionValida = false;
        }
        lex = l.lexico();
        if( !tipoDato() ){ 
            error(l.linea, "sintaxis", "Se esperaba un <Tipo de Dato> y llegó " + lex);
            declaracionValida = false;
        }
        else registrar.add(tipoVar);
        lex = l.lexico();
        if( !l.token.equals("Identi") ){ 
            error(l.linea, "sintaxis", "Se esperaba un <Identificador> y llegó " + lex);
            declaracionValida = false;
        }
        else{
            registrar.add(lex);
        }
        lex = l.lexico();
        if( lex.equals(",") ) declaracionMultiplesParametros();
    }
    
    public Stack<String> valorAsignacion(Stack<String> pila){
        pila = expresion(pila, null);
        if( !lex.equals(";") ) error(l.linea, "sintaxis", "Se esperaba < ; > y llegó " + lex);
        lex = l.lexico();
        return pila;
    }
    
    public Stack<String> valorRetorno(Stack<String> pila){
        pila = expresion(pila, null);
        if( !lex.equals(";") ) error(l.linea, "sintaxis", "Se esperaba < ; > y llegó " + lex);
        lex = l.lexico();
        return pila;
    }
    
    public boolean valorConstante(){
        if( !l.token.equals("CteEnt") && !l.token.equals("CteDec") && !l.token.equals("CteAlf") && !l.token.equals("CteLog") ) return false;
        else return true;
    }
    
    public boolean valorConstante(Stack<String> pila){
        if( l.token.equals("CteEnt") ){ 
            pila.push("E");
            code.instrucciones("LIT", lex, "0");
            pilaAux = pila;
            return true;
        }
        if( l.token.equals("CteDec") ){ 
            pila.push("R");
            code.instrucciones("LIT", lex, "0");
            pilaAux = pila;
            return true;
        }
        if( l.token.equals("CteAlf") ){ 
            pila.push("A");
            code.instrucciones("LIT", lex, "0");
            pilaAux = pila;
            return true;
        }
        if( l.token.equals("CteLog") ){ 
            pila.push("L");
            if( lex.equals("true") ) code.instrucciones("LIT", "V", "0");
            else code.instrucciones("LIT", "F", "0");
            pilaAux = pila;
            return true;
        }
        return false;
    }
    
    public boolean identi(Stack<String> pila){
        String nombre, identi;
        TablaSimbolos aux = null;
        if( l.token.equals("Identi") ){
            identi = lex;
            lex = l.lexico();
            if( lex.equals("(") ){
                String etRet = "E" + code.numEt;
                code.numEt++;
                code.instrucciones("LOD", "_" + etRet, "0");
                Vector<String> p = new Vector<String>();
                //parentesisParametros++;
                valorRetornoFuncion(p);
                if( !lex.equals(")")) error(l.linea, "Sintaxis", "Se esperaba un < ) > después de < ( > y llegó " + lex);
                lex = l.lexico();
                nombre = nombrePrograma + "." + identi;
                if(p.isEmpty()) nombre += ".void";
                else{
                    for(int i = 0; i < p.size(); i++){
                        nombre += "." + p.elementAt(i);
                    }
                }
                aux = t.Buscar(nombre, "procedimiento");
                if( aux == null ) {
                    aux = t.Buscar(nombre, "funcion"); 
                    if( aux == null ){
                        pila.push("I");
                        error(l.linea, "Semantica", "El método " + identi + " no existe");
                    }
                    else{
                        pila.push(palResToAlf(aux.tipo));
                        code.instrucciones("CAL", aux.nombre, "0");
                        if( code.bloquePrincipal ) code.registrarEtiqueta(code.principal.size(), etRet);
                        else code.registrarEtiqueta(code.metodos.size(), etRet);
                        code.instrucciones("LOD", aux.nombre, "0");
                    }
                }
                else{
                    pila.push("I");
                    error(l.linea, "semantica", "Los procedimientos no devuelven ningún valor");
                }
                l.idx--;
            }
            else{
                if( !nombrePrograma.equals(e.nombre) ){
                    aux = t.Buscar(e.nombre+"."+identi, "local");
                    if( aux == null && ciclo ){
                        if( buscarVarCiclo(e.nombre+".para."+identi) ) aux = t.Buscar(e.nombre+".para."+identi, "local");
                    }
                    if( aux == null ) aux = t.Buscar(e.nombre+"."+identi, "parametro");
                    if( aux == null ) aux = t.Buscar(nombrePrograma+"."+identi, "global");
                    if( aux == null ) aux = t.Buscar(nombrePrograma+"."+identi, "constante");
                }
                else{
                    aux = t.Buscar(nombrePrograma+"."+identi, "global");
                    if( aux == null && ciclo ) aux = t.Buscar(e.nombre+".para."+identi, "local");
                    if( aux == null ) aux = t.Buscar(nombrePrograma+"."+identi, "constante");
                }
                if( aux == null ){ 
                    pila.push("I");
                    error(l.linea, "semantica", "La variable " + identi + " no existe");
                }
                else pila.push(palResToAlf(aux.tipo));
                if( lex.equals("[")){
                    lex = l.lexico();
                    Stack<String> x = new Stack<String>();
                    x = expresion(x, null);
                    String tipo = alfToPalRes(TiOp.ResolverExpresion(x, this));
                    if( !tipo.equals("entero") ) error(l.linea, "sintaxis", "Se esperaba <Valor Entero> y llegó " + tipo);
                    if( lex.equals(",") ){
                        lex = l.lexico();
                        Stack<String> y = new Stack<String>();
                        y = expresion(y, null);
                        tipo = alfToPalRes(TiOp.ResolverExpresion(y, this));
                        if( !tipo.equals("entero") ) error(l.linea, "sintaxis", "Se esperaba <Valor Entero> y llegó " + tipo);
                        if( aux != null && aux.dimension == null ) error(l.linea, "Semantica", "La variable < " + identi +" > no debe tener dimension");
                        else if( aux != null && aux.dimension.y == 0 ) error(l.linea, "Semantica", "La variable < " + identi +" > es un vector");
                        if( !lex.equals("]")) error(l.linea, "Sintaxis", "Se esperaba < ] > y llegó " + lex);
                        //lex = l.lexico();
                    }
                    else{
                        if( aux != null && aux.dimension == null) error(l.linea, "Semantica", "La variable < " + identi +" > no debe tener dimension");
                        else if(aux != null && aux.dimension.y != 0 ) error(l.linea, "Semantica", "La variable < " + identi +" > es una matriz");
                        if( !lex.equals("]")) error(l.linea, "Sintaxis", "Se esperaba < ] > y llegó " + lex);
                    }
                }
                else{
                    l.idx -= lex.length();
                    if( aux != null && aux.dimension != null){ 
                        if( aux.dimension.y == 0 ) error(l.linea, "Semantica", "La variable < " + identi +" > es un vector, no se puede referenciar de esta manera");
                        else error(l.linea, "Semantica", "La variable < " + identi +" > es una matriz, no se puede referenciar de esta manera");
                    }
                    //else if(aux != null ) code.instrucciones("LOD", aux.nombre, "0");
                }
                if(aux != null)code.instrucciones("LOD", aux.nombre, "0");
            }
            pilaAux = pila;
            return true;
        }
        else return false;
    }
    
    public Vector<String> valorRetornoFuncion(Vector<String> p){
        Stack<String> pila = new Stack<String>();
        lex = l.lexico();
        if( !lex.equals(")") ){
            //pila = parametros(pila, par);
            pila = expresion(pila, null);
            p.add(alfToPalRes(TiOp.ResolverExpresion(pila, this)));
            while( lex.equals(",") ){
                pila.clear();
                lex = l.lexico();
                //pila = parametros(pila, par);
                pila = expresion(pila, null);
                p.add(alfToPalRes(TiOp.ResolverExpresion(pila, this)));
            }
        }
        return p;
    }
    
    public void declaracionProcedimientos(){
        e.tipo = tipoRetorno = "void";
        int line = l.linea;
        registrar.clear();
        String nombreProcedimiento = "";
        declaracionValida = true;
        if( !lex.equals("procedimiento") ){ 
            error(l.linea, "sintaxis", "Se esperaba <procedimiento> y llegó " + lex);
            declaracionValida = false;
        }
        else e.clase = lex;
        lex = l.lexico();
        if( !l.token.equals("Identi") ){ 
            error(l.linea, "sintaxis", "Se esperaba un <Identificador> y llegó " + lex);
            declaracionValida = false;
        }
        else{
            registrar.add(lex);
            nombreProcedimiento = lex;
        }
        lex = l.lexico();
        if( !lex.equals("(") ){ 
            error(l.linea, "sintaxis", "Se esperaba < ( > y llegó " + lex);
            declaracionValida = false;
        }
        lex = l.lexico();
        if( !lex.equals(")") ){
            if( !tipoDato() ){ 
                error(l.linea, "sintaxis", "Se esperaba un <Tipo de Dato> y llegó " + lex);
                declaracionValida = false;
            }
            else registrar.add(tipoVar);
            lex = l.lexico();
            if( !l.token.equals("Identi") ){ 
                error(l.linea, "sintaxis", "Se esperaba un <Identificador> y llegó " + lex);
                declaracionValida = false;
            }
            else registrar.add(lex);
            lex = l.lexico();
            if( !lex.equals(")") ) declaracionMultiplesParametros();
        }
        if( !lex.equals(")") ){ 
            error(l.linea, "sintaxis", "Se esperaba < ) > y llegó " + lex);
            declaracionValida = false;
        }
        else{
            if( declaracionValida ){ 
                e.nombre = t.registrarFuncion(line, registrar, new TablaSimbolos(nombrePrograma, e.clase, e.tipo, e.dimension, e.valor), this);
            }
        }
        lex = l.lexico();
        while( tipoDato() ){
            if( tipoDato() ) declaracionVariables();
        }
        if( !lex.equals("inicio") ) error(l.linea, "sintaxis", "Se esperaba <inicio> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("fin") ) bloqueInstrucciones();
        if( !lex.equals("fin") ) error(l.linea, "sintaxis", "Se esperaba <fin> y llegó " + lex);
        code.instrucciones("OPR", "0", "1");
        code.instrucciones("OPR", "0", "1");
        lex = l.lexico();
        if( !lex.equals("de") ) error(l.linea, "sintaxis", "Se esperaba <de> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("procedimiento") ) error(l.linea, "sintaxis", "Se esperaba <procedimiento> y llegó " + lex);
        lex = l.lexico();
        if( !l.token.equals("Identi") ) error(l.linea, "sintaxis", "Se esperaba un <Identificador> y llegó " + lex);
        else{
            if( !nombreProcedimiento.equals(lex) )error(l.linea, "semantica", "Se esperaba el identificador de funcion <" + nombreProcedimiento + "> y llegó " + lex);
        }
        lex = l.lexico();
        if( !lex.equals(";") ) error(l.linea, "sintaxis", "Se esperaba < ; > y llegó " + lex);
        lex = l.lexico();
        e.nombre = nombrePrograma;
    }
    
    public void bloqueInstrucciones(){
        if( l.token.equals("Identi") ) instruccionIdentificador();
        else if( lex.equals("si") ) instruccionSi();
        else if( lex.equals("para") ) instruccionPara();
        else if( lex.equals("interrumpe") ){
            if(!ciclo) error(l.linea, "Sintaxis", "La intruccion Interrumpe debe estar dentro de un ciclo");
            else{
                code.instrucciones("JMP", "0", interrumpe.peek());
            }
            lex = l.lexico();
            if( !lex.equals(";") ) error(l.linea, "Sintaxis", "Se esperaba un < ; > y llegó " + lex);
            lex = l.lexico();
        }
        else if( lex.equals("lee") ) instruccionLee();
        else if( lex.equals("imprime") || lex.equals("imprimenl") ) instruccionImprime();
        else if( lex.equals("regresa") ){
            lex = l.lexico();
            Stack<String> pila = new Stack<String>();
            if( !nombrePrograma.equals(e.nombre) ){
                if( lex.equals(";") && !tipoRetorno.equals("void") ) error(l.linea, "sintaxis", "Se esperaba un Valor de Retorno de tipo "+ tipoRetorno +" y llegó " + lex);
                else if( !lex.equals(";") ){ 
                    pila = valorRetorno(pila);
                    String valor = alfToPalRes(TiOp.ResolverExpresion(pila, this));
                    if( tipoRetorno.equals("void") ) error(l.linea, "Semantica", "Los procedimietos no tienen valor de retorno y llego " + valor);
                    else if( !tipoRetorno.equals(valor) ) error(l.linea, "Semantica", "Se esperaba un Valor de Retorno de tipo " + tipoRetorno + " y llegó " + valor);
                    else code.instrucciones("STO", "", nombreMetodo);
                } 
                else lex = l.lexico();
                code.instrucciones("OPR", "0", "1");
                code.instrucciones("OPR", "0", "1");
            }
            else{
                error(l.linea, "Semantica", "No puede haber Retornos en el principal");
                if( !lex.equals(";") ) valorRetorno(pila);
                else lex = l.lexico();
            }
            retorno = true;
        }
        else return;
        if( !lex.equals("fin") ){
            bloqueInstrucciones();
        }
    }
    
    public void instruccionLee(){
        if( !libreria ) error(l.linea, "Semantica", "La libreria <Entrada_Salida> no ha sido declarada");
        String nombre = "";
        TablaSimbolos aux = null;
        if( !lex.equals("lee")) error(l.linea, "Sintaxis", "Se esperaba <lee> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("(")) error(l.linea, "Sintaxis", "Se esperaba < ( > y llegó " + lex);
        lex = l.lexico();
        if( !l.token.equals("Identi")) error(l.linea, "Sintaxis", "Se esperaba < ( > y llegó " + lex);
        else{
            nombre = lex;
            if( !e.nombre.equals(nombrePrograma) ){
                aux = t.Buscar(e.nombre + "." + nombre, "local");
                if( aux == null && ciclo ){
                    if( buscarVarCiclo(e.nombre + ".para." + nombre) ) aux = t.Buscar(e.nombre + ".para." + nombre, "local");
                }
                if( aux == null ) aux = t.Buscar(e.nombre + "." + nombre, "parametro");
                if( aux == null ) aux = t.Buscar(nombrePrograma + "." + nombre, "global");
                if( aux == null ){
                    aux = t.Buscar(nombrePrograma + "." + nombre, "constante");
                    if( aux != null ) error(l.linea, "semantica", "El identificador < " + nombre + " > es una constante, no se permiten asignaciones");
                }
            }
            else{
                aux = t.Buscar(nombrePrograma + "." + nombre, "global");
                if( aux == null ){
                    aux = t.Buscar(nombrePrograma + "." + nombre, "constante");
                    if( aux != null ) error(l.linea, "semantica", "El identificador < " + nombre + " > es una constante, no se permiten asignaciones");
                }
            }
            if( aux == null ) error(l.linea, "Semantica", "La variable " + nombre + " no existe");
        }
        lex = l.lexico();
        if( lex.equals("[") ){
            if( aux != null ){
                if( aux.dimension == null ) error(l.linea, "Semantica", "La variable " + nombre + " no tiene esta dimension");
            }
            lex = l.lexico();
            Stack<String> pila = new Stack<String>();
            pila = expresion(pila, null);
            String ti = "indeterminado";
            if( !pila.isEmpty() ){
                ti = alfToPalRes(TiOp.ResolverExpresion(pila, this));
                if( !ti.equals("entero") ) error(l.linea, "Semantica", "Se esperaba un Valor Entero como indice de arreglo y llegó " + ti);
            }
            else{
                error(l.linea, "Semantica", "Se esperaba un Valor Entero como indice de arreglo y llegó " + ti);
            }
            if( lex.equals(",") ){
                lex = l.lexico();
                pila.clear();
                pila = expresion(pila, null);
                ti = "indeterminado";
                if( !pila.isEmpty() ){
                    ti = alfToPalRes(TiOp.ResolverExpresion(pila, this));
                    if( !ti.equals("entero") ) error(l.linea, "Semantica", "Se esperaba un Valor Entero como indice de arreglo y llegó " + ti);
                }
                else{
                    error(l.linea, "Semantica", "Se esperaba un Valor Entero como indice de arreglo y llegó " + ti);
                }
                if(aux != null && aux.dimension == null ) error(l.linea, "Semantica", "La variable < " + nombre +" > no debe tener dimension");
                else if( aux != null && aux.dimension.y == 0 ) error(l.linea, "Semantica", "La variable < " + nombre +" > es un vector");
            }
            else{
                if(aux != null && aux.dimension == null ) error(l.linea, "Semantica", "La variable < " + nombre +" > no debe tener dimension");
                else if( aux != null && aux.dimension.y != 0) error(l.linea, "Semantica", "La variable < " + nombre +" > es una matriz");
            }
            if( !lex.equals("]")) error(l.linea, "Sintaxis", "Se esperaba < ] > y llegó " + lex);
            lex = l.lexico();
        }
        else{
            if(aux != null && aux.dimension != null){
                if( aux.dimension.y == 0 ) error(l.linea, "Semantica", "La variable < " + nombre +" > es un vector, no se puede referenciar de esta manera");
                else error(l.linea, "Semantica", "La variable < " + nombre +" > es una matriz, no se puede referenciar de esta manera");
                //else code.instrucciones("OPR", aux.nombre, "19");
            }
        }
        code.instrucciones("OPR", aux.nombre, "19");
        if( !lex.equals(")") ) error(l.linea, "Sintaxis", "Se esperaba < ) > y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals(";") ) error(l.linea, "Sintaxis", "Se esperaba < ; > y llegó " + lex);
        lex = l.lexico();
    }
    //IMPORTANTE: IMPLEMENTAR NUMERO DE PARENTESIS POR LLAMADA FUNCION(1 PROCEDIMIENTO(2 (534*34) )2 )1
    public void instruccionImprime(){
        String imp = lex;
        Stack<String> pila = new Stack<String>();
        if( !libreria ) error(l.linea, "Semantica", "La libreria <Entrada_Salida> no ha sido declarada");
        lex = l.lexico();
        if( !lex.equals("(") ) error(l.linea, "Sintaxis", "Se esperaba < ( > y llegó " + lex);
        lex = l.lexico();
        if( lex.equals(")") ) error(l.linea, "Sintaxis", "Se esperaba recibir algo para imprimir");
        while( !lex.equals(")") ){
            if( !l.token.equals("CteAlf") && !identi(pila) ) error(l.linea, "Sintaxis", "Se esperaba <Constante Alfabetica> o <Identificador> y llegó " + lex);
            else if( l.token.equals("CteAlf") ) code.instrucciones("LIT", lex, "0");
            code.instrucciones("OPR", "0", "20");
            lex = l.lexico();
            if( !lex.equals(",") ) break;
            else lex = l.lexico();
        }
        if( imp.equals("imprimenl") ){ 
            code.instrucciones("LIT", '"'+" "+'"', "0");
            code.instrucciones("OPR", "0", "21");
        }
        if( !lex.equals(")") ) error(l.linea, "Sintaxis", "Se esperaba < ( > y llegó " + lex);
        //else parentesisParametros--;
        lex = l.lexico();
        if( !lex.equals(";") ) error(l.linea, "Sintaxis", "Se esperaba < ; > y llegó " + lex);
        lex = l.lexico();
    }
    
    public void instruccionIdentificador(){
        String identi, nom;
        identi = nom = lex;
        Stack<String> pila = new Stack<String>();
        TablaSimbolos aux = null;
        lex = l.lexico();
        if( lex.equals(":=") ){
            if( !nombrePrograma.equals(e.nombre) ){
                aux = t.Buscar(e.nombre+"."+identi, "local");
                if( aux == null && ciclo ){
                    if( buscarVarCiclo(e.nombre+".para."+identi) ) aux = t.Buscar(e.nombre+".para."+identi, "local");
                }
                if( aux == null ) aux = t.Buscar(e.nombre+"."+identi, "parametro");
                if( aux == null ) aux = t.Buscar(nombrePrograma+"."+identi, "global");
                if( aux == null ){
                    aux = t.Buscar(nombrePrograma+"."+nom, "constante");
                    if( aux != null ){
                        error(l.linea, "semantica", "El identificador < " + identi + " > es una constante, no se permiten asignaciones");
                    }
                }
            }
            else{
                aux = t.Buscar(nombrePrograma+"."+nom, "global");
                if( aux == null && ciclo ){
                    if( buscarVarCiclo(e.nombre+".para."+identi) ) aux = t.Buscar(e.nombre+".para."+identi, "local");
                }
                if( aux == null ){
                    aux = t.Buscar(nombrePrograma+"."+nom, "constante");
                    if( aux != null ){
                        error(l.linea, "semantica", "El identificador < " + identi + " > es una constante, no se permiten asignaciones");
                    }
                }
            }
            if( aux == null ){
                pila.push("I");
                error(l.linea, "Semantica", "El identificador < " + identi + " > no está declarado");
            }
            else{ 
                if( aux.dimension != null ){ 
                    if( aux.dimension.y == 0 ) error(l.linea, "Semantica", "No se puede referenciar un vector de esta manera");
                    else error(l.linea, "Semantica", "No se puede referenciar una matriz de esta manera");
                }
                pila.push(palResToAlf(aux.tipo));
            }
            pila.push(":=");
            lex = l.lexico();
            pila = valorAsignacion(pila);
            String ti = TiOp.ResolverExpresion(pila, this);
            if( ti.equals("I") && aux != null ) error(l.linea, "Semantica", "Asignacion de tipos no valida, es esperaba tipo "+aux.tipo);
            if( aux != null ) code.instrucciones("STO", "0", aux.nombre);
        }
        else if( lex.equals("(") ){
            String etRet = "E"+code.numEt;
            code.numEt++;
            code.instrucciones("LOD", "_"+etRet, "0");
            Vector<String> p = new Vector<String>();
            llamadaValida = true;
            //parentesisParametros++;
            p = valorRetornoFuncion(p);
            if( !lex.equals(")") ) error(l.linea, "sintaxis", "Se esperaba < ) > y llegó " + lex);
            lex = l.lexico();
            if( !lex.equals(";") ) error(l.linea, "sintaxis", "Se esperaba < ; > y llegó " + lex);
            else{
                if( llamadaValida ){
                    String n = nombrePrograma + "." + identi;
                    for(int i = 0; i < p.size(); i++) n += "." + p.elementAt(i);
                    if( p.isEmpty() ) n += ".void";
                    aux = t.Buscar(n, "procedimiento");
                    if( aux == null ){
                        aux = t.Buscar(n, "funcion");
                        if( aux != null ) error(l.linea, "Semantica", "Las funciones solo pueden ser llamadas desde sentencias de asignacion");
                    }
                    else{
                        code.instrucciones("CAL", aux.nombre, "0");
                        if( code.bloquePrincipal ) code.registrarEtiqueta(code.principal.size(), etRet);
                        else code.registrarEtiqueta(code.metodos.size(), etRet);
                    }
                    if( aux == null ) error(l.linea, "semantica", "El método < " + identi + " > no está declarado");    
                }
            }
            lex = l.lexico();
        }
        else if( lex.equals("[") ){
            boolean dim = false;
            lex = l.lexico();
            pila.clear();
            if( !nombrePrograma.equals(e.nombre) ){
                aux = t.Buscar(e.nombre+"."+identi, "local");
                if( aux == null && ciclo ){
                    if( buscarVarCiclo(e.nombre+".para."+identi) ) aux = t.Buscar(e.nombre+".para."+identi, "local");
                }
                if( aux == null ) aux = t.Buscar(e.nombre+"."+identi, "parametro");
                if( aux == null ) aux = t.Buscar(nombrePrograma+"."+identi, "global");
                if( aux == null ){
                    aux = t.Buscar(nombrePrograma+"."+nom, "constante");
                    if( aux != null ){
                        error(l.linea, "semantica", "El identificador < " + identi + " > es una constante, no se permiten asignaciones");
                    }
                }
            }
            else{
                aux = t.Buscar(nombrePrograma+"."+nom, "global");
                if( aux == null && ciclo ) aux = t.Buscar(e.nombre+".para."+identi, "local");
                if( aux == null ){
                    aux = t.Buscar(nombrePrograma+"."+nom, "constante");
                    if( aux != null ){
                        error(l.linea, "semantica", "El identificador < " + identi + " > es una constante, no se permiten asignaciones");
                    }
                }
            }
            if( aux == null ){
                pila.push("I");
                error(l.linea, "semantica", "El identificador < " + identi + " > no está declarado");
            }
            else{
                pila.push(palResToAlf(aux.tipo));
            }
            Stack<String> x = new Stack<String>();
            x = expresion(x, null);
            String ti = alfToPalRes(TiOp.ResolverExpresion(x, this));
            x.clear();
            if( !ti.equals("entero") ) error(l.linea, "sintaxis", "Se esperaba <Valor Entero Valido> y llegó " + ti);
            //code.instrucciones("STO", "0", "aux.dim1");
            if( lex.equals(",") ){
                lex = l.lexico();
                x = expresion(x, null);
                ti = alfToPalRes(TiOp.ResolverExpresion(x, this));
                x.clear();
                if( !ti.equals("entero") ) error(l.linea, "sintaxis", "Se esperaba <Valor Entero Valido> y llegó " + ti);
                dim = true;
                if( aux != null && aux.dimension == null ) error(l.linea, "Semantica", "La variable < " + identi +" > no debe tener dimension");
                else if( aux != null && aux.dimension.y == 0 ) error(l.linea, "Semantica", "La variable < " + identi +" > es un vector");
                //code.instrucciones("STO", "0", "aux.dim2");
            }
            else{
                if( aux != null && aux.dimension == null ) error(l.linea, "Semantica", "La variable < " + identi +" > no debe tener dimension");
                else if( aux != null && aux.dimension.y != 0 ) error(l.linea, "Semantica", "La variable < " + identi +" > es una matriz");
            }
            if( !lex.equals("]") ) error(l.linea, "sintaxis", "Se esperaba < ] > y llegó " + lex);
            lex = l.lexico();
            if( lex.equals(":=") ){
                pila.push(":=");
                lex = l.lexico();
                pila = valorAsignacion(pila);
                TiOp.ResolverExpresion(pila, this);
                //code.instrucciones("LOD", "aux.dim1", "0");
                //if( dim ) code.instrucciones("LOD", "aux.dim2", "0");
                if( aux != null ) code.instrucciones("STO", "0", aux.nombre);
            }
            else error(l.linea, "Sintaxis", "Se esperaba <Expresion de Asignación> después de identificador");
        }
    }
    
    public void instruccionSi(){
        String et1 = "E"+code.numEt;
        code.numEt++;
        String et2 = "";
        Stack<String> pila = new Stack<String>();
        if( !lex.equals("si") ) error(l.linea, "sintaxis", "Se esperaba <si> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("(") ) error(l.linea, "sintaxis", "Se esperaba < ( > y llegó " + lex);
        lex = l.lexico();
        pila = expresion(pila, null);
        String ti = alfToPalRes(TiOp.ResolverExpresion(pila, this));
        if( !ti.equals("logico") ) error(l.linea, "Semantica", "La condicion del <Si> debe ser de tipo <Logico> y llegó " + ti);
        else code.instrucciones("JMC", "F", "_"+et1);
        if( !lex.equals(")") ) error(l.linea, "sintaxis", "Se esperaba un < ) > después de < ( > ");
        lex = l.lexico();
        if( !lex.equals("hacer") ) error(l.linea, "sintaxis", "Se esperaba <hacer> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("sino") && !lex.equals("fin") ) bloqueInstrucciones();
        if( lex.equals("sino") ){
            et2 = "E"+code.numEt;
            code.numEt++;
            code.instrucciones("JMP", "0", "_"+et2);
            if( code.bloquePrincipal ) code.registrarEtiqueta(code.principal.size(), et1);
            else code.registrarEtiqueta(code.metodos.size(), et1);
        }
        else{
            if( code.bloquePrincipal ) code.registrarEtiqueta(code.principal.size(), et1);
            else code.registrarEtiqueta(code.metodos.size(), et1);
        }
        if( !lex.equals("sino") && !lex.equals("fin") ) error(l.linea, "sintaxis", "Se esperaba <fin> y llegó " + lex);
        else if( lex.equals("sino") ) {
            lex = l.lexico();
            bloqueInstrucciones();
            if( code.bloquePrincipal ) code.registrarEtiqueta(code.principal.size(), et2);
            else code.registrarEtiqueta(code.metodos.size(), et2);
            if( !lex.equals("fin") ) error(l.linea, "sintaxis", "Se esperaba <fin> y llegó " + lex);
        }
        lex = l.lexico();
        if( !lex.equals("de") ) error(l.linea, "sintaxis", "Se esperaba <de> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("si") ) error(l.linea, "sintaxis", "Se esperaba <si> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals(";") ) error(l.linea, "sintaxis", "Se esperaba < ; > y llegó " + lex);
        lex = l.lexico();
    }
    
    public void instruccionPara(){
        boolean mayor = true;
        String et1 = "", et2 = "";
        String nomVar = "", inc = "";
        et1 = "E" + code.numEt;
        code.numEt++;
        et2 = "E" + code.numEt;
        code.numEt++;
        interrumpe.push("_"+et2);
        boolean cicloAnidado = false;
        Stack<String> pila = new Stack<String>();
        String ti;
        if( !lex.equals("para") ) error(l.linea, "sintaxis", "Se esperaba <si> y llegó " + lex);
        lex = l.lexico();
        if( !l.token.equals("Identi") ) error(l.linea, "sintaxis", "Se esperaba una <Variable> y llegó " + lex);
        else{
            nomVar = e.nombre + ".para." + lex;
            if( !t.Existe(nomVar) ){ 
            t.Agregar(new TablaSimbolos(nomVar, "local", "entero", null, null));
            code.tablaSimbolos(new TablaSimbolos(nomVar, "local", "entero", null, null));
            }
        }
        variablesCiclos.push(nomVar);
        lex = l.lexico();
        if( !lex.equals("en") ) error(l.linea, "sintaxis", "Se esperaba <en> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("rango") ) error(l.linea, "sintaxis", "Se esperaba <rango> y llegó " + lex);
        lex = l.lexico();
        pila = expresion(pila, null);
        ti = alfToPalRes(TiOp.ResolverExpresion(pila, this));
        code.instrucciones("STO", "0", nomVar);
        if( code.bloquePrincipal ) code.registrarEtiqueta(code.principal.size(), et1);
        else code.registrarEtiqueta(code.metodos.size(), et1);
        code.instrucciones("LOD", nomVar, "0");
        if( !ti.equals("entero") ) error(l.linea, "sintaxis", "Se esperaba un <Valor Entero Valido> y llegó " + ti);
        pila.clear();
        ti = "";
        if( !lex.equals(".") ) error(l.linea, "sintaxis", "Se esperaba < . > y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals(".") ) error(l.linea, "sintaxis", "Se esperaba < . > y llegó " + lex);
        lex = l.lexico();
        pila = expresion(pila, null);
        ti = alfToPalRes(TiOp.ResolverExpresion(pila, this));
        if( !ti.equals("entero") ) error(l.linea, "sintaxis", "Se esperaba un <Valor Entero Valido> y llegó " + ti);
        pila.clear();
        ti = "";
        if( lex.equals("incrementa") || lex.equals("decrementa") ){
            if( lex.equals("decrementa") ){ 
                code.instrucciones("OPR", "0", "12");
                mayor = false;
            }
            else code.instrucciones("OPR", "0", "11");
            code.instrucciones("JMC", "F", "_"+et2);
            lex = l.lexico();
            if( !l.token.equals("CteEnt") ) error(l.linea, "sintaxis", "Se esperaba un <Constante Entera> y llegó " + lex);
            else inc = lex;
            lex = l.lexico();
        }
        else{
            code.instrucciones("OPR", "0", "11");
            code.instrucciones("JMC", "F", "_"+et2);
            inc = "1";
        }
        
        if(ciclo) cicloAnidado = true;
        else ciclo = true;
        if( !lex.equals("fin") ) bloqueInstrucciones();
        if(!cicloAnidado) ciclo = false;
        variablesCiclos.pop();
        code.instrucciones("LOD", nomVar, "0");
        code.instrucciones("LIT", inc, "0");
        if(mayor) code.instrucciones("OPR", "0", "2");
        else code.instrucciones("OPR", "0", "3");
        code.instrucciones("STO", "0", nomVar);
        code.instrucciones("JMP", "0", "_"+et1);
        if( code.bloquePrincipal ) code.registrarEtiqueta(code.principal.size(), et2);
        else code.registrarEtiqueta(code.metodos.size(), et2);
        interrumpe.pop();
        if( !lex.equals("fin") ) error(l.linea, "sintaxis", "Se esperaba <fin> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("de") ) error(l.linea, "sintaxis", "Se esperaba <de> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals("para") ) error(l.linea, "sintaxis", "Se esperaba <para> y llegó " + lex);
        lex = l.lexico();
        if( !lex.equals(";") ) error(l.linea, "sintaxis", "Se esperaba < ; > y llegó " + lex);
        lex = l.lexico();
    }
    
    public Stack<String> expresion(Stack<String> pila, String anterior){
        if( lex.equals("no") ){
            pila.push(lex);
            lex = l.lexico();
            if( lex.equals("(") ){
                pila.push(lex);
                lex = l.lexico();
                pila = expresion(pila, null);
                if( !lex.equals(")") ) error(l.linea, "Sintaxis", "Se esperaba un < ) > después de < ( > y llegó " + lex);
                else pila.push(lex);
                lex = l.lexico();
                code.instrucciones("OPR", "0", "17");
            }
            else{ 
                error(l.linea, "Sintaxis", "Se esperaba < ( > después de operador <no>");
                lex = l.lexico();
            }
            if( l.token.equals("OpeAri") || l.token.equals("OpeRel") || l.token.equals("OpeLog") ){ 
                pila.push(lex);
                lex = l.lexico();
                if( anterior == null ) pila = expresion(pila, pila.peek());
                else if( jerarquia(anterior) > jerarquia(pila.peek()) ){
                    pila = expresion(pila, pila.peek());
                    code.instrucciones("OPR", "0", opCode(anterior));
                }
                else{
                    code.instrucciones("OPR", "0", opCode(anterior));
                    pila = expresion(pila, pila.peek());
                }
            }
            else if( anterior != null ) code.instrucciones("OPR", "0", opCode(anterior));
        }
        else if( lex.equals("-") ){
            pila.push(lex);
            lex = l.lexico();
            if( lex.equals("(") ){
                pila.push(lex);
                lex = l.lexico();
                pila = expresion(pila, null);
                if( !lex.equals(")") ) error(l.linea, "Sintaxis", "Se esperaba un < ) > después de < ( > y llegó " + lex);
                else pila.push(lex);
            }
            else{
                if( !valorConstante(pila) && !identi(pila) ){ 
                    pila.push("I");
                    error(l.linea, "sintaxis", "Se esperaba un <Valor o Identificador> y llegó " + lex);
                }
                else{ 
                    pila = pilaAux;
                }
            }
            code.instrucciones("OPR", "0", "8");
            lex = l.lexico();
            if( l.token.equals("OpeAri") || l.token.equals("OpeRel") || l.token.equals("OpeLog") ){ 
                pila.push(lex);
                lex = l.lexico();
                if( anterior == null ) pila = expresion(pila, pila.peek());
                else if( jerarquia(anterior) > jerarquia(pila.peek()) ){
                    pila = expresion(pila, pila.peek());
                    code.instrucciones("OPR", "0", opCode(anterior));
                }
                else{
                    code.instrucciones("OPR", "0", opCode(anterior));
                    pila = expresion(pila, pila.peek());
                }
            }
            else if( anterior != null ) code.instrucciones("OPR", "0", opCode(anterior));
        }
        else if( lex.equals("(") ){
            pila.push(lex);
            lex = l.lexico();
            pila = expresion(pila, null);
            if( !lex.equals(")") ) error(l.linea, "Sintaxis", "Se esperaba un < ) > después de < ( > y llegó " + lex);
            else pila.push(lex);
            //if( anterior != null ) code.instrucciones("OPR", "0", opCode(anterior));
            lex = l.lexico();
            if( l.token.equals("OpeAri") || l.token.equals("OpeRel") || l.token.equals("OpeLog") ){
                pila.push(lex);
                lex = l.lexico();
                if( anterior == null ) pila = expresion(pila, pila.peek());
                else if( jerarquia(anterior) > jerarquia(pila.peek()) ){
                    pila = expresion(pila, pila.peek());
                    code.instrucciones("OPR", "0", opCode(anterior));
                }
                else{
                    code.instrucciones("OPR", "0", opCode(anterior));
                    pila = expresion(pila, pila.peek());
                }
            }
            else if( anterior != null ) code.instrucciones("OPR", "0", opCode(anterior));
        }
        else{
            if( !valorConstante(pila) && !identi(pila) ){ 
                pila.push("I");
                error(l.linea, "sintaxis", "Se esperaba un <Valor o Identificador> y llegó " + lex);
            }
            else pila = pilaAux;
            lex = l.lexico();
            if( l.token.equals("OpeAri") || l.token.equals("OpeRel") || l.token.equals("OpeLog") ){ 
                pila.push(lex);
                lex = l.lexico();
                if( anterior == null ) pila = expresion(pila, pila.peek());
                else if( jerarquia(anterior) > jerarquia(pila.peek()) ){
                    pila = expresion(pila, pila.peek());
                    code.instrucciones("OPR", "0", opCode(anterior));
                }
                else{
                    code.instrucciones("OPR", "0", opCode(anterior));
                    pila = expresion(pila, pila.peek());
                }
            }
            else if( anterior != null ) code.instrucciones("OPR", "0", opCode(anterior));
        }
        return pila;
    }
    
    public String buscarVariable(String nombre){
        TablaSimbolos aux;
        aux = t.Buscar(nombrePrograma+"."+nombre, "global");
        if( aux != null ) return aux.tipo;
        if( !e.nombre.equals(nombrePrograma) ){
            aux = t.Buscar(e.nombre+"."+nombre, "local");
            if( aux != null ) return aux.tipo;
            aux = t.Buscar(e.nombre+"."+nombre, "parametro");
            if( aux != null ) return aux.tipo;
        }
        return null;
    }
    
    public String buscarConstante(String nombre){
        TablaSimbolos aux = t.Buscar(nombrePrograma+"."+nombre, "constante");
        if( aux != null ) return aux.tipo;
        return null;
    }
    
    public String palResToAlf(String x){
        if( x.equals("entero") ) return "E";
        if( x.equals("decimal") ) return "R";
        if( x.equals("alfabetico") ) return "A";
        if( x.equals("logico") ) return "L";
        return "I";
    }
    
    public String alfToPalRes(String x){
        if( x.equals("E") ) return "entero";
        if( x.equals("R") ) return "decimal";
        if( x.equals("A") ) return "alfabetico";
        if( x.equals("L") ) return "logico";
        return "indeterminado";
    }

    public boolean valorEnteroConstante() {
        if( l.token.equals("Identi")){
            TablaSimbolos aux = t.Buscar(nombrePrograma + "." + lex, "constante");
            if(aux != null){
                if( !aux.tipo.equals("entero") ) error(l.linea, "Semantica", "Solo se permite el uso de constantes previamente declaradas");
                else{ 
                    valConst = Integer.parseInt(aux.valor);
                    return true;
                }
            }
            else error(l.linea, "Semantica", "Solo se permite el uso de constantes previamente declaradas");
        }
        else if( l.token.equals("CteEnt") ){ 
            valConst = Integer.parseInt(lex);
            return true;
        }
        else if( lex.equals("-") ){
            lex = l.lexico();
            if( l.token.equals("CteEnt") ){ 
                valConst = Integer.parseInt("-"+lex);
                return true;
            }
            else error(l.linea, "Sintaxis", "Se esperaba Valor Entero después de < - >");
        }
        return false;
    }
    
    public int jerarquia(String op){
        if( op.equals("(") || op.equals(")") ) return 0;
        else if( op.equals("^") ) return 1;
        else if( op.equals("*") || op.equals("/") || op.equals("%") ) return 2;
        else if( op.equals("+") || op.equals("-") ) return 3;
        else if( op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=") || op.equals("<>") || op.equals("=") ) return 4;
        else if( op.equals("no") || op.equals("y") || op.equals("o") ) return 5;
        else return 10;
    }
    
    public String opCode(String op){
        if( op.equals("+") ) return "2";
        else if( op.equals("-") ) return "3";
        else if( op.equals("*") ) return "4";
        else if( op.equals("/") ) return "5";
        else if( op.equals("%") ) return "6";
        else if( op.equals("^") ) return "7";
        else if( op.equals("<") ) return "9";
        else if( op.equals(">") ) return "10";
        else if( op.equals("<=") ) return "11";
        else if( op.equals(">=") ) return "12";
        else if( op.equals("<>") ) return "13";
        else if( op.equals("=") ) return "14";
        else if( op.equals("o") ) return "15";
        else if( op.equals("y") ) return "16";
        else if( op.equals("no") ) return "17";
        return null;
    }
    
    public boolean buscarVarCiclo(String variable){
        Stack<String> aux = new Stack<String>();
        boolean existe = false;
        String var = "";
        while(!variablesCiclos.isEmpty()){
            var = variablesCiclos.pop();
            if( variable.equals(var) ) existe = true;
            aux.push(var);
        }
        while(!aux.isEmpty()){
            variablesCiclos.push(aux.pop());
        }
        return existe;
    }
}
