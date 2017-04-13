import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class Bus {

	Collection<Capteur> list_capteur; // liste de capteurs presents dans le bus
	//Collection<JsonObject> list_jsonObject; // liste de message json presents dans le bus
	private static int MAXTAILLE = 150;int idtab =0;
	private SenderbyCapteur []tabmsgId = new SenderbyCapteur[MAXTAILLE]; //tableau des messages dans le bus
	private static int id_msg =0;
	Collection<JsonObject> list_jsonObject; // liste de message json presents dans le bus
	private int id = 0; // id unique par capteur , incr�ment� de 1 � chaque nouvelle connexion 

	public void list(){ // list all capteur in bus
		
		JsonObject ack = Json.createObjectBuilder()
				.add("type", "list")
				.add("ack", Json.createObjectBuilder().add("resp","ok")) // changer le resp (ici action par default)
				.build();
				
		JsonObject result = Json.createObjectBuilder().build();
		// parcourt la liste list_capteur et ajoute les caracteristiques de chaque capteurs present
		// dans le liste � l'objet result 
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
			int sendid =jsonObjrd.getInt("sender_id");
			if (list_capteur.contains(sendid)){ // verification existance de l'id
				tabmsgId[idtab].setSender_id(sendid); //stock l'id capteur
				tabmsgId[idtab].setTabid(jsonObjrd.getJsonObject("msg")); //stock le message
				idtab++;
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

	public int indexcapt(int sender_id){
		for(int i=0;i<MAXTAILLE;i++){
			if(sender_id == tabmsgId[i].getSender_id())
				return i;
			
		}
		return 0;
		
	}
	
	
	public void getInformation() { // Envoie des messages au client 
		Socket socket;
		try {
			socket = new Socket(InetAddress.getLocalHost(), 2002);
			OutputStream out = socket.getOutputStream();
			JsonWriter jswr = Json.createWriter(out);
			InputStream in = socket.getInputStream();
			JsonReader jsonread = Json.createReader(in);
			JsonObject jsonObjrd = jsonread.readObject();
			int msgid = jsonObjrd.getInt("msg_id");
			if (list_capteur.contains(jsonObjrd.getInt("sender_id"))){ // verification existance de l'id dans la liste
				if(msgid>=0 && msgid<MAXTAILLE){
					int indice = indexcapt(jsonObjrd.getInt("sender_id")); //indice du capteur dans le tableau capteur-message
					if(msgid < tabmsgId[indice].getmsgid()){ //verifie si msgid inferieur à l'id le plus ancien
					JsonObject msg = tabmsgId[indice].getTabid(0); //message le plus
					}
					else if(msgid>tabmsgId[indice].getmsgid()){ //verifie si msgid superieur à l'id le plus ancien
						JsonObject msg = tabmsgId[indice].getTabid(0);
					}
					else if(tabmsgId[indice].getTabid(0) == null){ //verifie si le tableau des messages est vide
						JsonObject reponse = Json.createObjectBuilder()
								.add("type", "get")
								.add("ack", Json.createObjectBuilder().add("resp",404))
								.build();
						jswr.writeObject(reponse);
					}
					else{
					JsonObject msg = tabmsgId[indice].getTabid(msgid);//recupere le message souhaité
					SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss"); /* recuperation de la date en milliseconde*/
					String dateInString = sdf.format(new Date());
					Date datepars = sdf.parse(dateInString);
					double date = datepars.getTime();
					JsonObject repmsg = Json.createObjectBuilder()
							.add("type","get")
							.add("ack", Json.createObjectBuilder().add("resp", 0))
							.add("date",date)
							.add("msg_id", msgid)
							.add("contents",msg)
							.build();
					jswr.writeObject(repmsg);
					}
				}
			} else {
				JsonObject reponse = Json.createObjectBuilder()
						.add("type", "get")
						.add("ack", Json.createObjectBuilder().add("resp",404))
						.build();
				jswr.writeObject(reponse);
			}
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
			//JsonObject jsonObj = jsonread.readObject();  // ne sert � rien pour le moment.
			jsonread.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
