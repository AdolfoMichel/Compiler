
package compilador;

import java.io.IOException;

/**
 *
 * @author adolfo
 */
public class Consola implements Runnable{
    
    String archivo = "";
    
    Consola(String a){
        archivo = a;
    }

    @Override
    public void run() {
        try {
            Runtime r = Runtime.getRuntime();
            String myScript = "wine ~/inter "+archivo + "; read -n 1 -s";
            String[] cmdArray = {"xterm", "-e", myScript + " ; le_exec"};
            r.exec(cmdArray).waitFor();
        } catch (InterruptedException ex){
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
}
