import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
	public static void main(String[] args) {
		Map<String, Integer> hashPalavras = new HashMap<String, Integer>();
		
		long t1 = 0;
		long t2 = 0;
		long t3 = 0;
		
		long tini = System.nanoTime();
		
		try {
			
			String pathname = "/home/deividfg/Downloads/livros/";
			File file = new File(pathname);
			File afile[] = file.listFiles();
			FileReader fr;
			BufferedReader bfr;
			String line;
			
			int x = 0;
			for (int j = afile.length; x < j; x++) {
				
				if(afile[x].getName().contains("txt")){
					fr = new FileReader(pathname+afile[x].getName());
					bfr = new BufferedReader(fr);
					
					line = "";
					
					t1 = System.nanoTime();
		
					while((line=bfr.readLine())!=null){
						line = Normalizer.normalize(line, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
						line = line.toLowerCase().replaceAll("[^a-z]", " ");
						String str[] = line.split(" ");
						for(int i = 0; i < str.length;i++){
							if (!str[i].equals("") && str[i].length() < 30){
								if(hashPalavras.containsKey(str[i])){
									int valor = hashPalavras.get(str[i]);
									valor++;
									hashPalavras.put(str[i], valor);
								}else{
									hashPalavras.put(str[i], 1);
								}
							}
						}
					}
					
					t2 = System.nanoTime();
					
					System.out.println(x + " - " + afile[x].getName()+": "+((t2-t1)/1000000.0/1000.0));
					
					bfr.close();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		LinkedList<ChaveValor> listaPalavras = new LinkedList<>();
		
		for (Iterator iterator = hashPalavras.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			//System.out.println(""+key+" "+hashPalavras.get(key));
			listaPalavras.add(new ChaveValor(key, hashPalavras.get(key)));
		}
		
		Collections.sort(listaPalavras, new Comparator<ChaveValor>() {
			@Override
			public int compare(ChaveValor o1, ChaveValor o2) {
				return o1.valor>o2.valor?-1:(o1.valor<o2.valor?1:0);
			}
		});
		
		try {
			FileWriter fr = new FileWriter("OUTPUT.csv");
			for (Iterator iterator = listaPalavras.iterator(); iterator.hasNext();) {
				ChaveValor chaveValor = (ChaveValor) iterator.next();
				//System.out.println(chaveValor.key+" "+chaveValor.valor);
				fr.write(chaveValor.key+";"+chaveValor.valor+"\n");
			}
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("NPALAVRAS: "+listaPalavras.size());
		System.out.println("Time Processing: "+((System.nanoTime()-tini)/1000000.0/1000.0));
	}
}

class ChaveValor{
	String key;
	int valor;
	
	public ChaveValor(String key, int valor) {
		super();
		this.key = key;
		this.valor = valor;
	}
}
