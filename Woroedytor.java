import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.Math.*;
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
	
//domyślna nazwa pliku (bez rozszerzenia!)	
	private static final String domNazwaPliku = new String("newfile"); //niepuste

//domyślne rozszerzenie 
	private static final String domRozszerzenie = new String(".txt"); // .* 

//czy pozostawić komunikaty od odpluskwiania?
	private static final boolean czyDebug = false; //true/false 
	
	private static final Color textColor = new Color(221,221,221);
	private static final Color kurColor = new Color(0,255,0);
	private static final Color backgroundColor = new Color(32,32,32);
    
    private static final int kurSzer = 12;
    private static final int kurWys = 23;
    private static final int fontRozmiar = 20;

    //KONIEC USTAWIEN UZYTKOWNIKA


    private static int linia=0;   //wspolrzedna y kursora wzgledem okna
    private static int kolumna=0;  //wspolrzedna x kursora wzgledem okna
    private static int linOff=0;  //(y) różnica między położeniem kursora 
    private static int kolOff=0;  //(x) względem okna a położeniem w liście "wpisane"
    private static int kolOff2=0; //zmienna pomocnicza, jest o jeden większa od 
    private static ArrayList<String> wpisane=new ArrayList<String>(linMax);
    static final Kursor kursor = new Kursor(0,0,kurSzer,kurWys,kurColor,kurColor);
    private static boolean menuWyjscia = false;
    private static boolean edytowany = false;

	
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

	static String nameFinder(String[] args){
		if(args.length > 0) return args[0];
		else {
			String folderName = "."; // Give your folderName
			File[] listFiles = new File(folderName).listFiles();
			boolean unikatowaNazwa = true;

			for (int i = 0; i < listFiles.length && unikatowaNazwa; i++) {
				if (listFiles[i].isFile()) {
					String fileName = listFiles[i].getName();
					if (fileName.equals(domNazwaPliku+domRozszerzenie)) unikatowaNazwa=false;	}}
			if(unikatowaNazwa) return domNazwaPliku+domRozszerzenie;
				int j=0;

				do{
					unikatowaNazwa = true;
					j++;
					for (int i = 0; i < listFiles.length && unikatowaNazwa; i++) {
							if (listFiles[i].isFile()) {
								String fileName = listFiles[i].getName();
								if (fileName.equals(domNazwaPliku+"("+j+")"+domRozszerzenie)) unikatowaNazwa=false;
					}}}while(!unikatowaNazwa);
				return domNazwaPliku+"("+j+")"+domRozszerzenie;}}


public static void main(String[] args) {
		// String defaultCharacterEncoding = System.getProperty("file.encoding");
		// System.out.println("defaultCharacterEncoding by property: " + defaultCharacterEncoding);
		// //System.out.println("defaultCharacterEncoding by code: " + getDefaultCharEncoding());
		// System.out.println("defaultCharacterEncoding by charSet: " + Charset.defaultCharset());
		// System.setProperty("file.encoding", "UTF-8");
		// System.out.println("defaultCharacterEncoding by property: "+ System.getProperty("file.encoding"));
		// System.out.println("defaultCharacterEncoding by charSet: " + Charset.defaultCharset());
		
	String nazwaPliku = new String(nameFinder(args));
  	JLabel labels[] = new JLabel[linMax];
    JFrame ramka = new JFrame("Woroedytor");
	ramka.setContentPane(kursor);
	ramka.getContentPane().setBackground(backgroundColor);
	// ramka.getContentPane().setBackground(Color.RED);
    ramka.setLayout(new BoxLayout(ramka.getContentPane(), BoxLayout.Y_AXIS));
    ramka.setBounds(200,0,kolMax*kurSzer+25,linMax*kurWys+55);
	ramka.setResizable(false);
	
    ramka.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	for(int i=0;i<labels.length;i++){
		labels[i] = new JLabel(" ");
		labels[i].setFont(new Font("Courier New", Font.PLAIN, fontRozmiar));
    	labels[i].setForeground(textColor);
    	labels[i].setPreferredSize(new Dimension(kolMax*kurSzer,kurWys));
    	labels[i].setMinimumSize(new Dimension(1,kurWys));
    	ramka.add(labels[i]);}
	JLabel status = new JLabel("Ln 1, Kol 1");
	status.setFont(new Font("Courier New", Font.PLAIN, 12));
	status.setForeground(textColor);

	ramka.add(status);
	
	status.setText("Ln 1, Kol 1 - "+openFile(nazwaPliku,args));
	refreshRows(labels);
	ramka.setTitle(nazwaPliku + " - Woroedytor");

		
	ramka.addKeyListener(
      new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
        	int keyCode = e.getKeyCode();
            char c = e.getKeyChar();  
            if(czyDebug) System.out.println("Numer znaku: "+(int) c+"\nKod klawisza: "+keyCode);
			if (c == 19) { //Ctrl + S
				if(saveFile(nazwaPliku)){
                    ramka.setTitle(nazwaPliku + " - Woroedytor");
                    edytowany = false;
				    status.setText("Ln "+(linia+linOff+1)+", Kol "+(kolumna+kolOff2+1)+" - ZAPISANE");}}
            else if (c == 27) enteredEscape(status); //Escape
			else if (menuWyjscia){
            if (c == 84 || c == 116) {
                saveFile(nazwaPliku);
                System.exit(0);}
            else if (c == 78 || c == 110) System.exit(0);}
            else{

		if (c== 0xffff){		
        if (keyCode == KeyEvent.VK_DOWN) wDol(labels);        	
        if (keyCode == KeyEvent.VK_UP) wGore(labels);        	
        if (keyCode == KeyEvent.VK_LEFT) wLewo(labels);        	
        if (keyCode == KeyEvent.VK_RIGHT) wPrawo(labels); }       	
        
        else if (c == 10) enteredEnter(labels); //Enter / Ctrl+J
            
        else if (c == 127) enteredDelete(labels); //Delete
		else if (c == 8) enteredBackspace(labels); //Backspace / Ctrl+H
        else if (c != 0xffff && c>31) { 
            wpisane.set(linia+linOff, wpisane.get(linia+linOff).substring(0,kolumna+kolOff2)+ c +wpisane.get(linia+linOff).substring(kolumna+kolOff2,wpisane.get(linia+linOff).length()));   
            kolumna++;
            edytowany = true; }
            status.setText("Ln "+(linia+linOff+1)+", Kol "+(kolumna+kolOff2+1));
        
        }
        
        if(!(wpisane.get(wpisane.size()-1).equals(""))) wpisane.add(""); //tworzy ostatnią pustą linię
				
		naprawKolumny();	
        if(edytowany) ramka.setTitle("* "+nazwaPliku + " - Woroedytor");
        labels[linia].setText(napiszAkt(wpisane.get(linia+linOff)));
        kursor.setKursor(linia,kolumna);
		kursor.revalidate();
		ramka.revalidate();
		ramka.repaint();
		if(czyDebug) System.out.println("Kolumna: "+kolumna+" Linia: "+linia);}
      }
    );
    
		ramka.setVisible(true);}
		
static void naprawKolumny(){
	while(kolumna>kolMax-2){
		kolumna-=(kolMax-8);
		kolOff+=(kolMax-8);
		if(kolOff==kolMax-8) kolOff++;
		kolOff2=kolOff-1;	}
		
	while(kolumna<7 && kolOff>0){
		kolumna+=(kolMax-8);
		kolOff-=(kolMax-8);
		if(kolOff==1) kolOff--;
		if(kolOff>0) kolOff2=kolOff-1;
		else kolOff2=0;	}}

static void wPrawo(JLabel[] labels){
    if (kolumna+kolOff2>=wpisane.get(linia+linOff).length()){
        if (linia+linOff<wpisane.size()-1){
            if(kolOff>0) labels[linia].setText(napiszZero(wpisane.get(linia+linOff)));
            if (linia<linMax-1) linia+=1;
            else {
                linia=linMax-linSkok;
                linOff+=linSkok;
                refreshRows(labels);}
            kolumna=0;
            kolOff=0;
            kolOff2=0;}}
    else kolumna+=1;}	

static void wLewo(JLabel[] labels){
    if (kolumna==0){
        if (linia+linOff>0) {
            if(kolOff>0) labels[linia].setText(napiszZero(wpisane.get(linia+linOff)));
            if(linia>0) linia-=1;
            else {
                linia=linSkok-1;
                linOff-=linSkok;
                refreshRows(labels);}
            kolumna=wpisane.get(linia+linOff).length();}}
    else kolumna-=1;}


static void wGore(JLabel[] labels){
    if (linia+linOff>0) {
        if(kolOff>0) labels[linia].setText(napiszZero(wpisane.get(linia+linOff)));
        if(linia>0) linia-=1;
        else {
            linia=linSkok-1;
            linOff-=linSkok;
            refreshRows(labels);}
    if (kolumna+kolOff2>wpisane.get(linia+linOff).length()) {
                kolumna=wpisane.get(linia+linOff).length();
                kolOff=0;
                kolOff2=0;}}}

static void wDol(JLabel[] labels){ 
    if (linia+linOff<wpisane.size()-1){
        if(kolOff>0) labels[linia].setText(napiszZero(wpisane.get(linia+linOff)));
        if (linia<linMax-1) linia+=1;
        else {
            linia=linMax-linSkok;
            linOff+=linSkok;
            refreshRows(labels);}      
        if (kolumna+kolOff2>wpisane.get(linia+linOff).length()) {
            kolumna=wpisane.get(linia+linOff).length();
            kolOff=0;
            kolOff2=0;}}}         	

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
	if(dlugosc==0) return " ";
	if(kolOff==0){
		if(dlugosc<=kolMax) return tekst;
		else return tekst.substring(0,kolMax-1)+"$"; 
		}
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

static void enteredBackspace(JLabel[] labels){
            edytowany = true;
        	if(kolumna+kolOff2==0){
        		if (linia==0 && linOff==0);
        		else {
        			if(linia==0 && linOff>=linSkok){
        				linOff-=linSkok;
        				linia=linSkok-1;}
        			else linia-=1;
        			kolumna=wpisane.get(linia+linOff).length();
        			wpisane.set(linia+linOff,wpisane.get(linia+linOff)+wpisane.get(linia+linOff+1));
        			wpisane.remove(linia+linOff+1);
        			refreshRows(labels);
        			}}
        	else if(kolumna+kolOff2==wpisane.get(linia+linOff).length()) {
        		wpisane.set(linia+linOff, wpisane.get(linia+linOff).substring(0,wpisane.get(linia+linOff).length()-1));
        		kolumna-=1;
        		}	
        	else {
        	wpisane.set(linia+linOff, wpisane.get(linia+linOff).substring(0,kolumna+kolOff2-1)+wpisane.get(linia+linOff).substring(kolumna+kolOff2,wpisane.get(linia+linOff).length()));
        	kolumna-=1;}}


static void enteredDelete(JLabel[] labels){
            edytowany = true;
        	if(kolumna+kolOff2==wpisane.get(linia+linOff).length()){
        		if (linia+linOff==wpisane.size() || wpisane.size()==1);
        		else {
        			wpisane.set(linia+linOff,wpisane.get(linia+linOff)+wpisane.get(linia+linOff+1));
        			wpisane.remove(linia+linOff+1);
        			refreshRows(labels);
        			}}

        	else {
        	wpisane.set(linia+linOff, wpisane.get(linia+linOff).substring(0,kolumna+kolOff2)+wpisane.get(linia+linOff).substring(kolumna+kolOff2+1,wpisane.get(linia+linOff).length()));}}

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
         else labels[i].setText(" ");}
	}}

  @SuppressWarnings("serial")
  class Kursor extends JPanel{
  	private int linia=0;
      private int kolumna=0;
      private int kurSzer=0;
	  private int kurWys=0;
	  private Color textColor;
	  private Color kurColor;
  	
  	Kursor(int linia, int kolumna, int kurSzer, int kurWys, Color textColor, Color kurColor){
  		this.linia=linia;
        this.kolumna=kolumna;
        this.kurSzer=kurSzer;
		this.kurWys=kurWys;
		this.textColor = textColor;
		this.kurColor = kurColor;}
  		
  	public void setKursor(int l, int k){
  		this.linia=l;
  		this.kolumna=k;}
    

/*Wersja dla kursywy
          @Override      
          public void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
              g2d.setColor(Color.green);
              g2d.fillRect(3+kurSzer*kolumna,kurWys*linia,kurSzer,kurWys);
                g2d.setColor(Color.black);
                g2d.drawRect(3+kurSzer*kolumna,kurWys*linia,kurSzer,kurWys);}*/      
          

    @Override      
  	public void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
  		g2d.setColor(kurColor);
  		g2d.fillRect(kurSzer*kolumna,kurWys*linia,kurSzer,kurWys);
			g2d.setColor(textColor);
			g2d.drawRect(kurSzer*kolumna,kurWys*linia,kurSzer,kurWys);
  }}