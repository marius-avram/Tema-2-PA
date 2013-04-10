interface Player { 
	
	/** Muta in functie de tipul jucatorului (alb/ negru) 
	 * cu numarul de spatii date.
	 * Implementarea difera in functie de jucator pentru ca 
	 * la piese albe mutarea se face in sens crescator, iar 
	 * la piese negre mutarea se face in sens descrescator.
	 * @return casuta finala
	 */
	int moveChecker(int startingPosition, int n);
	
	/** Determina daca jucatorul se afla in faza de final, 
	 * in care toate piesele sunt in casa lui.
	 * @param checkersLocation locatia pieselor pe tabla
	 * @return true/false daca se afla in faza de final sau nu
	 */
	boolean isInEndPhase(int[] checkersLocation);
	
	/** Returneaza in ordine crescatoare intervalul in care 
	 * se afla casa adversarului. (arrayul contine 2 elemente)*/ 
	int[] getOpponentHouseInterval();
	
	/** Returneaza indicele piesei aflata cel mai departe de 
	 * iesirea din casa. Functia va fi utilizata pentru generarea 
	 * mutarilor in faza terminala */
	int getFarest(int[] checkersLocation, int[] opponentCheckersLocation, int dice);
	
	/** Returneaza pozitia piesei care daca ar fi scoasa ar folosi 
	 * capacitatea zarului la maxim. Folosita de asemenea pentru 
	 * generarea de mutari in faza terminala. */
	int getFarest (int[] checkersLocation, int dice);
	
	/** Determina numarul de linii libere consecutive dinspre 
	 * margine spre restul tablei. Folosita pentru functia de 
	 * evaluare expectimax */ 
	int closerToHouse(int[] checkersLocation);
	
	/** Casuta de start pentru piese de o anumita culoare */
	int getStartingPoint();
	
	/** Casuta de final pentru piesele de o anumita culoare*/
	int getEndingPoint();
	
	/** Determina daca doi playeri apartin aceleasi instante */
	boolean equals(Object o);
	
}