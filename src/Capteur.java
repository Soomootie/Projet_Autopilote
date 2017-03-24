import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.*;

import javax.json.*;

public class Capteur {
	String sender_class;
	String sender_name;
	int sender_id;
	
	public Capteur(){
		
	}
	
	
	public Capteur(String sender_class, String sender_name) {
		this.sender_class = sender_class;
		this.sender_name = sender_name;
		this.sender_id = -1;
	}
	
	public String getSender_class() {
		return sender_class;
	}

	public void setSender_class(String sender_class) {
		this.sender_class = sender_class;
	}

	public String getSender_name() {
		return sender_name;
	}

	public void setSender_name(String sender_name) {
		this.sender_name = sender_name;
	}

	public int getSender_id() {
		return sender_id;
	}


	public void setSender_id(int sender_id) {
		this.sender_id = sender_id;
	}
	
	public void registerSender(String senderClass, String senderName){
		JsonObject jsonObj = Json.createObjectBuilder()
				.add("name", senderName)
				.add("class", senderClass)
				.build();
		Socket socket;
		try {
			socket = new Socket(InetAddress.getLocalHost(), 2002);
			OutputStream out = socket.getOutputStream();
			JsonWriter jswr = Json.createWriter(out);
			jswr.writeObject(jsonObj);
			InputStream in = socket.getInputStream();
			JsonReader jsonread = Json.createReader(in);
			JsonObject jsonObjrd = jsonread.readObject();
			this.setSender_id(jsonObjrd.getInt("id"));
			// ici ne pas faire de print
			System.out.println(jsonObjrd.getString("ack"));
			jsonread.close();
			socket.close();

		}catch (UnknownHostException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public int deregisterSender(int sender_id){
		
		return 0;
	}

	@Override
	public String toString() {
		return "Capteur [sender_class=" + sender_class + ", sender_name="
			+ sender_name + "]";
	}
}
