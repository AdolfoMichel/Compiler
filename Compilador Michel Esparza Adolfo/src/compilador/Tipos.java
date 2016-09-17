
package compilador;

import java.util.Stack;


public class Tipos {

    final String OperacionTipo[] = { "E:=E", "A:=A", "R:=R", "L:=L", "E:=R",
                                     "E+E", "E+R", "R+E", "R+R", "A+A",
                                     "E-E", "E-R", "R-E", "R-R",
                                     "E*E", "E*R", "R*E", "R*R",
                                     "E/E", "E/R", "R/E", "R/R",
                                     "E%E", "-E", "-R", 
                                     "E^E", "R^E",
                                     "LyL", "LoL", "noL",
                                     "E>E", "R>E", "E>R", "R>R",
                                     "E<E", "R<E", "E<R", "R<R",
                                     "E>=E", "R>=E", "E>=R", "R>=R",
                                     "E<=E", "R<=E", "E<=R", "R<=R",
                                     "E<>E", "R<>E", "E<>R", "R<>R", "A<>A",
                                     "E=E", "R=E", "E=R", "R=R", "A=A"
    };
    final String ResultadoTipo[] = { "E",  "A",  "R",  "L",  "R",
                                     "E", "R", "R", "R", "A",
                                     "E", "R", "R", "R",
                                     "E", "R", "R", "R",
                                     "R", "R", "R", "R",
                                     "E", "E", "R",
                                     "E", "R", 
                                     "L", "L", "L",
                                     "L", "L", "L", "L",
                                     "L", "L", "L", "L",
                                     "L", "L", "L", "L",
                                     "L", "L", "L", "L",
                                     "L", "L", "L", "L", "L",
                                     "L", "L", "L", "L", "L"
    };    

    public boolean operadorLogico(String op){
        if( op.equals("y") ||  op.equals("o") ||  op.equals("no") ) return true;
        return false;
    }
    
    public boolean operadorRelacional(String op){
        if( op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=") || op.equals("<>") || op.equals("=") ) return true;
        return false;
    }
    
    public boolean operadorAritmetico(String op){
        if( op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/") || op.equals("%") || op.equals("^") ) return true;
        return false;
    }
    
    public boolean operador(String op){
        if( operadorRelacional(op) || operadorLogico(op) || operadorAritmetico(op) || op.equals(":=") ) return true;
        return false;
    }
    
    public Stack<String> voltear(Stack<String> pila){
        Stack<String> aux = new Stack<String>();
        while(pila.size()>0){
            aux.push(pila.pop());
        }
        return aux;
    }
    
    public String ResolverExpresion(Stack<String> pila, AnalizadorSintactico r){
        String retorno = "I";
        Stack<String> aux = new Stack<String>();
        String op = "", exp = "";
        int parentesis = 0, cont = 0, i;
        if(!pila.isEmpty()){
            while( pila.size() > 0 ){
                if( pila.size() == 1 && exp.equals("") ) return pila.pop();
                parentesis = 0;
                if(!pila.isEmpty()) op = pila.pop();
                else return "I";
                if( op.equals(")") ){
                    parentesis++;
                    while(parentesis>0){
                        if(!pila.isEmpty()) op = pila.pop();
                        else return "I";
                        if( op.equals(")") ) parentesis++;
                        if( op.equals("(") ) parentesis--;
                        if( parentesis>0 ) aux.push(op);
                    }
                    pila.push(ResolverExpresion(voltear(aux), r));
                    aux.clear();
                }
                else if( op.equals("(") ){
                    parentesis++;
                    while(parentesis>0){
                        if(!pila.isEmpty()) op = pila.pop();
                        else return "I";
                        if( op.equals("(") ) parentesis++;
                        if( op.equals(")") ) parentesis--;
                        if( parentesis>0 ) aux.push(op);
                    }
                    pila.push(ResolverExpresion(voltear(aux), r));
                    aux.clear();
                }
                else if( operadorLogico(op) && !op.equals("no") ){
                    cont++;
                    exp += op;
                    while( pila.size() > 0){
                        if(!pila.isEmpty()) op = pila.pop();
                        else return "I";
                        if( !operadorLogico(op) && !op.equals(":=") ) aux.push(op);
                        else{
                            pila.push(op);
                            break;
                        }
                    }
                    pila.push(ResolverExpresion(voltear(aux), r));
                    aux.clear();
                }
                else if( operadorRelacional(op) ){
                    cont++;
                    exp += op;
                    while( pila.size() > 0){
                        if(!pila.isEmpty()) op = pila.pop();
                        else return "I";
                        if( !operadorRelacional(op) && !operadorLogico(op) && !op.equals(":=") ) aux.push(op);
                        else{
                            pila.push(op);
                            break;
                        }
                    }
                    pila.push(ResolverExpresion(voltear(aux), r));
                    aux.clear();
                }
                else{
                    cont++;
                    if( (op.equals("no")) || (op.equals("-") && pila.size()==0) || (op.equals("-") && (operador(pila.lastElement())) )){ 
                        cont++;
                        exp = op + exp;
                    }
                    else exp += op;
                    if( cont >= 3 ){

                        for(i = 0; i < OperacionTipo.length; i++){
                            if(exp.equals(OperacionTipo[i])) break;
                        }
                        if( i < OperacionTipo.length ) pila.push(ResultadoTipo[i]);
                        else{ 
                            r.error(r.l.linea, "semantica", "La operación<"+ exp +"> no es valida");
                            pila.push("I");
                        }
                        cont = 0;
                        exp = "";
                        if( pila.size()<=1 ) break;
                    }
                }
            }
            if(!pila.isEmpty()) retorno = pila.pop();
            else retorno = "I";
        }
        return retorno;
    }
}
