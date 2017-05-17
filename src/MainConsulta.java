import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;

public class MainConsulta {

    public static void main(String[] args) {
        
        //Lê a palavra
        String palavra = JOptionPane.showInputDialog("O que você procura?");
        String mensagem = null;
        try {
            //Lê o arquivo indexado.
            FileReader indexado = new FileReader(Constantes.CAMINHO + Constantes.INDEXADO);
            BufferedReader reader = new BufferedReader(indexado);
            Object[] linhas = reader.lines().toArray();
            
            for (int indiceLinha = 0; indiceLinha < linhas.length; indiceLinha++) {
                String linha = (String)linhas[indiceLinha];
                if (linha.startsWith(palavra)){
                    mensagem = linha;
                    break;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        JOptionPane.showMessageDialog(null, "<html> <u> " + mensagem + "</u></html>");
    }

}
