import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;

public class MainConsulta {

    public static void main(String[] args) throws IOException {
        
        long t1 = 0;
        
        //Lê a palavra
        String palavra = null;
        
        while (palavra == null || palavra.length() < Constantes.MIN_PALAVRA){
            palavra = JOptionPane.showInputDialog("O que você procura?");
            if (palavra.length() < Constantes.MIN_PALAVRA){
                JOptionPane.showMessageDialog(null, "A palavra deve conter ao menos " + Constantes.MIN_PALAVRA + " caracteres!");
            }
        }
        
        t1 = System.nanoTime();
        
        String busca = null;
        try {
            //Lê o arquivo indexado.
            FileReader indexado = new FileReader(Constantes.INDEXADO);
            BufferedReader reader = new BufferedReader(indexado);
            Object[] linhas = reader.lines().toArray();
            
            for (int indiceLinha = 0; indiceLinha < linhas.length; indiceLinha++) {
                String linha = (String)linhas[indiceLinha];
                if (linha.split(";")[0].equalsIgnoreCase(palavra)){
                    busca = linha;
                    break;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //Mostra mensagem
        if (busca == null){
            JOptionPane.showMessageDialog(null, "Texto não encontrado!");
        } else {
            //Captura linhas dos arquivos.
            String[] livros = busca.split(";");
            
            StringBuilder mensagem = new StringBuilder();
            mensagem.append("<html>");
            
            //Começa em 1 pois o primeiro campo é a palavra.
            for (int indiceLivros = 1; indiceLivros < livros.length; indiceLivros++) {
                
                String[] linhas = livros[indiceLivros].split(",");
                String nomeLivro = linhas[0];
                
                FileReader arquivo = new FileReader(Constantes.CAMINHO + nomeLivro + Constantes.EXTENSAO);
                BufferedReader reader = new BufferedReader(arquivo);
                Object[] linhasDoArquivo = reader.lines().toArray();
                
                //Começa em 1 pois o primeiro campo é o nome do livro.
                for (int indiceLinhas = 1; indiceLinhas < linhas.length; indiceLinhas++) {
                    String palavraSublinhada = (String)linhasDoArquivo[Integer.parseInt(linhas[indiceLinhas])];
                    
                    int inicioIndexPalavra = palavraSublinhada.toLowerCase().indexOf(palavra);
                    int fimIndexPalavra = palavraSublinhada.toLowerCase().indexOf(palavra)+palavra.length();
                    
                    palavraSublinhada = palavraSublinhada.substring(0, inicioIndexPalavra) + "<u>" + palavra + "</u>" + palavraSublinhada.substring(fimIndexPalavra, palavraSublinhada.length());
                    mensagem.append("Livro: " + nomeLivro + ", Linha: " + (Integer.parseInt(linhas[indiceLinhas])+1) + ", Texto: " + palavraSublinhada + "<br>");
                }
                reader.close();
            }
            mensagem.append("</html>");
            System.out.println("Tempo de busca (segundos): " + ((System.nanoTime() - t1) / 1000000.0 / 1000.0));
            System.out.println(mensagem);
            JOptionPane.showMessageDialog(null, mensagem);
        }
    }

}
