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

	public void receiveInformation(){
		
		JsonObject jsonObj = Json.createObjectBuilder()
				.add("sender_id", sender_id)
				.add("msg_id", msg_id)
				.build();
		Socket socket;
		try {
			socket = new Socket(InetAddress.getLocalHost(), 8888);
			OutputStream out = socket.getOutputStream();
			JsonWriter jswr = Json.createWriter(out);
			jswr.writeObject(jsonObj);
			InputStream in = socket.getInputStream();
			JsonReader jsonread = Json.createReader(in);
			JsonObject jsonObjrd = jsonread.readObject();
			JsonObject ack = jsonObjrd.getJsonObject("ack");			
			int res = ack.getInt("resp");
			if (res == 0){
				System.out.println("Receive success !\n");
				System.out.println("type :"+jsonObjrd.getString("type")+"\n");
				System.out.println("ack :"+jsonObjrd.getJsonObject("ack")+"\n");
				System.out.println("msg_id :"+jsonObjrd.getJsonObject("msg_id")+"\n");
				System.out.println("date :"+jsonObjrd.getInt("date")+"\n");
				System.out.println("contents :"+jsonObjrd.getJsonObject("contents")+"\n");
			}
			else{
				System.out.println("Error : " + codeError(res));
				
			}	
			jsonread.close();
			socket.close();

		}catch (UnknownHostException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
public void receiveLast(){
		
		JsonObject jsonObj = Json.createObjectBuilder()
				.add("sender_id", sender_id) 
				.build();
		Socket socket;
		try {
			socket = new Socket(InetAddress.getLocalHost(), 8888);
			OutputStream out = socket.getOutputStream();
			JsonWriter jswr = Json.createWriter(out);
			jswr.writeObject(jsonObj);
			InputStream in = socket.getInputStream();
			JsonReader jsonread = Json.createReader(in);
			JsonObject jsonObjrd = jsonread.readObject();
			JsonObject ack = jsonObjrd.getJsonObject("ack");			
			int res = ack.getInt("resp");
			if (res == 0){
				System.out.println("Receive success !\n");
				System.out.println("type :"+jsonObjrd.getString("type")+"\n");
				System.out.println("ack :"+jsonObjrd.getJsonObject("ack")+"\n");
				System.out.println("msg_id :"+jsonObjrd.getJsonObject("msg_id")+"\n");
				System.out.println("date :"+jsonObjrd.getInt("date")+"\n");
				System.out.println("contents :"+jsonObjrd.getJsonObject("contents")+"\n");
			}
			else{
				System.out.println("Error : " + codeError(res));
				
			}	
			jsonread.close();
			socket.close();

		}catch (UnknownHostException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}


	
	String codeError(int code){
		switch(code){
			case 400 : return "Bad Request - Your request sucks";
			case 401 : return "Unauthorized - Your API key is wrong";
			case 403 : return "Forbidden - The kitten requested is hidden for administrators only";
			case 404 : return "Not Found - The specified kitten could not be found";
			case 405 : return "Method Not Allowed - You tried to access a kitten with an invalid method";
			case 406 : return "Not Acceptable - You requested a format that isn't json";
			case 410 : return "Gone - The kitten requested has been removed from our servers";
			case 418 : return "I'm a teapot";
			case 428 : return "Deregister failed ";
			case 438 : return "Send failed";
			case 429 : return "Too Many Requests - You're requesting too many kittens! Slow down";
			case 500 : return "Internal Server Error - We had a problem with our server. Try again later";
			case 503 : return "Service Unavailable - We're temporarily offline for maintenance. Please try again later";
			default : return "Code not found";
		
		}
		
		
	}	
	
	public static void main(String[] args) {
		VisualisateurAcc acc = new VisualisateurAcc(2, 9);
		acc.receiveInformation();
	}
	
	
}
