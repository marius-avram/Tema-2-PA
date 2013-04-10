import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Main {
	
	public static void main(String args[]) {
		if (args.length < 4) {
			System.out.println("Usage: ./client server_hostname server_port " +
					"opponent_level(1=dumb, 5, 7, 8) own_level(1=dumb, 5, 7, 8)");
			return;
		}
		// variabile pentru conexiune
		Socket socket = null;
		DataOutputStream out = null;
        DataInputStream in = null;
        
        // variabile pentru joc;
        Player ownPlayer = null, theOtherPlayer = null;
        String endln = System.getProperty("line.separator");
        
		try {
			// realizez conexiunea la server
			socket = new Socket(args[0], Integer.parseInt(args[1]));
			// scriu in out pe socket
			out = new DataOutputStream(socket.getOutputStream());
			// citesc din in de pe socket
            in = new DataInputStream(socket.getInputStream());
            
            // trimit primul mesaj - dificulatea adversarului
            byte[] message = new byte[1];
            message[0] = Byte.parseByte(args[2]);
            ServerConnection.sendMessage(message, out);
            
            // primesc raspuns cu culoarea mea
            message = ServerConnection.readMessage(in);
            if (message[0] == 1) {
            	ownPlayer = new BlackPlayer();
            	theOtherPlayer = new WhitePlayer();
            	System.out.println("Sunt jucatorul Negru.");
            } else if (message[0] == 0) {
            	ownPlayer = new WhitePlayer();
            	theOtherPlayer = new BlackPlayer();
            	System.out.println("Sunt jucatorul Alb.");
            } else {
            	System.out.println("Eroare atribuire jucator.");
            }
            
            Board board = new Board(ownPlayer);
            ExpectiMax exp = new ExpectiMax(theOtherPlayer);
            
            int nMoves, from, span, dice1, dice2, round=0;
            byte[] moveCommand;
            
            while (true) {
            	message = ServerConnection.readMessage(in);
            	/* Se primeste un mesaj de la server care contine miscarile adversarului 
            	   si zarul propriu. Daca suntem primii la mutare, atunci nu primim 
            	   mutari din partea adversarului. Daca adversarul nu a putut sa mute, 
            	   atunci nu primim mutari din partea adversarului. */
            	round++;
            	nMoves = 0;
            	
            	if (message[0] == 76){ 
            		System.out.println("Jocul a fost pierdut.");
            		break;
            	}
            	if (message[0] == 87){ 
            		System.out.println("Jocul a fost castigat!");
            		break;
            	}
            	
            	System.out.println("");
            	// Se actualizeaza tabla cu mutarile adversarului.
            	while (message.length - nMoves*2 > 2){ 
            		from = message[nMoves*2];
            		if (from == 30) {
            			from = 0;
            		}
            		span = message[nMoves*2+1];
            		board.makeMove(from, span, theOtherPlayer);
            		nMoves++;
            	}
            	
            	System.out.println("Runda " + round + endln);
            	System.out.println(board);
            	
            	/* Daca jocul s-a terminat se iese din bucla */
            	if (board.gameEnded()){
            		break;
            	}
            	
            	dice1 = message[nMoves*2];
            	dice2 = message[nMoves*2+1];
            	
            	
            	System.out.println("Zarurile aruncate: " + dice1 + " " + dice2);
            	/* Apelam functia cu zarurile sortate crescator pentru a include 
            	 * toate mutarile posibile */
            	if (dice1 < dice2) {
            		exp.doBestChoice(board, dice1, dice2);
            	}
            	else { 
            		exp.doBestChoice(board, dice2, dice1);
            	}
            	
            	moveCommand = exp.getBestChoice().getMoveCommand();
            	
            	/* Compunerea mesajului ce urmeaza sa fie trimis catre 
            	 * server. */
            	byte[] response;
            	if (moveCommand[0] == 0){
            		response = new byte[0];
            	}
            	else {
	            	response = new byte[moveCommand[0]*2];
	            	for(int i = 0; i < response.length; i++){
	            		response[i] = moveCommand[i+1];
	            		if (response[i] == 0) { 
	            			response[i] = 30;
	            		}
	            	}
            	}
            	round++;
            	// Afisam noua tabla de joc.
            	System.out.println("Runda " + round + endln);
            	board = exp.getBestChoice();
            	System.out.println(board);
            	ServerConnection.sendMessage(response, out);
            	
            }
            socket.close();
            
    		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
		
	}
}
