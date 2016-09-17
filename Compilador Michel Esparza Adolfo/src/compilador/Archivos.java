
package compilador;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;


public class Archivos {
    public String Abrir(Ventana x) throws FileNotFoundException{
        String texto = "";
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(x) == JFileChooser.APPROVE_OPTION) {
            System.out.println("Archivo: " + chooser.getSelectedFile());
            Scanner scanner = new Scanner( chooser.getSelectedFile(), "UTF-8" );
            texto = scanner.useDelimiter("\\A").next();
            scanner.close(); // Put this call in a finally block
            x.archivoAbierto = chooser.getSelectedFile();
        }
        return texto;
    }
    
    public boolean Guardar(String texto, Ventana x){
        JFileChooser chooser = new JFileChooser();
        if(x.archivoAbierto == null){
            if(chooser.showSaveDialog(x) == JFileChooser.APPROVE_OPTION){
                PrintStream out = null;
                try {
                    out = new PrintStream(new FileOutputStream(chooser.getSelectedFile()));
                    out.print(texto);
                    x.archivoAbierto = chooser.getSelectedFile();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Archivos.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                } finally {
                    out.close();
                    return true;
                }
            }
            return false;
        }
        else{
            PrintStream out = null;
            try {
                out = new PrintStream(new FileOutputStream(x.archivoAbierto));
                out.print(texto);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Archivos.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } finally {
                out.close();
                return true;
            }
        }
    }
}
