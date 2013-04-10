import java.util.ArrayList;
import java.util.Stack;

public class Board {
	private int[] whiteCheckers; 
	private int[] blackCheckers;
	private int[] opponentCheckers; // Vor fi referinte catre primele. 
	private int[] playerCheckers;	// opponentCheckers este referinta catre 
				// piesele adversarului cu care a fost instantiata clasa
	private int[] ownCheckers;		// Din nou referinte catre primele 
	private int[] theOtherCheckers;	// theOtherCheckers reprezinta 
				// piesele adversarului pentru anumite functii care iau ca
				// parametru un argument de tip Player
	private Stack<int[]> whiteCheckersBackups;
	private Stack<int[]> blackCheckersBackups;

	
	private Player player;
	private byte[] moveCommand; 
	
	public static int MAX_STRIPES = 24;
	public static int BAR = 0;
	public static int OUT = 25;
	public static int POSITIONS = 26;
	private static int PIECES_PER_PLAYER = 15;
	
	public Board(Player player){
		/* Vom rezerva pozitia 0 pentru piesele aflate pe bara 
		 * si pozitia 25 pentru piesele scoase din joc (care au
		 * parcurs tot drumul). 
		 */
		whiteCheckers = new int[POSITIONS];
		blackCheckers = new int[POSITIONS];
		
		/* Copiile tablei de joc de la un anumit moment de timp 
		 * se salveaza intr-o stiva. Astfel incat atunci cand 
		 * vom dori sa facem Undo prima mutare la care se revine 
		 * este ultima adaugata in stiva.
		 */
		whiteCheckersBackups = new Stack<int[]>();
		blackCheckersBackups = new Stack<int[]>();
		
		/*Initializam tabla de joc */
		whiteCheckers[24] 	= 2;
		whiteCheckers[13] 	= 5; 
		whiteCheckers[8] 	= 3;
		whiteCheckers[6] 	= 5; 
		
		
		blackCheckers[1]	= 2;
		blackCheckers[12]	= 5;
		blackCheckers[17]	= 3;
		blackCheckers[19]	= 5; 		
		
		
		this.setPlayer(player);
		/* In functie de caz initializam variabilele 
		 * opponentCheckers si playerCheckers.*/
		updateReferences();
		
	}
	
	/** Creeaza o instanta a unei table pe baza unei instante deja 
	 * existente. */
	public Board(Board board){
		this(board.getPlayer());
		System.arraycopy(board.whiteCheckers, 0, whiteCheckers, 0, POSITIONS);
		System.arraycopy(board.blackCheckers, 0, blackCheckers, 0, POSITIONS);
		if (board.moveCommand != null && board.moveCommand != null) {
			moveCommand = new byte[10];
			System.arraycopy(board.moveCommand, 0, moveCommand, 0, board.moveCommand.length);
		}
	}
	
	
	/** Niste setteri si getteri in continuare */
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int[] getPlayerCheckers() {
		return playerCheckers;
	}

	public int[] getOpponentCheckers() {
		return opponentCheckers;
	}

	public void setOpponentCheckers(int[] opponentCheckers) {
		this.opponentCheckers = opponentCheckers;
	}

	public void setPlayerCheckers(int[] playerCheckers) {
		this.playerCheckers = playerCheckers;
	}

	public byte[] getMoveCommand() {
		return moveCommand;
	}

	public void setMoveCommand(byte[] moveCommand) {
		int size = moveCommand[0]*2 + 1;
		this.moveCommand = new byte[size];
		System.arraycopy(moveCommand, 0, this.moveCommand, 0, size);
	}
	
	/** Seteaza campurile OwnCheckers si theOthersCheckers. Acestea 
	 * indica care sunt piesele proprii ale playerului cu care este 
	 * apelata metoda si care sunt piesele adversarului sau.
	 * Adversarul nu este neaparat cel din opponentCheckers. Cazul 
	 * poate fi si invers. */
	public void setOwnAndTheOtherCheckers(Player player){
		
		if (this.getPlayer().equals(player)){
			ownCheckers = getPlayerCheckers(); 
			theOtherCheckers = getOpponentCheckers();
		}
		else {
			ownCheckers = getOpponentCheckers(); 
			theOtherCheckers = getPlayerCheckers();
		}
		
	}

	/** Actualizeaza referintele variabilelor membru 
	 * playerCheckers si opponentCheckers. Ele tin referinte catre 
	 * piesele jucatorului(player) si ale adversarului.
	 */
	private void updateReferences(){
		if (getPlayer() instanceof WhitePlayer) {
			setPlayerCheckers(whiteCheckers);
			setOpponentCheckers(blackCheckers);
		} 
		else if (getPlayer() instanceof BlackPlayer) {
			setPlayerCheckers(blackCheckers);
			setOpponentCheckers(whiteCheckers);
		}
	}
	
	
	/** Genereaza toate mutarile valide pentru o anumita 
	 * valoare a zarului. Functia este folosita doar de 
	 * playerul dat ca parametru clasei prin constructor.
	 * @param dice valoarea unui zar de la 1..6
	 * @return lista cu pozitiile pieselor ce se pot muta,
	 * distanta se stie din parametru
	 */
	public ArrayList<Integer> generateValidMoves(int dice, Player p){ 
		
		ArrayList<Integer> moves = new ArrayList<Integer>();
		int newPos;
		setOwnAndTheOtherCheckers(p);
		boolean endPhase = p.isInEndPhase(ownCheckers);
		
		/* Cazul in care avem piese pe bara trebuie sa le mutam mai 
		 * intai pe acestea inainte celorlalte.
		 */
		if (ownCheckers[BAR] != 0){
			newPos = p.moveChecker(BAR, dice);
			if (theOtherCheckers[newPos] <= 1){ 
				moves.add(BAR);
			}
			return moves;
		}
		
		/* Cazul in care ne aflam in faza terminala trebuie sa mutam 
		 * piesa aflata cel mai departe de iesirea din casa */
		if (endPhase) {
			int best = p.getFarest(ownCheckers, dice);
			if (best != -1) {
				moves.add(best);
				return moves;
			}
			
			best = p.getFarest(ownCheckers, theOtherCheckers, dice);
			if (best != -1) {
				moves.add(best);
			}
			
			return moves;
		
		}
		
		for (int i=0; i <= MAX_STRIPES; i++){
			if (ownCheckers[i] > 0) {
				newPos = p.moveChecker(i, dice);
				/* Adaugam in Lista piesa pe care dorim sa o mutam*/
				if (theOtherCheckers[newPos] <= 1 || newPos == OUT){
					if (newPos == OUT) {
						if (endPhase) {
							moves.add(i);
						}
					}
					else { 
						moves.add(i);
					}
				}
			}
		}
		
		return moves;
	}
	
	/** Realizeaza o mutare pentru jucator sau oponent a unei piese 
	 * de pe pozitia from, catre pozitia to. Functia actualizeaza
	 * tabla daca mutarea se poate efectua. Atat jucatorul cat si 
	 * adversarul pot efectua mutari cu aceasta functie. Au fost 
	 * reluate verificari de validitate ale mutarilor pentru ca 
	 * functia este folosita in generarea tuturor mutarilor posibile 
	 * ale adversarului. Generarea se face direct cu aplicarea pe 
	 * tabla de joc.
	 *   
	 * @return indicele casutei pe care se face mutarea. In cazul 
	 * in care mutarea nu este valida indicele este -1.
	 */
	public int makeMove(int from, int dist, Player p) {
		
		int to = p.moveChecker(from, dist);
		boolean endPhase;
		
		this.setOwnAndTheOtherCheckers(p);	
		
		/* Daca mutarea duce piesa in afara tablei trebuie sa se verifice
		 * daca se poate efectua acea mutare - ne aflam in faza terminala. */
		if (to == OUT){ 
			endPhase = p.isInEndPhase(ownCheckers);
			if (!endPhase) {
				return -1;
			}
		}
		
		
		/*Trebuie sa verificam daca mutarea este valida */
		if ((ownCheckers[from] > 0 && theOtherCheckers[to] <= 1) 
			|| (to == OUT)) { 
			ownCheckers[from]--;
			ownCheckers[to]++;
			/* S-a scos o piesa a adversarului jucatorului dat
			 * ca parametru.*/
			if (theOtherCheckers[to] == 1 && to != OUT){
				theOtherCheckers[BAR]++;
				theOtherCheckers[to]--;
			}
		}
		else { 
			return -1;
		}
		
		
		return to;
	}
	
	/** Genereaza toate mutarile pentru 2 zaruri date. Mutarile astfel 
	 * generate sunt introduse intr-un ArrayList de Boards. Avem nevoie
	 * si de alte informatii in afara de asezarea pieselor pe tabla, 
	 * pentru ca vom dori sa aflam candva valoarea euristica a nodului.
	 * 
	 * Obs: functia genereaza mutari doar pentru zaruri cu valori diferite
	 * (cand nu avem dubla).
	 */
	private ArrayList<Board> generateValidBoardsForTwo(int dice1, int dice2, Player p){
		ArrayList<Integer> moves1; // Mutarile pentru primul zar
		ArrayList<Integer> moves2; // Mutarile pentru al doilea zar
		ArrayList<Board> configurations = new ArrayList<Board>();
		int maxMoves = 0; // Retine numarul maxim de mutari din configuratii
		Board config;
		byte[] moveCommand;
		
		makeBoardBackup();
		moves1 = generateValidMoves(dice1, p);
		
		for (int i=0; i < moves1.size(); i++){
			makeMove(moves1.get(i), dice1, p) ;
			if (1 > maxMoves){
				maxMoves = 1;
				configurations.clear();
			}
			moveCommand = new byte[5];
			moveCommand[1] = moves1.get(i).byteValue();
			moveCommand[2] = (byte)dice1;
			
			/* Se genereaza pe baza fiecarui aruncari a primului
			 * zar. Vor exista mai multe variante, din fiecare 
			 * derivand alte configuratii de tabla.
			 */
			makeBoardBackup();
			moves2 = generateValidMoves(dice2, p);
			/* Inseamna ca nu mai avem nici o a doua mutare in acest caz.
			 * Se adauga doar daca nu exista alte mutari care sa foloseasca
			 * ambele zaruri.*/
			if (moves2.size() == 0 && maxMoves == 1){
				config = new Board(this);
				moveCommand[0] = 1; // Salvam nr de mutari
				config.setMoveCommand(moveCommand);
				configurations.add(config);
				break;
			}
			for (int j=0; j < moves2.size(); j++) {
				//System.out.println("*Moved " + moves2.get(j) + " with " + dice2);
				makeMove(moves2.get(j), dice2, p) ;
				moveCommand[3] = moves2.get(j).byteValue();
				moveCommand[4] = (byte)dice2;
				moveCommand[0] = 2;
				if (2 > maxMoves){
					maxMoves = 2;
					/* Avem cel putin o mutare valida care sa 
					 * foloseasca cele doua zaruri. Facem clear 
					 * pentru a elimina eventualele mutari anterioare 
					 * care folosesc doar un singur zar.
					 */
					configurations.clear();
				}
				config = new Board(this);
				config.setMoveCommand(moveCommand);
				configurations.add(config);
				undoMove();	
			}
			removeBackup();
			undoMove();
		}
		removeBackup();
		
		return configurations;
	}
	
	/** Genereaza toate mutarile posibile pentru o dubla (cand 
	 * jucatorul arunca de doua ori acelasi zar. Din nou se 
	 * returneaza o lista de Board si nu doar configuratia pieselor
	 * pe tabla. */
	private ArrayList<Board> generateBoardsForDouble(int dice, Player p) {
		ArrayList<Integer> moves1, moves2, moves3, moves4;
		ArrayList<Board> configurations = new ArrayList<Board>();
		int maxMoves = 0;
		byte[] moveCommand;
		Board config;
		
		moves1 = generateValidMoves(dice, p);
		
		makeBoardBackup();
		for (int i=0; i < moves1.size(); i++){
			moveCommand = new byte[9];
			moveCommand[1] = moves1.get(i).byteValue();
			moveCommand[2] = (byte)dice;
			makeMove(moves1.get(i), dice, p);
			
			if (1 > maxMoves) { 
				maxMoves = 1;
			}
			moves2 = generateValidMoves(dice, p);
			if (moves2.size() == 0 && maxMoves == 1){
				/* Nu mai exista mutare valida dupa prima mutare facuta. */
				moveCommand[0] = 1;
				config = new Board(this);
				config.setMoveCommand(moveCommand);
				configurations.add(config);
				break;
			}
			makeBoardBackup();
			for (int j=0; j < moves2.size(); j++){
				moveCommand[3] = moves2.get(j).byteValue();
				moveCommand[4] = (byte)dice;
				makeMove(moves2.get(j), dice, p) ;
				
				if (2 > maxMoves){
					maxMoves = 2;
					configurations.clear();
				}
				moves3 = generateValidMoves(dice, p);
				if (moves3.size() == 0 && maxMoves == 2){
					/* Nu mai exista mutare valida dupa primele doua mutari
					 * facute */
					moveCommand[0] = 2;
					config = new Board(this);
					config.setMoveCommand(moveCommand);
					configurations.add(config);
					break;
				}
				makeBoardBackup();
				for (int k=0; k < moves3.size(); k++){
					moveCommand[5] = moves3.get(k).byteValue();
					moveCommand[6] = (byte)dice;
					makeMove(moves3.get(k), dice, p);
						
					if (3 > maxMoves) {
						maxMoves = 3;
						configurations.clear();
					}
					moves4 = generateValidMoves(dice, p);
					if (moves4.size() == 0 && maxMoves == 3){
						/* Nu mai exista mutare valida dupa primele trei mutari
						 * facute. Se salveaza doar daca nu avem mutari care sa
						 * foloseasca la maxim zarurile. */
						moveCommand[0] = 3;
						config = new Board(this);
						config.setMoveCommand(moveCommand);
						configurations.add(config);
						break;
					}
					makeBoardBackup();
					for (int l=0; l < moves4.size(); l++){
						if (4 > maxMoves) {
							maxMoves = 4;
							configurations.clear();
						}
						makeMove(moves4.get(l), dice, p) ;
						/* Salvam tabla de joc si totodata salvam mutarile 
						 * facute pentru a ajunge de la tabla initiala la 
						 * aceasta tabla. */
						config = new Board(this);
						moveCommand[7] = moves4.get(l).byteValue();
						moveCommand[8] = (byte)dice;
						moveCommand[0] = 4;
						config.setMoveCommand(moveCommand);
						configurations.add(config);
						undoMove();
					}
					removeBackup();
					undoMove();
				}
				removeBackup();
				undoMove();
			}
			removeBackup();
			undoMove();
		}
		removeBackup();
		
		return configurations;
	}
	
	/** Genereaza toate configuratiile de piese posibile pe tabla 
	 * de joc pe baza a doua zaruri aruncate. */
	public ArrayList<Board> generateTableConfigurations(int dice1, int dice2, Player p){
		
		ArrayList<Board> configurations = new ArrayList<Board>();
		this.setOwnAndTheOtherCheckers(p);	
		/* Cazul normal */
		if (dice1 != dice2){
			if (p.isInEndPhase(ownCheckers) && dice1 < dice2) {
				int aux = dice1;
				dice1 = dice2; 
				dice2 = aux;
			}
			configurations = generateValidBoardsForTwo(dice1, dice2, p);
			if (configurations.isEmpty()){
				/* Daca prima combinatie de zaruri nu returneaza nimic, 
				 * incercam si invers. */
				configurations = generateValidBoardsForTwo(dice2,dice1,p);
			}
		}
		
		/* Cazul in care avem dubla */
		else {
			
			configurations = generateBoardsForDouble(dice1, p);
		}
		
		return configurations;
	}
	
	/** Determina daca jocul s-a terminat sau nu */
	public boolean gameEnded(){
		if (whiteCheckers[OUT] == PIECES_PER_PLAYER || 
			blackCheckers[OUT] == PIECES_PER_PLAYER) 
			return true; 
		else 
			return false;
	}
	
	/** Determina daca jocul a fost castigat de jucatorul 
	 * cu care a fost initializat clasa Board. */
	public boolean Won(){ 
		if (gameEnded() && getPlayerCheckers()[OUT] == PIECES_PER_PLAYER)
			return true;
		else 
			return false;
	}
	
	/** Determina numarul maxim de piese care sa gaseste pe o casuta,
	 * functia este folosita pentru afisarea corecta a tablei.*/
	private int MaxPiecesOnStripesInterval(int begining, int end){
		int max = 5;
		for (int i=begining; i <= end; i++){ 
			if (whiteCheckers[i] > max){ 
				max = whiteCheckers[i];
			}
			if (blackCheckers[i] > max){
				max = blackCheckers[i];
			}
		}
		return max;
	}
	
	/** Face un back-up al pieselor. Functia este folosita pentru a putea 
	 * reveni tabla anterioara atunci cand se face o mutare.*/
	public void makeBoardBackup(){
		int[] whiteCopy = new int[POSITIONS];
		int[] blackCopy = new int[POSITIONS];
		System.arraycopy(whiteCheckers, 0, whiteCopy, 0, POSITIONS);
		System.arraycopy(blackCheckers, 0, blackCopy, 0, POSITIONS);
		whiteCheckersBackups.push(whiteCopy);
		blackCheckersBackups.push(blackCopy);
	}
	
	/** Se revine la configuratia anterioara a pieselor. Aceasta returneaza 
	 * ultima configuratie salvata cu makeBoardBackup. Foloseste o structura 
	 * de tip stiva pentru a intoarce tabla cu timpul care a trecut de la 
	 * salvare cel mai scurt.*/
	public boolean undoMove(){
		if (whiteCheckersBackups.empty() || blackCheckersBackups.empty()){
			return false;
		}
		int[] whiteTop = whiteCheckersBackups.peek();
		int [] blackTop = blackCheckersBackups.peek();
		System.arraycopy(whiteTop, 0, whiteCheckers, 0, POSITIONS);
		System.arraycopy(blackTop, 0, blackCheckers, 0, POSITIONS);
		updateReferences();
		return true;
	}
	
	/** Sterge un backup al unei table din varful stivei */
	public boolean removeBackup(){
		if (whiteCheckersBackups.empty() || blackCheckersBackups.empty()){
			return false;
		}
		whiteCheckersBackups.pop();
		blackCheckersBackups.pop();
		return true;
	}
	
	/** Sterge toate backupurile. */
	public void clearBackups(){
		whiteCheckersBackups.clear();
		blackCheckersBackups.clear();
	}
	
	/** Numarul maxim de piese aflate pe portia de deasupra
	 * a tablei de joc. Rol in afisare. */
	private int MaxPiecesOnStripesAbove(){ 
		return MaxPiecesOnStripesInterval(13,24);
	}
	
	/** Numarum maxim de piese aflate pe pozitia de jos a 
	 * tablei de joc. Rol in afisare. */
	private int MaxPiecesOnStripesBelow(){ 
		return MaxPiecesOnStripesInterval(1,12);
	}
	
	/** Functie folosita in afisare */
	private String DrawLineIncreasingly(int i, int from, int to){
		String result="";
		for (int j = from; j <= to; j++){ 
			if (whiteCheckers[j] >= i) { 
				result += "O ";
			}
			else if (blackCheckers[j] >= i) { 
				result += "X ";
			}
			else {
				result += "  ";
			}
			
			if (j != to){ 
				result += " ";
			}
		}
		return result;
	}
	
	/** Functie folosita in afisare */
	private String DrawLineDecreasingly(int i, int from, int to){
		String result="";
		for (int j = from; j >= to; j--){ 
			if (whiteCheckers[j] >= i) { 
				result += "O ";
			}
			else if (blackCheckers[j] >= i) { 
				result += "X ";
			}
			else {
				result += "  ";
			}
			
			if (j != to) { 
				result += " ";
			}
		}
		return result;
	}
	
	/** Functia de afisare a tablei */
	public String toString(){ 
		String result, middle;
		String endln = System.getProperty("line.separator");
		int limit;
		result = "+13-14-15-16-17-18------19-20-21-22-23-24-+" + endln;
		middle = "|  |  ";
		/* Desenam Partea de sus a tablei */
		limit = MaxPiecesOnStripesAbove();
		for (int i = 1; i <= limit; i++){
			result += "| " + DrawLineIncreasingly(i, 13, 18);
			result += middle;
			result += DrawLineIncreasingly(i, 19, 24) + "|" + endln;
		}
		
		result += "|==================|  |===================|" + endln;
		
		/* Desenam Partea de jos a tablei */
		limit = MaxPiecesOnStripesBelow();
		for (int i=limit; i >= 1; i--){
			result += "| " + DrawLineDecreasingly(i, 12, 7);
			result += middle;
			result += DrawLineDecreasingly(i, 6, 1) + "|" + endln;
		}
		
		result += "+12-11-10--9--8--7-------6--5--4--3--2--1-+" + endln;
		result += "BAR W:" + this.whiteCheckers[BAR] + " OUT W:" + this.whiteCheckers[OUT] + endln;
		result += "BAR B:" + this.blackCheckers[BAR] + " OUT B:" + this.blackCheckers[OUT] + endln;
		
		return result;
	
	}
}
