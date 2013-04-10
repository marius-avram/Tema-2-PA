
public class ExpectiMax {
	// PRO's
	private static int HOUSES = 70; // mai mult de o piesa pe casuta
	private static int EAT = 40; // am capturat o piesa a adversarului
	private static int SPREAD = 5; // intinderea pe tabla
	private static int AWAY_FROM_BASE = 10; // piese care incarca sa paraseasca 
									     // baza inamicului
	private static int WIN = 100; // jocul a fost castigat
	
	// CON's
	private static int TOWERS = -3; // mai mult de 3 piese pe o casuta
	private static int EXPOSED = -20; // o singura piesa expusa pe tabla
	private static int EATEN = -20; //piesa mancata de inamic
	private static int LOSE = -100; // jocul a fost pierdut
	
	private static float PROBABILITY = 1F/21F; // probabilitatea de a se intampla unul
						//evenimentele de aruncare a zarurilor (au probabilitate egala)
	private Board bestChoice;
	private Player theOtherPlayer;
	
	
	public ExpectiMax(Player theOtherPlayer){
		this.theOtherPlayer = theOtherPlayer;
	}
	
	
	public Board getBestChoice(){
		return bestChoice;
	}
	
	/* Determina valoarea euristica a unui nod/ a tablei de joc 
	 * raporata la jucatorul din board*/
	public int heuristicValue(Board board){
		Player ownPlayer = board.getPlayer();
		int[] playerCheckers = board.getPlayerCheckers();
		int[] opponentCheckers = board.getOpponentCheckers();
		
		
		int houses = 0, eat = 0, spread = 0, win = 0; 
		int towers = 0, exposed = 0, left = 0, eaten = 0, 
			lose = 0;
		int result = 0;
		for (int i = 1; i < Board.POSITIONS - 1; i++){
			if (playerCheckers[i] > 1)
				houses++;
			if (playerCheckers[i] != 0) 
				spread++;
			
			if (playerCheckers[i] > 2)
				towers += playerCheckers[i] - 2;
			if (playerCheckers[i] == 1)
				exposed++;
		}
		
		eat = opponentCheckers[Board.BAR];
		eaten = playerCheckers[Board.BAR];
		if (board.gameEnded()) {
			if (board.Won())
				win++;
			else 
				lose++;
		}
		
		
		left = ownPlayer.closerToHouse(playerCheckers);
			
		
		result = (HOUSES*houses + EAT*eat + SPREAD*spread 
				+ AWAY_FROM_BASE*left + WIN*win) + (TOWERS*towers 
				+ EXPOSED*exposed + EATEN*eaten + LOSE*lose);
		
		return result;
	}
	
	
	/** Metoda va fi apelata de forma expectimax(board, 3, 0, dice1, dice2) pentru
	 * o cautare la primul nivel */
	public float expectimax(Board board, int depth, int level, int dice1, int dice2) { 
		float alfa = 0, deeper;
		Board temporaryCopy = null;
		
		if (depth == 0) {
			/* Am ajuns la ultimul nivel. Returnam valoarea euristica 
			 * a tablei de joc in functi de configuratia pieselor
			 * de la acest nivel */
			return heuristicValue(board);
		}
		
		depth--;
		/* E randul nostru sa jucam */
		if (level % 3 == 0){
			level++;
			alfa = Integer.MIN_VALUE;
			/* Generam toate mutarile pentru combinatia de zaruri initiala, 
			 * pe care am primit-o de la server. */
			for (Board child : board.generateTableConfigurations(dice1, dice2, board.getPlayer())){
				if (level == 1){
					temporaryCopy = new Board(child);
				}
				deeper = expectimax(child, depth, level, dice1, dice2);
				
				if (alfa < deeper){ 
					// determin maximul
					alfa = deeper; 
					// Salvam tabla in caz de succes
					bestChoice = temporaryCopy;
				}
			}
		}
		
		/* Urmeaza un eveniment aleator */
		else if (level % 3 == 1) {
			level++;
			alfa = 0;
			/* Generam toate combinatiile de zaruri posibile si le dam 
			 * mai departe in recursivitate. */
			for (int i = 1; i <=2; i++) {
				for (int j = i; j <= 2; j++){
					alfa += PROBABILITY * expectimax(board, depth, level, i, j);
				}
			}
		}
		
		/* E randul adversarului sa joace */
		else if (level % 3 == 2) {
			level++;
			alfa = Integer.MAX_VALUE;
			/* Pentru combinatia de zaruri data la nivelul random, 
			 * generam toate mutarile posibile pe care putem sa le 
			 * facem cu o anumita configuratie. */
			for(Board child : board.generateTableConfigurations(dice1, dice2, theOtherPlayer)){
				deeper = expectimax(child, depth, level, dice1, dice2);
				if (alfa > deeper){ 
					//determin minimul
					alfa = deeper; 
				}
			}
		}
		
		return alfa;
	}
	
	/** Apeleaza metoda expectimax si seteaza comanda pentru tabla 
	 * aleasa astfel incat aceasta sa aiba formatul necesar trimiterii
	 * catre server. */
	public void doBestChoice(Board board, int dice1, int dice2){
		
		float alfa = expectimax(board, 3, 0, dice1, dice2);
		
		byte[] moveCommand = bestChoice.getMoveCommand();
		
		if (alfa == Integer.MIN_VALUE ) { 
			moveCommand = new byte[1];
			moveCommand[0] = 0;
			bestChoice.setMoveCommand(moveCommand);
		}
		
	}
	
}
