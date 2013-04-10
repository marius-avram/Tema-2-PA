
public class WhitePlayer implements Player {
	public static int STARTING_POINT = 24;
	public static int ENDING_POINT = 1;
	public static int MIDPOINT = 7;
	
	@Override
	public int moveChecker(int startingPosition, int n) {
		int finalPosition = 0;
		/* Daca piesa este pe bara */
		if (startingPosition == Board.BAR){ 
			finalPosition = STARTING_POINT - n + 1;
		}
		/* Altfel piesa este pe tabla, in joc */
		else {
			finalPosition = startingPosition - n;
			/* Mutarea ne-a dus piesa la capatul jocului */
			if (finalPosition < getEndingPoint()){
				finalPosition = Board.OUT;
			}
		}
		return finalPosition;
	}
	
	@Override
	public boolean isInEndPhase(int[] checkersLocation){
		/* Cazul in care avem o piesa pe bara */
		if (checkersLocation[Board.BAR] != 0) {
			return false;
		}
		int i = getStartingPoint();
		for( ; i >= MIDPOINT; i--){
			if (checkersLocation[i] > 0){ 
				return false;
			}
		}
		return true;
	}
	
	
	@Override
	public int getFarest(int[] checkersLocation, int[] opponentLocations, int dice) {
		int result = -1;
		int newLocation = 0;
		for (int i = MIDPOINT-1; i >= ENDING_POINT; i--) {
			if (checkersLocation[i] > 0) {
				newLocation = i-dice;
				
				if ((newLocation >= ENDING_POINT && opponentLocations[newLocation] <= 1) ||
					(newLocation < ENDING_POINT)){
					result = i;	
					return result;
				}
				else if (newLocation >= ENDING_POINT && opponentLocations[newLocation] > 1){
					return -1;
				}
			}
		}
		return result;
	}
	
	public int getFarest (int[] checkersLocation, int dice){
		int result = ENDING_POINT + dice - 1;
		if (checkersLocation[result] > 0){
			return result;
		}
		return -1;
	}
	

	@Override
	public int closerToHouse(int[] checkersLocation) {
		int free = 0;
		for (int i = STARTING_POINT; i >= ENDING_POINT; i--){
			if (checkersLocation[i] > 0)
				break;
			free++;
		}
		return free;
	}

	@Override
	public int[] getOpponentHouseInterval() {
		int[] result = new int[2];
		result[0] = 19; 
		result[1] = 24;
		return result;
	}
	
	
	@Override
	public int getStartingPoint() {
		return STARTING_POINT;
	}
	@Override
	public int getEndingPoint() {
		return ENDING_POINT;
	}
	
	@Override 
	public boolean equals(Object o){
		return (o instanceof WhitePlayer);
	}


	
	
}
