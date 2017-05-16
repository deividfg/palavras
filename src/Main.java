import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {
	public static int wpc = 0;
	public static int processados = 0;
	public static final String PATH = "/home/deivid/java/ProjetosGIT/palavras/";
	
	public static void main(String[] args) {
		ConcurrentHashMap<String, ConcurrentHashMap<String, String>> hashPalavras = new ConcurrentHashMap<>();
		long t1 = 0;
		long t2 = 0;
		long t3 = 0;
		
		File folder = new File(PATH);
		File[] listOfFiles = folder.listFiles();
		
		final ConcurrentLinkedQueue<File> listfiles = new ConcurrentLinkedQueue<>();
		
		t1 = System.nanoTime();
		
		for (int j = 0; j < listOfFiles.length; j++) {
		    if (listOfFiles[j].getName().endsWith("txt")){
		        listfiles.add(listOfFiles[j]);
		    }
		}
		
		final int tamanhototal = listfiles.size();
		
		int ntread = 16;
		Thread threads[] = new Thread[ntread];
		
		for(int i = 0; i < ntread;i++){
			threads[i] = new Thread(new Runnable() {
				public void run() {
					long t4 = 0;
					long t5 = 0;
					
					while(listfiles.size()>0) {
						String filename = listfiles.poll().getPath();
						
						t4 = System.nanoTime();
						
						processaArquivo(hashPalavras, filename);
						
						t5 = System.nanoTime();
						System.out.println(""+(processados+1)+"/"+tamanhototal+" Time " + ((t5 - t4) / 1000000.0 / 1000.0) +" WPC "+wpc+" ->" + filename);
						processados++;
					}
				}
			},"T"+i);
		}
		
		for(int i = 0; i < ntread;i++){
			threads[i].start();
		}
		
		for(int i = 0; i < ntread;i++){
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		t2 = System.nanoTime();

		LinkedList<ChaveValor> listaPalavras = new LinkedList<>();

		for (Iterator iterator = hashPalavras.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			System.out.println(""+key+" "+hashPalavras.get(key));
			listaPalavras.add(new ChaveValor(key, hashPalavras.get(key)));
		}

		Collections.sort(listaPalavras, new Comparator<ChaveValor>() {
			@Override
			public int compare(ChaveValor o1, ChaveValor o2) {
				return o1.paginas.compareTo(o2.paginas);
			}
		});

		try {
			FileWriter fr = new FileWriter("OUTPUT.csv");
			for (Iterator iterator = listaPalavras.iterator(); iterator.hasNext();) {
				ChaveValor chaveValor = (ChaveValor) iterator.next();
				System.out.println(chaveValor.key+" "+chaveValor.paginas);
				fr.write(chaveValor.key + ";" + chaveValor.paginas +";"+chaveValor.getStringOut()+"\n");
			}
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		t3 = System.nanoTime();

		System.out.println("NPALAVRAS " + listaPalavras.size());
		System.out.println("Time Processing " + ((t2 - t1) / 1000000.0 / 1000.0));
		System.out.println("Time Sort " + ((t3 - t2) / 1000000.0 / 1000.0));
	}

	private static void processaArquivo(ConcurrentHashMap<String, ConcurrentHashMap<String, String>> hashPalavras, String filename) {
		try {
			FileReader fr = new FileReader(filename);
			BufferedReader bfr = new BufferedReader(fr);

			String line = "";
			
			Object[] lines = bfr.lines().toArray();
			
			for (int x = 0; x < lines.length; x++) {
			    line = (String)lines[x];
			    line = Normalizer.normalize(line, Normalizer.Form.NFD);
                line = line.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                line = line.replaceAll("[^a-zA-Z ]", " ");
                line = line.toLowerCase();
                String str[] = line.split(" ");
                for (int i = 0; i < str.length; i++) {
                    if (str[i].length() > 0 && str[i].length() < 30) {
                        if (hashPalavras.containsKey(str[i])) {
                            ConcurrentHashMap<String, String> hlivros = hashPalavras.get(str[i]);
                            if(hlivros.containsKey(filename)){
                                String valor = hlivros.get(filename);
                                valor = valor + "," + x;
                                hlivros.put(filename, valor);
                            }else{
                                hlivros.put(filename, x+"");
                            }
                        } else {
                            ConcurrentHashMap<String, String> hlivros = new ConcurrentHashMap<>();
                            hlivros.put(filename, x+"");
                            hashPalavras.put(str[i], hlivros);
                        }
                        wpc++;
                    }
                }
            }

			/*while ((line = bfr.readLine()) != null) {
				line = Normalizer.normalize(line, Normalizer.Form.NFD);
				line = line.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
				line = line.replaceAll("[^a-zA-Z ]", " ");
				line = line.toLowerCase();
				String str[] = line.split(" ");
				for (int i = 0; i < str.length; i++) {
					if (str[i].length() > 0 && str[i].length() < 30) {
						if (hashPalavras.containsKey(str[i])) {
							ConcurrentHashMap<String, Integer> hlivros = hashPalavras.get(str[i]);
							if(hlivros.containsKey(filename)){
								int valor = hlivros.get(filename);
								valor++;
								hlivros.put(filename, valor);
							}else{
								hlivros.put(filename, 1);
							}
						} else {
							ConcurrentHashMap<String, Integer> hlivros = new ConcurrentHashMap<>();
							hlivros.put(filename, 1);
							hashPalavras.put(str[i], hlivros);
						}
						wpc++;
					}
				}
			}*/
			bfr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class ChaveValor {
	String key;
	String paginas;
	ConcurrentHashMap<String, String> hash = null;

	public ChaveValor(String key, ConcurrentHashMap<String, String> hash) {
		super();
		this.key = key;
		this.hash = hash;
		
		for (Iterator iterator = hash.values().iterator(); iterator.hasNext();) {
			String v = (String) iterator.next();
			paginas = paginas + "," + v;
		}	
	}
	public ChaveValor(String key, String valor){
		this.key = key;
		this.paginas = valor;
		hash = null;
	}
	
	public String getStringOut(){
		String s = "";
		LinkedList<ChaveValor> listaLivros = new LinkedList<>();

		for (Iterator iterator = hash.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			listaLivros.add(new ChaveValor(key, hash.get(key)));
		}

		Collections.sort(listaLivros, new Comparator<ChaveValor>() {
			@Override
			public int compare(ChaveValor o1, ChaveValor o2) {
				return o1.paginas.compareTo(o2.paginas);
			}
		});
		
		for (Iterator iterator = listaLivros.iterator(); iterator.hasNext();) {
			ChaveValor chaveValor = (ChaveValor) iterator.next();
			s+=""+chaveValor.key.replace("/home/deivid/java/ProjetosGIT/palavras/","")+";"+chaveValor.paginas+";";
		}
		
		return s;
	}
}
