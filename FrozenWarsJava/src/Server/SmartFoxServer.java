package Server;

import java.net.InetAddress;
import java.net.URL;


import Application.MatchManager;
import Application.MatchManager.Direction;
import com.smartfoxserver.v2.exceptions.SFSException;
import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.requests.PublicMessageRequest;

public class SmartFoxServer implements IEventListener {

	private static final String SFS_ZONE = "BasicExamples";
	private SmartFox sfsClient;
	private MatchManager manager;
	
	public SmartFoxServer(){
		String ip = getServerIP();
		sfsClient = new SmartFox(false);
		sfsClient.connect(ip,9933);
		addEventListeners();
	}
	
	public void addManager(MatchManager manager){
		this.manager = manager;
	}
	
	
	public int getMyPlayerId(){
		return sfsClient.getMySelf().getPlayerId();
	}
	
	private void addEventListeners() {
		sfsClient.addEventListener(SFSEvent.CONNECTION, this);
		sfsClient.addEventListener(SFSEvent.LOGIN, this);
		sfsClient.addEventListener(SFSEvent.ROOM_JOIN, this);
		sfsClient.addEventListener(SFSEvent.USER_ENTER_ROOM, this);
		sfsClient.addEventListener(SFSEvent.USER_EXIT_ROOM, this);
		sfsClient.addEventListener(SFSEvent.PUBLIC_MESSAGE,this);
	}

	private String getServerIP() {
		String ip = "";
		try {
			InetAddress address = InetAddress.getByName(new URL("http://boomwars-server.no-ip.org").getHost());
			ip = address.getHostAddress();
		} catch (Exception e){
			
		}
		return ip;
	}

	public void dispatch(BaseEvent event) throws SFSException {

		if(event.getType().equals(SFSEvent.PUBLIC_MESSAGE)){
			String message = event.getArguments().get("message").toString();
			if (message.charAt(0)=='M'){
				Direction dir = null;
				if (message.charAt(1)=='0') dir = Direction.left;
				else if (message.charAt(1)=='1') dir = Direction.right;
				else if (message.charAt(1)=='2') dir = Direction.up;
				else if (message.charAt(1)=='3') dir = Direction.down;
				int jugador = Integer.parseInt(message.substring(2));
				manager.movePlayerEvent(dir,jugador);
			}
			else if (message.charAt(0)=='L'){
				int xPosition = 0;
				int yPosition = 0;
				boolean found = false;
				int i = 0;
				while (i<message.length() & !found){
					found = (message.charAt(i)=='X');
					if (found) xPosition = i;
					else i++;
				}
				i = 0;
				found = false;
				while (i<message.length() & !found){
					found = (message.charAt(i)=='Y');
					if (found) yPosition = i;
					else i++;
				}
				xPosition++;
				int xLancePosition = Integer.parseInt(message.substring(xPosition, yPosition));
				yPosition++;
				int yLancePosition = Integer.parseInt(message.substring(yPosition));
				manager.putLanceEvent(xLancePosition,yLancePosition);
			}
		}
		else if(event.getType().equalsIgnoreCase(SFSEvent.LOGIN))
		{					
			// Join The Lobby room
			sfsClient.send(new JoinRoomRequest("The Lobby"));			    	
		}
		else if (event.getType().equalsIgnoreCase(SFSEvent.ROOM_JOIN)){
			
		}
	}
	
	public void conectaSala(String user){
		sfsClient.send(new LoginRequest(user,"", SFS_ZONE));
	}

	public void sendMove(Direction dir, int myPlayerId) {
		int dirCod = -1;
		if (dir.equals(Direction.left)) dirCod = 0;
		else if (dir.equals(Direction.right)) dirCod = 1;
		else if (dir.equals(Direction.up)) dirCod = 2;
		else if (dir.equals(Direction.down)) dirCod = 3;
		sfsClient.send(new PublicMessageRequest("M"+Integer.toString(dirCod)+Integer.toString(myPlayerId)));	
	}
	
	public void sendLance(int x,int y) {
		sfsClient.send(new PublicMessageRequest("L"+"X"+x+"Y"+y));	
	}
}