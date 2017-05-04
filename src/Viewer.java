import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

public class Viewer {
	private int sender_id;
	private int msg_id;

	public Viewer() {
		this.sender_id = 0;
		this.msg_id = 0;
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
	
	public void receiveInformation(int sender_id, int msg_id) {
		JsonObject jsonObj = Json.createObjectBuilder().add("type", "get")
				.add("sender_id", sender_id).add("msg_id", msg_id).build();
		String jsonText = jsonObj.toString();
		Socket socket;
		
		try {
			socket = new Socket(InetAddress.getLocalHost(), 8888);
			OutputStreamWriter out = new OutputStreamWriter(
					socket.getOutputStream());
			BufferedWriter wr = new BufferedWriter(out);
			wr.write(jsonText);
			wr.newLine();
			wr.flush();
			
			InputStreamReader in = new InputStreamReader(
					socket.getInputStream());
			BufferedReader rd = new BufferedReader(in);
			String jsonResp = rd.readLine();
			JsonReader jsonReader = Json
					.createReader(new StringReader(jsonResp));
			JsonObject object = jsonReader.readObject();
			JsonObject ack = object.getJsonObject("ack");
			String res = ack.getString("resp");
			if (res.equals("ok")){
				System.out.println("Receive success !\n");
				System.out.println("type :" + object.getString("type") + " "
						+ "msg_id :" + object.getInt("msg_id") + " "
						+ "date :" + object.getInt("date") + " "+"contents :"
						+ object.getJsonObject("contents") + "\n");
			} else {
				int code_res = ack.getInt("error_id");
				System.out.println("Error : " + codeError(code_res));
			}
			jsonReader.close();
			socket.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void receiveLast(int sender_id) {

		JsonObject jsonObj = Json.createObjectBuilder().add("type", "getlast")
				.add("sender_id", sender_id).build();
		String jsonText = jsonObj.toString();
		Socket socket;
		try {
			socket = new Socket(InetAddress.getLocalHost(), 8888);
			OutputStreamWriter out = new OutputStreamWriter(
					socket.getOutputStream());
			BufferedWriter wr = new BufferedWriter(out);
			wr.write(jsonText);
			wr.newLine();
			wr.flush();
			InputStreamReader in = new InputStreamReader(
					socket.getInputStream());
			BufferedReader rd = new BufferedReader(in);
			String jsonResp = rd.readLine();
			JsonReader jsonReader = Json
					.createReader(new StringReader(jsonResp));
			JsonObject object = jsonReader.readObject();
			JsonObject ack = object.getJsonObject("ack");
			String res = ack.getString("resp");
			if (res.equals("ok")){
				System.out.println("Receive success !\n");
				System.out.println("type :" + object.getString("type") + " "
						+ "msg_id :" + object.getInt("msg_id") + " "
						+ "date :" + object.getInt("date") + " "+"contents :"
						+ object.getJsonObject("contents") + "\n");
			} else {
				int code_res = ack.getInt("error_id");
				System.out.println("Error : " + codeError(code_res));
			}
			jsonReader.close();
			socket.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void list(String senderClass, String senderName) {
		Socket socket;
		try {
			socket = new Socket(InetAddress.getLocalHost(), 8888);

			JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
			jsonObjectBuilder.add("type", "list");
			if (!senderClass.equals(""))
				jsonObjectBuilder.add("sender_class", senderClass);
			if (!senderName.equals(""))
				jsonObjectBuilder.add("sender_name", senderName);
			
			JsonObject jsonObj = jsonObjectBuilder.build();
			String jsonText = jsonObj.toString();

			OutputStreamWriter out = new OutputStreamWriter(
					socket.getOutputStream());
			BufferedWriter wr = new BufferedWriter(out);
			wr.write(jsonText);
			wr.newLine();
			wr.flush();

			InputStreamReader in = new InputStreamReader(
					socket.getInputStream());
			BufferedReader rd = new BufferedReader(in);
			String jsonResp = rd.readLine();
			JsonReader jsonReader = Json
					.createReader(new StringReader(jsonResp));
			JsonObject object = jsonReader.readObject();
			System.out.println(object);

			jsonReader.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	String codeError(int code) {
		switch (code) {
		case 400:
			return "Bad Request - Your request sucks";
		case 401:
			return "Unauthorized - Your API key is wrong";
		case 403:
			return "Forbidden - The kitten requested is hidden for administrators only";
		case 404:
			return "Not Found - The specified kitten could not be found";
		case 405:
			return "Method Not Allowed - You tried to access a kitten with an invalid method";
		case 406:
			return "Not Acceptable - You requested a format that isn't json";
		case 410:
			return "Gone - The kitten requested has been removed from our servers";
		case 418:
			return "I'm a teapot";
		case 428:
			return "Deregister failed ";
		case 438:
			return "Send failed";
		case 429:
			return "Too Many Requests - You're requesting too many kittens! Slow down";
		case 500:
			return "Internal Server Error - We had a problem with our server. Try again later";
		case 503:
			return "Service Unavailable - We're temporarily offline for maintenance. Please try again later";
		default:
			return "Code not found";

		}

	}

	public static void main(String[] args) {
		/*Viewer viewer1 = new Viewer(1, 9);
		viewer1.receiveInformation(18, 9);*/
		Viewer viewer2 = new Viewer();
		viewer2.receiveInformation(1, 3);
		viewer2.receiveInformation(1, -1);
		viewer2.receiveInformation(1, 12);
		viewer2.receiveInformation(1, 13);
		viewer2.receiveInformation(1, 17);
		
		viewer2.list("ACC", "");
	}
}
