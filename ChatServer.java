import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.", id);
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
				if(line.indexOf("/to ") == 0){
					sendmsg(line, id);
				}else if(line.indexOf("/userlist") == 0){
					send_userlist(id);
				}else
					broadcast(id + " : " + line, id);
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.", id);
			System.out.println("[Server] User (" + id + ") exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg, String user){
		Object warn = hm.get(user);
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			if((warn != null) && warring(msg)) {
				PrintWriter user_pw = (PrintWriter)warn;
				user_pw.println("You used warring word, so Your message can't send to other users.");
				user_pw.flush();
			}
			else {
				Object obj = hm.get(to);
				if(obj != null){
					PrintWriter pw = (PrintWriter)obj;
					pw.println(id + " whisphered. : " + msg2);
					pw.flush();
				} // if
			}
		}
	} // sendmsg
	public void broadcast(String msg, String user){
		
		Object obj = hm.get(user);
		if( (obj != null) && warring(msg)) {
			PrintWriter user_pw = (PrintWriter)obj; 
			user_pw.println("You used warring word, so Your message can't send to other users.");
			user_pw.flush();
		}
		else {
			synchronized(hm){
				Collection collection = hm.values();
				Iterator iter = collection.iterator();
				PrintWriter user_pw = (PrintWriter)obj;
				if(obj == null) { //null doesn't need to pw_user
					while(iter.hasNext()){
						PrintWriter pw = (PrintWriter)iter.next();
						pw.println(msg);
						pw.flush();
					}
				}
				else {
					while(iter.hasNext()){
						PrintWriter pw = (PrintWriter)iter.next();
						if(user_pw == pw) {
							//no send message
						}else {
							pw.println(msg);
							pw.flush();
						}
					}
				}
			}
		}
	} // broadcast
	//send to user name for run method ,then user is not null
	//convert Printwriter to user's pw
	//Check if pw and user's pw are same ,then no println
	
	public void send_userlist(String user) {
		int count = 0;
		Object obj = hm.get(user);
		PrintWriter pw = (PrintWriter)obj;
		
		Iterator<String>list = hm.keySet().iterator();
		while(list.hasNext()) {
			String clinet = list.next();
			count++;
			pw.println(clinet);
			pw.flush();
		}
		pw.println("userlist : " + count);
		pw.flush();
	}//userlist Method
	//send to user name for run method
	//save to user's printwriter to Object and convert Printwriter
	//hm.keyset to iterator and loop
	//Make a String key and put the hm.key
	//Printout key 
	
	public boolean warring(String line) {
		ArrayList<String>word = new ArrayList<String>();
		word.add("bitch");
		word.add("idiot");
		word.add("What the hell");
		word.add("fuck");
		word.add("shit");
		
		if(line.contains(word.get(0))||line.contains(word.get(1))||line.contains(word.get(2))||line.contains(word.get(3))||line.contains(word.get(4))) {
			return true;
		}
		
		return false;
	}
}
