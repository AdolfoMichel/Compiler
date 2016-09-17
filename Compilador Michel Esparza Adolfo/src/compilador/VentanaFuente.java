
package compilador;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class VentanaFuente extends JFrame implements ActionListener{
    
    JTextPane texto;
    JTextArea linea;
    JTextArea resultado;
    
    Font fuente;
    
    JLabel lblTamFuente = new JLabel("Tamaño");
    JComboBox tamFuente = new JComboBox();
    JLabel lblBold = new JLabel("Negritas");
    JCheckBox bold = new JCheckBox();
    JButton aceptar = new JButton("Aplicar");
    JButton cancelar = new JButton("Cancelar");
    JPanel panelFormatos = new JPanel();
    JPanel panelEstilos = new JPanel();
    JPanel panelBotones = new JPanel();
    
    VentanaFuente(JTextPane a, JTextArea b, JTextArea c, Font fuenteActual){
        super("Fuente");
        
        texto = a;
        linea = b;
        resultado = c;
        
        setSize(400, 200);
        
        setLayout(new BorderLayout());
        panelFormatos.setLayout(new GridLayout(1,2));
        panelFormatos.add(lblTamFuente);
        tamFuente.addItem("12");
        tamFuente.addItem("16");
        tamFuente.addItem("20");
        tamFuente.addItem("24");
        tamFuente.addItem("28");
        tamFuente.addItem("32");
        tamFuente.addItem("36");
        tamFuente.addItem("40");
        panelFormatos.add(tamFuente);
        panelEstilos.setLayout(new GridLayout(1, 2));
        panelEstilos.add(lblBold);
        panelEstilos.add(bold);
        panelBotones.setLayout(new GridLayout(1,2));
        panelBotones.add(aceptar);
        panelBotones.add(cancelar);
        add(panelFormatos, BorderLayout.NORTH);
        add(panelEstilos, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        
        aceptar.addActionListener(this);
        cancelar.addActionListener(this);
        
        for(int i = 12; i <= 40; i+=4){
            if(fuenteActual.getSize() == i){
                tamFuente.setSelectedIndex((i/4)-3);
            }
        }
        
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object evento = ae.getSource();
        if( evento == aceptar ){
            if( bold.isSelected() ){
                fuente = new Font("Fuente", Font.BOLD, Integer.parseInt((String) tamFuente.getSelectedItem()));
            }
            else{
                fuente = new Font("Fuente", Font.LAYOUT_LEFT_TO_RIGHT, Integer.parseInt((String) tamFuente.getSelectedItem()));
            }
            texto.setFont(fuente);
            linea.setFont(fuente);
            resultado.setFont(fuente);
            this.dispose();
        }
        if( evento == cancelar ){
            this.dispose();
        }
    }

}
