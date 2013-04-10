
public class BlackPlayer implements Player {
	
	public static int STARTING_POINT = 1;
	public static int ENDING_POINT = 24;
	public static int MIDPOINT = 18;
	
	@Override
	public int moveChecker(int startingPosition, int n) {
		int finalPosition = 0;
		/*Daca piesa este pe bara */
		if (startingPosition == Board.BAR){
			finalPosition = n;
		}
		/* Altfel piesa este pe tabla in joc */
		else {
			finalPosition = startingPosition + n;
			/* Mutarea ne-a dus piesa la captul jocului */
			if (finalPosition > getEndingPoint()){
				finalPosition = Board.OUT;
			}
		}
		return finalPosition;
	}

	@Override
	public boolean isInEndPhase(int[] checkersLocation){
		if (checkersLocation[Board.BAR] != 0) { 
			return false;
		}
		int i = getStartingPoint();
		for ( ; i <= MIDPOINT; i++){
			if (checkersLocation[i] > 0){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int[] getOpponentHouseInterval() {
		int[] result = new int[2];
		result[0] = 1; 
		result[1] = 6;
		return result;
	}
	
	@Override
	public int getFarest(int[] checkersLocation, int[] opponentLocations, int dice) {
		int result = -1;
		int newLocation = 0;
		for (int i = MIDPOINT+1; i <= ENDING_POINT; i++){
			if (checkersLocation[i] > 0){
				newLocation = i + dice; 
				if ((newLocation <= ENDING_POINT && opponentLocations[newLocation] <= 1)
					 || (newLocation > ENDING_POINT)){
					result = i;
					return result;
				}
				else if (newLocation <= ENDING_POINT && opponentLocations[newLocation] > 1){
					return -1;
				}
			}
		}
		return result;
	}
	
	public int getFarest (int[] checkersLocation, int dice){
		int result = ENDING_POINT - dice + 1;
		if (checkersLocation[result] > 0){
			return result;
		}
		return -1;
	}
	

	@Override
	public int closerToHouse(int[] checkersLocation) {
		int free = 0;
		
		for (int i = STARTING_POINT; i <= ENDING_POINT; i++){
			if (checkersLocation[i] > 0)
				break;
			free++;
		}
		return free;
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
		return (o instanceof BlackPlayer);
	}


	
	
}
