package compilador;

import java.util.Scanner;

public class AnalizadorLexico {
    
    Scanner leer = new Scanner( System.in );
    final int ERR = -1, ACP = 999;
    String entrada="", token = "";
    int idx = 0, linea=1;
    
    final int matran[][] = {
                //Letra|_   Dígito      OpArit      / 		*           < 		>           =   	.           delim       :           "   
    	/*0*/{	1,          2,		7,          17, 	7,          8,          12,         11,         16,         16,         14,         5   },
    	/*1*/{	1,          1,		ACP,        ACP, 	ACP,        ACP,	ACP,        ACP,	ACP,        ACP,	ACP,        ACP },
    	/*2*/{	ACP,        2,		ACP,        ACP, 	ACP,        ACP,	ACP,        ACP,	3,          ACP,	ACP,        ACP },
    	/*3*/{	ERR,        4,		ERR,        ERR, 	ERR,        ERR,	ERR,        ERR,	ERR,        ERR,	ERR,        ERR },
    	/*4*/{	ACP,        4,		ACP,        ACP, 	ACP,        ACP,	ACP,        ACP,	ACP,        ACP,	ACP,        ACP },
    	/*5*/{	5,          5,		5,          5, 		5,          5,          5,          5,          5,          5,		5,          6   },    		
    	/*6*/{	ACP,        ACP,	ACP,        ACP, 	ACP,        ACP,	ACP,        ACP,	ACP,        ACP,	ACP,        ACP },
    	/*7*/{	ACP,        ACP,	ACP,        ACP, 	ACP,        ACP,	ACP,        ACP,	ACP,        ACP,	ACP,        ACP },
    	/*8*/{	ACP,        ACP,	ACP,        ACP, 	ACP,        ACP,	10,         9,		ACP,        ACP,	ACP,        ACP },
    	/*9*/{	ACP,        ACP,	ACP,        ACP, 	ACP,        ACP,	ACP,        ACP,	ACP,        ACP,	ACP,        ACP },
    	/*10*/{	ACP,        ACP,	ACP,        ACP, 	ACP,        ACP,	ACP,        ACP,	ACP,        ACP,	ACP,        ACP },
    	/*11*/{	ACP,        ACP,	ACP,        ACP, 	ACP,        ACP,	ACP,        ACP,	ACP,        ACP,	ACP,        ACP },
  	/*12*/{	ACP,        ACP,	ACP,        ACP, 	ACP,        ACP,	ACP,        13,		ACP,        ACP,	ACP,        ACP },
    	/*13*/{	ACP,        ACP,	ACP,        ACP, 	ACP,        ACP,	ACP,        ACP,	ACP,        ACP,	ACP,        ACP },
  	/*14*/{	ERR,        ERR,	ERR,        ERR, 	ERR,        ERR,	ERR,        15,		ERR,        ERR,	ERR,        ERR },
    	/*15*/{	ACP,        ACP,	ACP,        ACP, 	ACP,        ACP,	ACP,        ACP,	ACP,        ACP,	ACP,        ACP },
    	/*16*/{	ACP,        ACP,	ACP,        ACP, 	ACP,        ACP,	ACP,        ACP,	ACP,        ACP,	ACP,        ACP },
  	/*17*/{	ACP,        ACP,	ACP,        18, 	19,         ACP,	ACP,        ACP,	ACP,        ACP,	ACP,        ACP },
  	/*18*/{	18,         18,		18,         18, 	18,         18,		18,         18,		18,         18,		18,         18  },
  	/*19*/{	19,         19,		19,         19, 	20,         19,		19,         19,		19,         19,		19,         19  },    		
  	/*20*/{	19,         19,		19,         21, 	20,         19,		19,         19,		19,         19,		19,         19  },    		
    	/*21*/{	ACP,        ACP,	ACP,        ACP, 	ACP,        ACP,	ACP,        ACP,	ACP,        ACP,	ACP,        ACP },
    };
   
    final String palRes[] = {
    	"usando", "la", "biblioteca", "decimal", "entero", "alfabetico", "logico", 
        "constante", "tipo", "arreglo", "inicio", "fin", "de", "programa", "funcion", 
        "regresa", "si", "sino", "hacer", "procedimiento", "para", "en", "incrementa", 
        "decrementa", "rango", "interrumpe", "principal",  "lee", "imprime", "imprimenl"
    };
        
    final String opeLog[] = {
        "no", "y", "o"
    };
        
    final String cteLog[] = {
        "true", "false"
    }; 
        
    public boolean esPalRes( String lex) {
    	for( int i = 0; i < palRes.length; i++ )
            if( palRes[i].equals(lex) ) return true;
    	return false;
    }
    
    public boolean esOpeLog( String lex) {
    	for( int i = 0; i < opeLog.length; i++ )
            if( opeLog[i].equals(lex) ) return true;
    	return false;
    }
    
    public boolean esCteLog( String lex) {
    	for( int i = 0; i < cteLog.length; i++ )
            if( cteLog[i].equals(lex) ) return true;
    	return false;
    }
    
    public  int colCar( char c) {
    	if ( Character.isAlphabetic( c ))  return 0;
    	if ( c == '_') return 0;
    	if( Character.isDigit( c ) ) return 1;
    	if ( c == '+' || c == '-' || c == '%' || c == '^' ) return 2;
    	if ( c == '/') return 3;
    	if ( c == '*') return 4;
    	if ( c == '<') return 5;
    	if ( c == '>') return 6;
    	if ( c == '=') return 7;
    	if ( c == '.') return 8;
    	if ( c == '[' || c == ']' || c == '(' || c == ')' || c == ',' || c == ';') return 9;
    	if ( c == ':') return 10;
    	if ( c == '"') return 11;
    	if( c == ' ' || c == '\t' || c == '\n') return 15;

    	//System.out.println("Simbolo Ilegal " + c);
    	return ERR;
    }
    
    public String lexicoColor() {
    	String lexema = "";
    	int estado = 0, estAnt=0;
    	while ( idx < entrada.length() && estado != ERR && estado != ACP ) {
            char c = entrada.charAt( idx++);
            while ( (c == ' ' || c == '\t' || c == '\n') && estado == 0){
                if( c == '\n' ) linea++;
                if( entrada.length() <= idx+1 ){ 
                    idx++;
                    break;
                }
                c = entrada.charAt( idx++);
            }
            if( estado == 18 && c == '\n') {
                estAnt = 18;
    		estado = ACP;
            }
            int col = colCar( c );
            if( col < 0 && ( estado != 5 && estado != 18 && estado != 19 ) ) estado = ERR;
            if( col < 0 && estAnt == 5 ) estado = 5;
            if( col < 0 && estAnt == 18 ) estado = 18;
            if( col < 0 && estAnt == 19 ) estado = 19;
            if( col < 0 && estado == ERR ) System.out.println("Simbolo Ilegal " + c + " linea " + linea);
            if(col > 14 && ( estado != 5 && estAnt != 18 && estado != 19 ) ) {
                estAnt = estado;
                estado = ACP;
            }
            if( estado != ERR && estado != ACP && ( col < 0 || col >11 ) ){
                estAnt = estado;
                lexema += c;
            }
            if( estado != ERR && estado != ACP && col >=0 && col <= 11 ) {
                estAnt = estado;
    		estado = matran[estado][col];
    		if( estado != ACP && estado != ERR ) lexema += c;
            }
    	}// Fin de While
        if( estado == ACP ) idx--;
        else estAnt = estado;
	token = "NOTOKN";
	switch( estAnt ) {
            case 1: token= "Identi"; 
                if( esPalRes( lexema ) ) token = "PalRes";
                else if( esOpeLog( lexema) ) token = "OpeLog";
                else if( esCteLog( lexema) ) token = "CteLog";
	    break;
            case 2: token= "CteEnt"; 
            break;
            case 4: token= "CteDec"; 
            break;
            case 6: token= "CteAlf"; 
            break;
            case 7: case 17: token= "OpeAri"; 
            break;
            case 8: case 9: case 10: case 11: case 12: case 13: token= "OpeRel"; 
            break;
            case 15: token= "OpeAsi"; 
            break;
            case 16: token= "Delimi"; 
            break;
            default: token="NoTokn";
	}
	if (estAnt == 18 || estAnt == 21 ) {
            lexema ="";
            token = "Coment";
	}
    	return lexema;
    }
    
    public String lexico() {
    	String lexema = "";
    	int estado = 0, estAnt=0;
    	/*if( idx >= entrada.length() ){
            token = "NoTokn";
            return "";
        }*/
    	while ( idx < entrada.length() && estado != ERR && estado != ACP ) {
            char c = entrada.charAt( idx++);
            while ( (c == ' ' || c == '\t' || c == '\n') && estado == 0){
                if( c == '\n' ) linea++;
                if( entrada.length() <= idx+1 ){ 
                    idx++;
                    break;
                }
                c = entrada.charAt( idx++);
            }
            if( estado == 18 && c == '\n') {
                estAnt = 18;
    		estado = ACP;
            }
            int col = colCar( c );
            if( col < 0 && ( estado != 5 && estado != 18 && estado != 19 ) ) estado = ERR;
            if( col < 0 && estAnt == 5 ) estado = 5;
            if( col < 0 && estAnt == 18 ) estado = 18;
            if( col < 0 && estAnt == 19 ) estado = 19;
            if( col < 0 && estado == ERR ) System.out.println("Simbolo Ilegal " + c + " linea " + linea);
            if(col > 14 && ( estado != 5 && estAnt != 18 && estado != 19 ) ) {
                estAnt = estado;
                estado = ACP;
            }
            if( estado != ERR && estado != ACP && ( col < 0 || col >11 ) ){
                estAnt = estado;
                lexema += c;
            }
            if( estado != ERR && estado != ACP && col >=0 && col <= 11 ) {
                estAnt = estado;
    		estado = matran[estado][col];
    		if( estado != ACP && estado != ERR ) lexema += c;
            }
    	}// Fin de While
        if( estado == ACP ) idx--;
        else estAnt = estado;
	token = "NOTOKN";
	switch( estAnt ) {
            case 1: token= "Identi"; 
                if( esPalRes( lexema ) ) token = "PalRes";
                else if( esOpeLog( lexema) ) token = "OpeLog";
                else if( esCteLog( lexema) ) token = "CteLog";
	    break;
            case 2: token= "CteEnt"; 
            break;
            case 4: token= "CteDec"; 
            break;
            case 6: token= "CteAlf"; 
            break;
            case 7: case 17: token= "OpeAri"; 
            break;
            case 8: case 9: case 10: case 11: case 12: case 13: token= "OpeRel"; 
            break;
            case 15: token= "OpeAsi"; 
            break;
            case 16: token= "Delimi"; 
            break;
            default: token="NoTokn";
	}
	if (estAnt == 18 || estAnt == 21 ) {
            lexema ="";
            token = "Coment";
	}
	if( token.equals("Coment") ) return lexico();
    	return lexema;
    }
    
}
