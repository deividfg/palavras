import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainIndexa {
	public static void main(String[] args) {
		ConcurrentHashMap<String, ConcurrentHashMap<String, String>> palavras = new ConcurrentHashMap<>();
		long t1 = 0;
		long t2 = 0;
		int processados = 0;
		
		File pasta = new File(Constantes.CAMINHO);
		File[] arquivosTemp = pasta.listFiles();
		
		final ConcurrentLinkedQueue<File> arquivos = new ConcurrentLinkedQueue<>();
		
		t1 = System.nanoTime();
		
		//Adicionando somente arquivos com a extensão definida.
		for (int j = 0; j < arquivosTemp.length; j++) {
		    if (arquivosTemp[j].getName().endsWith(Constantes.EXTENSAO)){
		        arquivos.add(arquivosTemp[j]);
		    }
		}
		
		long t4 = 0;
		long t5 = 0;
		
		while(!arquivos.isEmpty()) {
			String nomeArquivo = arquivos.poll().getPath();
			
			t4 = System.nanoTime();
			
			//Realiza o processamento no arquivo.
			processaArquivo(palavras, nomeArquivo);
			
			t5 = System.nanoTime();
			System.out.println("" + (processados+1) + "/" + arquivos.size() + " Tempo: " + ((t5 - t4) / 1000000.0 / 1000.0)+ " Arquivo: " + nomeArquivo);
			processados++;
		}
					
		t2 = System.nanoTime();
		
		//Escreve dados no arquivo de indexação.
		//Formato: "palavra;livro,pagina,pagina,...;livro,pagina,pagina...;...
		//Exemplo: "resume;mobydick,2025,9589;wells-war-189,3838,5586,6040;stoker-dracula-168,1268,9797"
		escreveArquivoIndexacao(palavras);
		
		System.out.println("NPALAVRAS " + palavras.size());
		System.out.println("Time Processing " + ((t2 - t1) / 1000000.0 / 1000.0));
	}

    private static void escreveArquivoIndexacao(ConcurrentHashMap<String, ConcurrentHashMap<String, String>> palavras) {
        try {
			FileWriter fr = new FileWriter(Constantes.INDEXADO);
			for (Iterator iteratorPalavras = palavras.keySet().iterator(); iteratorPalavras.hasNext();) {
			    Object chavePalavra = iteratorPalavras.next();
			    fr.write(chavePalavra+"");
			    
			    for (Iterator iteratorLivros = palavras.get(chavePalavra).keySet().iterator(); iteratorLivros.hasNext();) {
			        Object chaveLivro = iteratorLivros.next();
			        fr.write(";" + chaveLivro + "," + palavras.get(chavePalavra).get(chaveLivro));
                }
			    
				fr.write("\n");
			}
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	private static void processaArquivo(ConcurrentHashMap<String, ConcurrentHashMap<String, String>> palavras, String nomearquivo) {
		try {
			FileReader arquivo = new FileReader(nomearquivo);
			BufferedReader reader = new BufferedReader(arquivo);

			Object[] linhas = reader.lines().toArray();
			
			//Diminui o nome do arquivo pra ocupar menos espaço no arquivo de indexação
			nomearquivo = nomearquivo.replace(Constantes.CAMINHO, "").replaceAll(Constantes.EXTENSAO, "");
			
			for (int indiceLinha = 0; indiceLinha < linhas.length; indiceLinha++) {
			    //Remove caracteres especiais, numeros.
			    String linha = normalizaLinha(linhas, indiceLinha);
                String palavra[] = linha.split(" ");
                for (int indicePalavra = 0; indicePalavra < palavra.length; indicePalavra++) {
                    if (palavra[indicePalavra].length() >= Constantes.MIN_PALAVRA && palavra[indicePalavra].length() < 30) {
                        if (palavras.containsKey(palavra[indicePalavra])) {
                            ConcurrentHashMap<String, String> livros = palavras.get(palavra[indicePalavra]);
                            if(livros.containsKey(nomearquivo)){
                                livros.put(nomearquivo, livros.get(nomearquivo) + "," + indiceLinha);
                            }else{
                                livros.put(nomearquivo, indiceLinha+"");
                            }
                        } else {
                            ConcurrentHashMap<String, String> livros = new ConcurrentHashMap<>();
                            livros.put(nomearquivo, indiceLinha+"");
                            palavras.put(palavra[indicePalavra], livros);
                        }
                    }
                }
            }
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    private static String normalizaLinha(Object[] linhas, int indiceLinha) {
        String linha = (String)linhas[indiceLinha];
        linha = Normalizer.normalize(linha, Normalizer.Form.NFD);
        linha = linha.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        linha = linha.replaceAll("[^a-zA-Z ]", " ");
        linha = linha.toLowerCase();
        return linha;
    }
}