import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

public class Woroedytor {
  
    //USTAWIENIA UZYTKOWNIKA

//Liczba linii w oknie
    private static final int linMax=30; //>=1

//Ekran skacze o tyle linii w g�r�/d� przy wyj�ciu kursora za okno
    private static final int linSkok=15; //>=1, <linMax

//Liczba kolumn w oknie, dalej si� zawija
	private static final int kolMax=80; //>=10

//ile zostaje znaków z lewej przy przejściu do zawiniętego wiersza?
    private static final int kolLeft=6; //def: 6, polecam co najmniej 1;
	
//domyślna nazwa pliku (bez rozszerzenia!)	
	private static final String domNazwaPliku = new String("newfile"); //niepuste

//domyślne rozszerzenie 
	private static final String domRozszerzenie = new String(".txt"); // .* 

//czy pozostawić komunikaty od odpluskwiania?
	private static final boolean czyDebug = false; //true/false 

//kolor tekstu	
	private static final Color textColor = new Color(221,221,221);

//kolor kursora	
	private static final Color kurColor = new Color(0,255,0);

//kolor tla	
	private static final Color backgroundColor = new Color(32,32,32);
 
    private static final int kurSzer = 12;
    private static final int kurWys = 23;
	private static final int fontRozmiar = 20;

//czcionka tekstu
	private static final Font textFont = new Font("Courier New", Font.PLAIN, fontRozmiar); 

//czcionka statusu
	private static final Font statusFont = new Font("Courier New", Font.PLAIN, 12);
	
//czcionka fontu - stała stylu, może być PLAIN, BOLD, ITALIC lub BOLD+ITALIC - rozmiar fontu
    //KONIEC USTAWIEN UZYTKOWNIKA


    private static int linia=0;   //wspolrzedna y kursora wzgledem okna
    private static int kolumna=0;  //wspolrzedna x kursora wzgledem okna
    private static int linOff=0;  //(y) różnica między położeniem kursora 
    private static int kolOff=0;  //(x) względem okna a położeniem w liście "wpisane"
    private static int kolOff2=0; //zmienna pomocnicza, jest o jeden większa od 
    private static ArrayList<String> wpisane=new ArrayList<String>(linMax);
    private static boolean menuWyjscia = false;
    private static boolean edytowany = false;



	static void compileAndRun(String[] nazwaPliku){
	try{	
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd /d "+nazwaPliku[3]+" && javac "+nazwaPliku[1]+" && java "+nazwaPliku[2]); //+" && pause"
		//ProcessBuilder("cmd.exe", "/c", "cd \"C:\\Program Files\\Microsoft SQL Server\" && dir");
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) { break; }
            System.out.println(line);}}
	catch(IOException e) {System.out.println("Kompilacja zakonczona niepowodzeniem!"); }
	}


	
	static String openFile(String nazwaPliku, String[] args) {
		if(args.length == 0) {
            wpisane.add("");
            return "NOWY PLIK";}
		try{
			BufferedReader reader = new BufferedReader(new FileReader(nazwaPliku));
			String line;
			if (czyDebug) System.out.println("Znaleziono plik");
			while ((line = reader.readLine()) != null) wpisane.add(line);
			reader.close();
			return "WCZYTANE";}
		catch (FileNotFoundException e){
			if (czyDebug) System.out.println("Nie znaleziono podanego pliku");
			wpisane.add("");
			return "NIE ZNALEZIONO PLIKU";}
		catch (IOException e){
			if (czyDebug){
      System.out.println(nazwaPliku+ "Wczytanie pliku zakonczone niepowodzeniem: "+e);
			e.printStackTrace();}
			wpisane.add("");
			return "BLAD WCZYTYWANIA PLIKU";}
	}


	static String[] nameFinder(String[] args) {
		
		String[] foundName = new String[4]; 
		//0-nazwa pliku przekazana; 1-nazwa pliku samego; 2-nazwa bez rozszerzenia; 3-sciezka folderu zawierajacego

		if(args.length > 0) {
			foundName[0]=args[0]; 
			File plik = new File(args[0]);
			foundName[1]=plik.getName();
			foundName[2]=foundName[1].replaceFirst("[.][^.]+$", "");//usuwa ostatnia kropke wszystko po niej (rozszerzenie)
			try {
				foundName[3]=plik.getCanonicalPath().replaceFirst("[\\\\][^\\\\]+$", ""); //usuwa wszystko po ostatnim backslashu w sciezce
			 }
			catch (IOException e){
				System.out.println("Krytyczne niepowodzenie! "+args[0]);
				System.exit(1);
			}
		}
		else {
			File folder = new File(".");
			try{ foundName[3]=folder.getCanonicalPath();}
			catch (IOException e) {System.out.println("Nie znaleziono folderu domyslnego! Niektore funkcje moga nie dzialac!");}

			File[] listFiles = folder.listFiles();
			boolean unikatowaNazwa = true;

			for (int i = 0; i < listFiles.length && unikatowaNazwa; i++) {
				if (listFiles[i].isFile()) {
					String fileName = listFiles[i].getName();
					if (fileName.equals(domNazwaPliku+domRozszerzenie)) unikatowaNazwa=false;	}}
			if(unikatowaNazwa) {
				foundName[0]=foundName[1]=domNazwaPliku+domRozszerzenie;
				foundName[2]=domNazwaPliku;}
			else{
				int j=0;
				do{
					unikatowaNazwa = true;
					j++;
					for (int i = 0; i < listFiles.length && unikatowaNazwa; i++) {
							if (listFiles[i].isFile()) {
								String fileName = listFiles[i].getName();
								if (fileName.equals(domNazwaPliku+"("+j+")"+domRozszerzenie)) unikatowaNazwa=false;
					}}}while(!unikatowaNazwa);
				foundName[2]=domNazwaPliku+"("+j+")";
				foundName[0]=foundName[1]=foundName[2]+domRozszerzenie;	}}
		if(czyDebug) System.out.println(Arrays.toString(foundName));
		return foundName;}


public static void main(String[] args) {

	final String[] nazwaPliku = nameFinder(args);
  	JLabel labels[] = new JLabel[linMax];
	JFrame mainWindow = new JFrame("Woroedytor");
	JLayeredPane lp = new JLayeredPane();
    mainWindow.setBounds(200,0,kolMax*kurSzer+25,linMax*kurWys+55);
	//ramka.setContentPane(tekst);
	//ramka.getContentPane().setBackground(backgroundColor);
	mainWindow.setResizable(false);
    mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	mainWindow.add(lp);

	JPanel textPane = new JPanel();
	// ramka.getContentPane().setBackground(Color.RED);
	textPane.setLayout(new BoxLayout(textPane, BoxLayout.Y_AXIS));
	textPane.setBackground(backgroundColor);
	textPane.setSize(kolMax*kurSzer+25,linMax*kurWys+55);
	//ramka.add(kursor);
	
	for(int i=0;i<labels.length;i++){
		labels[i] = new JLabel(" ");
		labels[i].setFont(textFont);
    	labels[i].setForeground(textColor);
    	labels[i].setPreferredSize(new Dimension(kolMax*kurSzer,kurWys));
    	labels[i].setMinimumSize(new Dimension(1,kurWys));
		//labels[i].setLocation(0,kurWys*i);
    	textPane.add(labels[i]);}
	JLabel status = new JLabel("Ln 1, Kol 1");
	status.setFont(statusFont);
	status.setForeground(textColor);
	textPane.add(status);

	status.setText("Ln 1, Kol 1 - "+openFile(nazwaPliku[0],args));
	refreshRows(labels);
	mainWindow.setTitle(nazwaPliku[1] + " - Woroedytor");


	JLabel cursorLetter = new JLabel(" ");
	cursorLetter.setFont(textFont);
	cursorLetter.setForeground(Color.BLACK);
	cursorLetter.setLocation(0,0);
	cursorLetter.setSize(cursorLetter.getPreferredSize());

	@SuppressWarnings("serial")   
	JPanel cursor = new JPanel(){
		@Override      
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			cursorLetter.setLocation(kurSzer*kolumna,kurWys*linia);
			if (kolumna+kolOff2>=wpisane.get(linia+linOff).length()) cursorLetter.setText(" ");
			else cursorLetter.setText(wpisane.get(linia+linOff).substring(kolumna+kolOff2,kolumna+kolOff2+1));
			g2d.setColor(kurColor);
			g2d.fillRect(kurSzer*kolumna,kurWys*linia,kurSzer,kurWys);
			g2d.setColor(textColor);
			g2d.drawRect(kurSzer*kolumna,kurWys*linia,kurSzer,kurWys);
		}};

	cursor.setLayout(null);
	cursor.add(cursorLetter);
	cursor.setSize(kolMax*kurSzer+25,linMax*kurWys+55);
	cursor.setOpaque(false);
	cursor.repaint();
	
	lp.add(textPane,Integer.valueOf(1));
	lp.add(cursor,Integer.valueOf(2));
	lp.setVisible(true);

	mainWindow.addKeyListener(
      new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
        	int keyCode = e.getKeyCode();
            char c = e.getKeyChar();  
            if(czyDebug) System.out.println("Numer znaku: "+(int) c+"\nKod klawisza: "+keyCode);
			if (c == 19) { //Ctrl + S
				if(saveFile(nazwaPliku[0])){
                    mainWindow.setTitle(nazwaPliku[1] + " - Woroedytor");
                    edytowany = false;
				    status.setText("Ln "+(linia+linOff+1)+", Kol "+(kolumna+kolOff2+1)+" - ZAPISANE");}}
            else if (c == 27) enteredEscape(status); //Escape
			else if (menuWyjscia){
            if (c == 84 || c == 116) {
                saveFile(nazwaPliku[0]);
                System.exit(0);}
            else if (c == 78 || c == 110) System.exit(0);}
            else{

		if (c== 0xffff){		
        if (keyCode == KeyEvent.VK_DOWN) wDol(labels);        	
        if (keyCode == KeyEvent.VK_UP) wGore(labels);        	
        if (keyCode == KeyEvent.VK_LEFT) wLewo(labels);        	
		if (keyCode == KeyEvent.VK_RIGHT) wPrawo(labels);
		if (keyCode == 36) enteredHome();
		if (keyCode == 35) enteredEnd();
		if (keyCode == 33) {for (int i=(linia+linMax);i>0;i--) wGoreStub(labels); enteredHome();}
		if (keyCode == 34) enteredPgDn(labels);
		}
		   

        
        else if (c == 10) enteredEnter(labels); //Enter / Ctrl+J            
        else if (c == 127) enteredDelete(labels); //Delete
		else if (c == 8) enteredBackspace(labels); //Backspace / Ctrl+H
		else if (c == 14) compileAndRun(nazwaPliku);
        else if (c>31) { 
            wpisane.set(linia+linOff, wpisane.get(linia+linOff).substring(0,kolumna+kolOff2)+ c +wpisane.get(linia+linOff).substring(kolumna+kolOff2,wpisane.get(linia+linOff).length()));   
            kolumna++;
            edytowany = true; }
            status.setText("Ln "+(linia+linOff+1)+", Kol "+(kolumna+kolOff2+1));
        
        }
        
        if(!(wpisane.get(wpisane.size()-1).equals(""))) wpisane.add(""); //tworzy ostatnią pustą linię
				
		naprawKolumny();	
        if(edytowany) mainWindow.setTitle("* "+nazwaPliku[1] + " - Woroedytor");
        labels[linia].setText(napiszAkt(wpisane.get(linia+linOff)));
        //cursor.setKursor(linia,kolumna);
		cursor.revalidate();
		mainWindow.revalidate();
		mainWindow.repaint();
		if(czyDebug) System.out.println("Kolumna: "+kolumna+" Linia: "+linia);}
      }
    );
    
		mainWindow.setVisible(true);}
		
static void naprawKolumny(){
	while(kolumna>kolMax-2){
		kolumna-=(kolMax-kolLeft-2);
		kolOff+=(kolMax-kolLeft-2);
		if(kolOff==kolMax-kolLeft-2) kolOff++;
		kolOff2=kolOff-1; }
		
	while(kolumna<=kolLeft && kolOff>0){
		kolumna+=(kolMax-kolLeft-2);
		kolOff-=(kolMax-kolLeft-2);
		if(kolOff==1) kolOff--;
		if(kolOff>0) kolOff2=kolOff-1;
		else kolOff2=0;	}}

	

static void wLewo(JLabel[] labels){
    if (kolumna==0) {
		if (wGoreStub(labels)) enteredEnd();}
    else kolumna-=1;}

static void wGore(JLabel[] labels){
    if (wGoreStub(labels)) endIfNecessary();}

static boolean wGoreStub(JLabel[] labels){
	boolean notFirstLine = (linia+linOff>0);
	if (notFirstLine) {
		if(kolOff>0) labels[linia].setText(napiszZero(wpisane.get(linia+linOff)));
        if(linia>0) linia-=1;
        else {
            linia=linSkok-1;
            linOff-=Math.min(linSkok,linOff);
			refreshRows(labels);}}
	return notFirstLine;
}

static void endIfNecessary(){ if(kolumna+kolOff2>wpisane.get(linia+linOff).length()) enteredEnd(); }

static void enteredEnd(){
	kolumna = wpisane.get(linia+linOff).length();
	kolOff = 0; 
	kolOff2 = 0;
}

static void enteredHome(){ kolumna = 0; kolOff = 0; kolOff2 = 0;}

static void wPrawo(JLabel[] labels){
    if (kolumna+kolOff2>=wpisane.get(linia+linOff).length()){
        if (wDolStub(labels)) enteredHome();}
    else kolumna+=1;}       	

static boolean wDolStub(JLabel[] labels){
	boolean isNotLast = (linia+linOff<wpisane.size()-1);
	if(isNotLast) {
		if(kolOff>0) labels[linia].setText(napiszZero(wpisane.get(linia+linOff)));
        if (linia<linMax-1) linia+=1;
        else {
            linia=linMax-linSkok;
            linOff+=linSkok;
            refreshRows(labels);}    
	}
	return isNotLast;
}

static void enteredPgDn(JLabel[] labels){
	enteredHome();
	if(linOff+linMax < wpisane.size()){
		linia = 0;
		linOff+=linMax;
		refreshRows(labels);}
	else for(int i=linia;i<linMax;i++) wDolStub(labels);
}

static void wDol(JLabel[] labels){ 
    if (wDolStub(labels)) endIfNecessary();}  

static boolean saveFile(String nazwaPliku){
	try {
		BufferedWriter writer = new BufferedWriter(new FileWriter(nazwaPliku));
		int dlugosc = wpisane.size()-1;
		for (int i=0; i<dlugosc; i++){
		writer.write(wpisane.get(i),0,wpisane.get(i).length());
		writer.newLine();}
		writer.close();
        if(czyDebug) System.out.println(nazwaPliku+ ": Zapis zako�czono powodzeniem!");
        return true;}
	catch(IOException exception){
        if(czyDebug) System.out.println(nazwaPliku+ ": Zapis nie powi�d� si�: "+exception);
        return false;}
}

static String napiszAkt(String tekst){   
	int dlugosc = tekst.length(); 
	if(kolOff==0) return napiszZero(tekst);
	else {
		if(dlugosc<kolMax+kolOff) return "$"+tekst.substring(kolOff,Math.min(kolOff+kolMax-1,dlugosc));
		else return "$"+tekst.substring(kolOff,Math.min(kolOff+kolMax-2,dlugosc))+"$";		
	}}

static String napiszZero(String tekst){   
	int dlugosc = tekst.length(); 
	if(dlugosc==0) return " ";
	else if(dlugosc<=kolMax) return tekst;
	else return tekst.substring(0,kolMax-1)+"$"; 
}


static void enteredEscape(JLabel status){
    if(!edytowany) System.exit(0);
    menuWyjscia ^= true;
    if (menuWyjscia) status.setText("Ln "+(linia+linOff+1)+", Kol "+(kolumna+kolOff2+1)+" - ZAPISAC PRZED WYJSCIEM? (T-Tak/N-Nie/Esc-Anuluj)");
    else status.setText("Ln "+(linia+linOff+1)+", Kol "+(kolumna+kolOff2+1)+" - ANULOWANO");
    System.out.println(menuWyjscia);
}

// static void enteredBackspace(JLabel[] labels){
//     edytowany = true;
//   	if(kolumna+kolOff2==0){
// 	if (linia==0 && linOff==0);
// 	else {
// 		if(linia==0 && linOff>=linSkok){
//     		linOff-=linSkok;
//         	linia=linSkok-1;}
//         else linia-=1;
//         kolumna=wpisane.get(linia+linOff).length();
//         wpisane.set(linia+linOff,wpisane.get(linia+linOff)+wpisane.get(linia+linOff+1));
//         wpisane.remove(linia+linOff+1);
//         refreshRows(labels);}}
//     else if(kolumna+kolOff2==wpisane.get(linia+linOff).length()) {
//         wpisane.set(linia+linOff, wpisane.get(linia+linOff).substring(0,wpisane.get(linia+linOff).length()-1));
//         kolumna-=1;}	
//     else {
//         wpisane.set(linia+linOff, wpisane.get(linia+linOff).substring(0,kolumna+kolOff2-1)+wpisane.get(linia+linOff).substring(kolumna+kolOff2,wpisane.get(linia+linOff).length()));
//         kolumna-=1;}}

static void removeCharStub(JLabel[] labels, boolean lastCharInLine){
	edytowany = true;
	if(lastCharInLine){
		wpisane.set(linia+linOff,wpisane.get(linia+linOff)+wpisane.get(linia+linOff+1));
		wpisane.remove(linia+linOff+1);
		refreshRows(labels);}
	else wpisane.set(linia+linOff, wpisane.get(linia+linOff).substring(0,kolumna+kolOff2)+wpisane.get(linia+linOff).substring(kolumna+kolOff2+1,wpisane.get(linia+linOff).length()));}

static void enteredDelete(JLabel[] labels){
	if (kolumna+kolOff2==wpisane.get(linia+linOff).length()) {
		if (linia+linOff==wpisane.size()-1);
		else removeCharStub(labels, true);}
	else removeCharStub(labels, false);}

static void enteredBackspace(JLabel[] labels){
	if(kolumna+kolOff2==0){
		if (linia+linOff==0);
		else {wLewo(labels); removeCharStub(labels, true);}}
	else {wLewo(labels); removeCharStub(labels, false);}}

// static void enteredDelete(JLabel[] labels){
// 	edytowany = true;
// 	if(kolumna+kolOff2==wpisane.get(linia+linOff).length()){
// 		if (linia+linOff==wpisane.size()-1);
// 		else {
// 			wpisane.set(linia+linOff,wpisane.get(linia+linOff)+wpisane.get(linia+linOff+1));
// 			wpisane.remove(linia+linOff+1);
// 			refreshRows(labels);}}
// 	else {
// 	wpisane.set(linia+linOff, wpisane.get(linia+linOff).substring(0,kolumna+kolOff2)+wpisane.get(linia+linOff).substring(kolumna+kolOff2+1,wpisane.get(linia+linOff).length()));}}

static void enteredEnter(JLabel[] labels){
    edytowany = true;
	wpisane.add(linia+linOff+1, wpisane.get(linia+linOff).substring(kolumna+kolOff2,wpisane.get(linia+linOff).length()));
	wpisane.set(linia+linOff, wpisane.get(linia+linOff).substring(0,kolumna+kolOff2));
	if(linia==linMax-1) {
		linia=linMax-linSkok;
		linOff+=linSkok;}
	else linia+=1;
	kolumna=0;
	kolOff=0;
	kolOff2=0;
	refreshRows(labels);		
}

static void refreshRows(JLabel[] labels){
	for(int i=0;i<linMax;i++){
		if (i+linOff<wpisane.size()){
			if(i==linia) labels[i].setText(napiszAkt(wpisane.get(i+linOff)));
			else labels[i].setText(napiszZero(wpisane.get(i+linOff)));}
		else labels[i].setText(" ");}}}  
				