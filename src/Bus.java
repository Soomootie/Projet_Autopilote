import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

public class Bus {

	Collection<Capteur> list_capteur; // liste de capteurs presents dans le bus
	private int id = 0; // id unique par capteur

	public void registerAccept() {
		list_capteur = new ArrayList<Capteur>();
		JsonObject ack = Json.createObjectBuilder()
				.add("type", "register")
				.add("resp", "ok")
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
			cap.setSender_id(id++); // attribution de l'id puis incrémentation
									// de celui-ci
			list_capteur.add(cap); // ajout du capteur créé ci-avant dans la
									// liste de capteurs
			jsonread.close();
			socket.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void deregisterAccept() {
		Socket socket;
		try {
			socket = new Socket(InetAddress.getLocalHost(), 2002);
			OutputStream out = socket.getOutputStream();
			JsonWriter jswr = Json.createWriter(out);
			
			InputStream in = socket.getInputStream();
			JsonReader jsonread = Json.createReader(in);
			JsonObject jsonObjrd = jsonread.readObject();
			int sender_id = jsonObjrd.getInt("sender_id");
			if (list_capteur.remove(sender_id)){
				System.out.println();
			}
			else {
				/* à finir*/
			}
		}
		
		catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JsonObject reponse = Json.createObjectBuilder() // objet json correspondant à la repponse 
				.add("type", "deregister")
				.add("ack", Json.createObjectBuilder().add("resp", "ok"))
				.build();
		
		
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
			JsonObject jsonObj = jsonread.readObject();
			jsonread.close();
			in.close();
			// Accelerometer acc = new Accelerometer();
			// acc.setSender_class(jsonObj.getString("sender_class"));
			// acc.setSender_name(jsonObj.getString("sender_name"));
			// acc.setX(jsonObj.getInt("x"));
			// acc.setY(jsonObj.getInt("y"));
			// acc.setZ(jsonObj.getInt("z"));
			// System.out.println(acc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
