import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.*;

import javax.json.*;

public class Capteur {
	String sender_class;
	String sender_name;
	int sender_id;
		
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
	
	

	String codeError(int code){
		switch(code){
			case 400 : return "Bad Request – Your request sucks";
			case 401 : return "Unauthorized – Your API key is wrong";
			case 403 : return "Forbidden – The kitten requested is hidden for administrators only";
			case 404 : return "Not Found – The specified kitten could not be found";
			case 405 : return "Method Not Allowed – You tried to access a kitten with an invalid method";
			case 406 : return "Not Acceptable – You requested a format that isn’t json";
			case 410 : return "Gone – The kitten requested has been removed from our servers";
			case 418 : return "I’m a teapot";
			case 428 : return "Deregister failed ";
			case 438 : return "Send failed";
			case 429 : return "Too Many Requests – You’re requesting too many kittens! Slow down";
			case 500 : return "Internal Server Error – We had a problem with our server. Try again later";
			case 503 : return "Service Unavailable – We’re temporarily offline for maintenance. Please try again later";
			default : return "Code not found";
		
		}
		
		
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
			System.out.println(jsonObjrd.getString("resp"));
			jsonread.close();
			socket.close();

		}catch (UnknownHostException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public int deregisterSender(int sender_id){
		JsonObject jsonObj = Json.createObjectBuilder()
				.add("sender_id", sender_id)
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
			JsonObject ack = jsonObjrd.getJsonObject("ack");			
			int res = ack.getInt("resp");
			jsonread.close();
			socket.close();
			if (res == 0){
				System.out.println("Deregister success !");
				return 1;	
			}
			else{
				System.out.println("Error : " + codeError(res));
				return 0;
				
			}	

		}catch (UnknownHostException e) {
			e.printStackTrace();
			return -1;
		}catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		
	}
		
	@Override
	public String toString() {
		return "Capteur [sender_class=" + sender_class + ", sender_name="
			+ sender_name + "]";
	}
}
