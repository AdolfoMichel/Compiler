package compilador;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;


public class Ventana extends JFrame implements ActionListener {
    
    AnalizadorLexico c = new AnalizadorLexico();
    File archivoAbierto = null;
    int num = 1;
    JMenuBar menu = new JMenuBar();
    JMenu archivo = new JMenu("Archivo");
    JMenu construir = new JMenu("Construir");
    JMenu opciones = new JMenu("Opciones");
    JMenuItem nuevo = new JMenuItem("Nuevo");
    JMenuItem abrir = new JMenuItem("Abrir");
    JMenuItem guardar = new JMenuItem("Guardar");
    JMenuItem guardarComo = new JMenuItem("Guardar Como");
    JMenuItem salir = new JMenuItem("Salir");
    JMenuItem compilar = new JMenuItem("Compilar");
    JMenuItem correr = new JMenuItem("Correr");
    JMenuItem fuente = new JMenuItem("Fuente");
    JTextPane texto;
    JPanel panelResultado = new JPanel();
    JTable tablaErrores = new JTable();
    JTextArea resultado = new JTextArea();
    JTextArea linea = new JTextArea();
    final Font fuenteTexto;
    
    Ventana(){
        super("Compilador");
        setSize(1200, 1000);
        setLocationRelativeTo(null);
        
        Color gray = new Color(200, 200, 200);
        linea.setBackground(gray);
        resultado.setBackground(gray);
        
        archivo.add(nuevo);
        archivo.add(abrir);
        archivo.add(guardar);
        archivo.add(guardarComo);
        archivo.add(salir);
        construir.add(compilar);
        construir.add(correr);
        opciones.add(fuente);
        menu.add(archivo);
        menu.add(construir);
        menu.add(opciones);
        
        nuevo.addActionListener(this);
        abrir.addActionListener(this);
        guardar.addActionListener(this);
        guardarComo.addActionListener(this);
        salir.addActionListener(this);
        compilar.addActionListener(this);
        correr.addActionListener(this);
        fuente.addActionListener(this);

        final StyleContext cont = StyleContext.getDefaultStyleContext();
        final AttributeSet rojo = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.RED);
        final AttributeSet negro = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
        final AttributeSet azul = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.BLUE);
        final AttributeSet verde = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.GREEN);
        final AttributeSet gris = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.GRAY);
        fuenteTexto = new Font("Arial", Font.LAYOUT_LEFT_TO_RIGHT, 28);
        
        DefaultStyledDocument doc = new DefaultStyledDocument() {
            public void insertString (int offset, String str, AttributeSet a) throws BadLocationException {
                super.insertString(offset, str, a);
                c.entrada = getText(0, getLength());
                int L = 0, R = 0;
                c.idx=0;
                c.linea = 1;
                c.token="";
                while( c.idx < c.entrada.length() ) {
                    L = c.idx;
                    String lex = c.lexicoColor();
                    R = c.idx;
                    if (c.token.matches("(\\W)*(OpeRel|OpeAri|OpeAsi|OpeLog)"))
                        setCharacterAttributes(L, R - L, verde, false);
                    else if (c.token.matches("(\\W)*(PalRes|CteLog)"))
                        setCharacterAttributes(L, R - L, azul, false);
                    else if (c.token.matches("(\\W)*(CteAlf|CteDec|CteEnt)"))
                        setCharacterAttributes(L, R - L, rojo, false);
                    else if (c.token.matches("(\\W)*(Coment)"))
                        setCharacterAttributes(L, R - L, gris, false);
                    else
                        setCharacterAttributes(L, R - L, negro, false);
        	}
                if(num < c.linea){
                    num = c.linea;
                    linea.setText("");
                    for(int i=1; i<=num; i++)
                        linea.append(i + "\n");
                }
            }

            public void remove (int offs, int len) throws BadLocationException {
                super.remove(offs, len);
                c.entrada = getText(0, getLength());
                int L = 0, R = 0;
                c.idx = 0;
                c.linea = 1;
                c.token = "";
                while( c.idx < c.entrada.length() ) {
                    L = c.idx;
                    String lex = c.lexicoColor();
                    R = c.idx;
                    if (c.token.matches("(\\W)*(OpeRel|OpeAri|OpeAsi|OpeLog)"))
                        setCharacterAttributes(L, R - L, verde, false);
                    else if (c.token.matches("(\\W)*(PalRes|CteLog)"))
                        setCharacterAttributes(L, R - L, azul, false);
                    else if (c.token.matches("(\\W)*(CteAlf|CteDec|CteEnt)"))
                        setCharacterAttributes(L, R - L, rojo, false);
                    else if (c.token.matches("(\\W)*(Coment)"))
                        setCharacterAttributes(L, R - L, gris, false);
                    else
                        setCharacterAttributes(L, R - L, negro, false);
        	}
                if(num > c.linea){
                    num = c.linea;
                    linea.setText("");
                    for(int i=1; i<=num; i++)
                        linea.append(i + "\n");
                }
            }
        };
        texto = new JTextPane(doc);
        texto.setFont(fuenteTexto);
        linea.setFont(fuenteTexto);
        resultado.setFont(fuenteTexto);
        JPanel panel = new JPanel();
        linea.setEditable(false);
        resultado.setEditable(false);
        linea.setText("1\n");
        resultado.setText("\n\n\n");
        panelResultado.add(resultado);
        panel.setLayout(new BorderLayout());
        panel.add(linea, BorderLayout.WEST);
        panel.add(texto, BorderLayout.CENTER);
        this.add(new JScrollPane(panel), BorderLayout.CENTER);
        this.add(menu, BorderLayout.NORTH);
        this.add(new JScrollPane(panelResultado), BorderLayout.SOUTH);
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
    
    public static void main(String[] args) {
        Ventana x = new Ventana();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object evento = ae.getSource();
        if(evento == nuevo){
            Ventana x = new Ventana();
            this.dispose();
        }
        if(evento == abrir){
            try {
                texto.setText(new Archivos().Abrir(this));
                resultado.setText("\n\n\n");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Ventana.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(evento == guardar){
            new Archivos().Guardar(texto.getText(), this);
        }
        if(evento == guardarComo){
            if(archivoAbierto != null){
                File aux = archivoAbierto;
                archivoAbierto = null;
                if(!new Archivos().Guardar(texto.getText(), this)){
                    archivoAbierto = aux;
                }
            }
            else{
                new Archivos().Guardar(texto.getText(), this);
            }
            
        }
        if(evento == salir){
            this.dispose();
        }
        if(evento == compilar){
            new Archivos().Guardar(texto.getText(), this);
            String errores = new AnalizadorSintactico(archivoAbierto.getAbsolutePath()).parser(texto.getText());
            if(errores != "") 
                resultado.setText(errores);
            else
                resultado.setText("Compilación correcta");
        }
        if(evento == correr){
            new Archivos().Guardar(texto.getText(), this);
            AnalizadorSintactico analizador = new AnalizadorSintactico(archivoAbierto.getAbsolutePath());
            String errores = analizador.parser(texto.getText());
            if(errores != "") 
                resultado.setText(errores);
            else{
                resultado.setText("Compilación correcta");
                String ruta = archivoAbierto.getAbsolutePath();
                String nombre = analizador.nombrePrograma;
                int i;
                for(i = ruta.length()-1; i >= 0; i--){
                    if( ruta.charAt(i) == '/') break;
                }
                String archivo = ruta.substring(0, i)+"/"+nombre;
                archivo = '"' + archivo + '"';
                new Thread(new Consola(archivo)).start();
            }
        }
        if(evento == fuente){
            new VentanaFuente(texto, linea, resultado, texto.getFont());
        }
    }
}
