package autopiltote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;

public class Bus {

	Collection<Capteur> list_capteur; // liste de capteurs presents dans le bus
	Collection<JsonObject> list_jsonObject; // liste de message json presents dans le bus
	private int id = 0; // id unique par capteur , incrémenté de 1 à chaque nouvelle connexion 

	public void list(){ // list all capteur in bus
		
		JsonObject ack = Json.createObjectBuilder()
				.add("type", "list")
				.add("ack", Json.createObjectBuilder().add("resp","ok")) // changer le resp (ici action par default)
				.build();
				
		JsonObject result = Json.createObjectBuilder().build();
		// parcourt la liste list_capteur et ajoute les caracteristiques de chaque capteurs present
		// dans le liste à l'objet result 
		for (Iterator<Capteur> iterator = list_capteur.iterator(); iterator.hasNext();) {
			Capteur capteur = (Capteur) iterator.next();
			String sender_class = capteur.getSender_class();
			String sender_name = capteur.getSender_name();
			int sender_id = capteur.getSender_id();
			JsonObject jsonTmp = Json.createObjectBuilder()
					.add("sender_id", sender_id)
					.add("sender_class", sender_class)
					.add("sender_name", sender_name)
					.build();
			result = merge(result, jsonTmp);
		}
		
		result = merge(ack, result); 
		Socket socket;
		try{
			socket = new Socket(InetAddress.getLocalHost(), 2002);
			OutputStream out = socket.getOutputStream();
			JsonWriter jswr = Json.createWriter(out);
			jswr.writeObject(result);
		}
		catch (UnknownHostException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void checkIn() { // enregistrement cote bus
		list_capteur = new ArrayList<Capteur>();
		JsonObject ack = Json.createObjectBuilder()
				.add("type", "register")
				.add("ack", Json.createObjectBuilder().add("resp", "ok"))
				.build();
		Socket socket;
		try {
			socket = new Socket(InetAddress.getLocalHost(), 2002);
			OutputStream out = socket.getOutputStream();
			JsonWriter jswr = Json.createWriter(out);
			jswr.writeObject(ack); // envoie le message ack
			InputStream in = socket.getInputStream();
			JsonReader jsonread = Json.createReader(in);
			JsonObject jsonObjrd = jsonread.readObject();
			String name = jsonObjrd.getString("name"); // lecture du nom
			// provenant du capteur
			String type = jsonObjrd.getString("class"); // lecture de la classe
			// provenant du capteur
			Capteur cap = new Capteur(name, type); // creation d'un capteur
			cap.setSender_id(id++); // attribution de l'id puis incrÃ©mentation
			// de celui-ci
			list_capteur.add(cap); // ajout du capteur crÃ©Ã© ci-avant dans la
			// liste de capteurs
			jsonread.close();
			socket.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void checkOut() { // desenregistrement cote bus
		Socket socket;
		try {
			socket = new Socket(InetAddress.getLocalHost(), 2002); // changer numero de port
			OutputStream out = socket.getOutputStream();
			JsonWriter jswr = Json.createWriter(out);
			InputStream in = socket.getInputStream();
			JsonReader jsonread = Json.createReader(in);
			JsonObject jsonObjrd = jsonread.readObject();
			int sender_id = jsonObjrd.getInt("sender_id");
			// if "sender_id" is in list_capteur , is remove
			if (list_capteur.remove(sender_id)){ 
				JsonObject reponse = Json.createObjectBuilder()
						.add("type", "deregister")
						.add("ack", Json.createObjectBuilder().add("resp", 0))
						.build();
				jswr.writeObject(reponse);
			}
			else { // if "sender_id" isn't in list_capteur , an error code is send
				JsonObject reponse = Json.createObjectBuilder()
						.add("type", "deregister")
						.add("ack", Json.createObjectBuilder().add("resp",428))
						.build();
				jswr.writeObject(reponse);
			}
		}

		catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public void ackSend() { // ack send 
		Socket socket;
		try {
			socket = new Socket(InetAddress.getLocalHost(), 2002);
			OutputStream out = socket.getOutputStream();
			JsonWriter jswr = Json.createWriter(out);
			InputStream in = socket.getInputStream();
			JsonReader jsonread = Json.createReader(in);
			JsonObject jsonObjrd = jsonread.readObject();
			if (list_capteur.contains(jsonObjrd.getInt("sender_id"))){ // verification existance de l'id
				list_jsonObject.add(jsonObjrd);
				JsonObject  reponse = Json.createObjectBuilder()
						.add("type", "send")
						.add("ack", Json.createObjectBuilder().add("resp", 0))
						.build();
				jswr.writeObject(reponse);
			} else { // if "sender_id" is'nt know , return an error code
				JsonObject reponse = Json.createObjectBuilder()
						.add("type", "send")
						.add("ack", Json.createObjectBuilder().add("resp",438))
						.build();
				jswr.writeObject(reponse);
			}
		}

		catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// concatenate two JsonObject
	public static JsonObject merge(JsonObject oldJsonObject, JsonObject newJsonObject) {
		JsonObjectBuilder jsonObjectBuilder =Json.createObjectBuilder();

		for (String key : oldJsonObject.keySet()){
			jsonObjectBuilder.add(key, oldJsonObject.get(key));
		}
		for (String key : newJsonObject.keySet()){
			jsonObjectBuilder.add(key, newJsonObject.get(key));
		}

		return jsonObjectBuilder.build();
	}

	public static void main(String[] args) {
		ServerSocket socketserver;
		Socket socketduserveur;
		try {
			socketserver = new ServerSocket(Integer.parseInt(args[0]));
			socketduserveur = socketserver.accept();
			InputStream in = socketduserveur.getInputStream();
			// BufferedReader read = new BufferedReader(new
			// InputStreamReader(in));
			JsonReader jsonread = Json.createReader(in);
			//JsonObject jsonObj = jsonread.readObject();  // ne sert à rien pour le moment.
			jsonread.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
