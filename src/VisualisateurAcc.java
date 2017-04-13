import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

public class VisualisateurAcc {
	private int sender_id;
	private int msg_id;

	public VisualisateurAcc(int sender_id, int msg_id) {
		super();
		this.sender_id = sender_id;
		this.msg_id = msg_id;
	}

	public int getSender_id() {
		return sender_id;
	}

	public void setSender_id(int sender_id) {
		this.sender_id = sender_id;
	}

	public int getMsg_id() {
		return msg_id;
	}

	public void setMsg_id(int msg_id) {
		this.msg_id = msg_id;
	}

	public void requeteInformation(){
		
		JsonObject jsonObj = Json.createObjectBuilder()
				.add("sender_id", sender_id)
				.add("msg_id", msg_id)
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
			System.out.println(jsonObjrd.getString("resp"));
			jsonread.close();
			socket.close();

		}catch (UnknownHostException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
}
