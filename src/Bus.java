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

public class Bus {

	Collection<Capteur> list_capteur; // liste de capteurs presents dans le bus
	//Collection<JsonObject> list_jsonObject; // liste de message json presents dans le bus
	private static int MAXTAILLE = 150;
	int idtab =0;
	private SenderbyCapteur []tabmsgId = new SenderbyCapteur[MAXTAILLE]; //tableau des messages dans le bus
	//private static int id_msg =0;
	//Collection<JsonObject> list_jsonObject; // liste de message json presents dans le bus
	private int id = 0; // id unique par capteur , incremente de 1 a chaque nouvelle connexion 

	
	// parcourt la liste list_capteur et ajoute les caracteristiques de chaque capteurs present
	// dans la liste a l'objet result
	public JsonObject list_capteurs (String type, String data, int id){
		JsonObject result = Json.createObjectBuilder().build();
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
			if ( (type.equals("sender_class") && data.equals(sender_class)) 
					|| (type.equals("sender_name") && data.equals(sender_name)) 
					|| (type.equals("sender_id") && id == sender_id)
					|| (type.equals("") && data.equals("") && id == -1))
					result = merge(result, jsonTmp);	
		}
		return result;
	}
	
	public void list(){ // list all capteur in bus
		Socket socket;
		String s_class = "";
		String s_name = "";
		int s_id = -1;
		JsonObject ack = Json.createObjectBuilder()
				.add("type", "list")
				.add("ack", Json.createObjectBuilder().add("resp","ok")) // changer le resp (ici action par default)
				.build();
		
		try{
			socket = new Socket(InetAddress.getLocalHost(), 2020);
			OutputStream out = socket.getOutputStream();
			InputStream in = socket.getInputStream();
			JsonReader jsonread = Json.createReader(in);
			JsonObject jsonObjrd = jsonread.readObject();
			try { // try catch si la chiane de caractere recherchee dans l'objet n'est pas trouvee
				s_class = jsonObjrd.getString("sender_class");
			} catch (NullPointerException e) {}
			try{
				s_name = jsonObjrd.getString("sender_name");
			} catch (NullPointerException e) {}
			try{
				s_id = jsonObjrd.getInt("sender_id");	
			} catch (NullPointerException e) {}
			JsonObject list;
			if (! s_class.equals("")){ // on teste si la chaine a bien ete trouvee
				list = list_capteurs("sender_class", s_class , -1);
			} else if ( ! s_name.equals("")){ // idem
				list = list_capteurs("sender_name", s_name , -1);
			} else if ( s_id != -1){  // idem
				list = list_capteurs("sender_id", "" , s_id);
			} else { // cas par default on renvoie tous capteurs presents dans list_capteur
				list = list_capteurs("", "", -1);
			}
			list = merge (ack, list);
			JsonWriter jswr = Json.createWriter(out);
			jswr.writeObject(list);
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
			socket = new Socket(InetAddress.getLocalHost(), 2020);
			OutputStream out = socket.getOutputStream();
			JsonWriter jswr = Json.createWriter(out);
			jswr.writeObject(ack); // envoie le message ack
			InputStream in = socket.getInputStream();
			JsonReader jsonread = Json.createReader(in);
			JsonObject jsonObjrd = jsonread.readObject();
			String name = jsonObjrd.getString("name"); // lecture du nom provenant du capteur
			String type = jsonObjrd.getString("class"); // lecture de la classe provenant du capteur
			Capteur cap = new Capteur(name, type); // creation d'un capteur
			cap.setSender_id(id++); // attribution de l'id puis incrementation de celui-ci
			list_capteur.add(cap); // ajout du capteur cree ci-avant dans la liste de capteurs
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
			socket = new Socket(InetAddress.getLocalHost(), 2020); // changer numero de port
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
			socket = new Socket(InetAddress.getLocalHost(), 2020);
			OutputStream out = socket.getOutputStream();
			JsonWriter jswr = Json.createWriter(out);
			InputStream in = socket.getInputStream();
			JsonReader jsonread = Json.createReader(in);
			JsonObject jsonObjrd = jsonread.readObject();
			int sendid =jsonObjrd.getInt("sender_id");
			if (list_capteur.contains(sendid)){ // verification existance de l'id
				tabmsgId[idtab].setSender_id(sendid); //stocke l'id capteur
				tabmsgId[idtab].setTabid(jsonObjrd.getJsonObject("msg")); //stocke le message
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
			socket = new Socket(InetAddress.getLocalHost(), 2020);
			OutputStream out = socket.getOutputStream();
			JsonWriter jswr = Json.createWriter(out);
			InputStream in = socket.getInputStream();
			JsonReader jsonread = Json.createReader(in);
			JsonObject jsonObjrd = jsonread.readObject();
			int msgid = jsonObjrd.getInt("msg_id");
			SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss"); // recuperation de la date en milliseconde
			String dateInString = sdf.format(new Date());
			Date datepars = sdf.parse(dateInString);
			int date = (int)datepars.getTime();
			if (list_capteur.contains(jsonObjrd.getInt("sender_id"))){ // verification existance de l'id dans la liste
				if(msgid>=0 && msgid<MAXTAILLE){
					int indice = indexcapt(jsonObjrd.getInt("sender_id")); //indice du capteur dans le tableau capteur-message
					if(msgid < tabmsgId[indice].getmsgid()){ //verifie si msgid inferieur a  l'id le plus ancien
					JsonObject msg = tabmsgId[indice].getTabid(0); //message le plus ancien dans la table des messages
					JsonObject repmsg = Json.createObjectBuilder()
							.add("type","get")
							.add("ack", Json.createObjectBuilder().add("resp", 0))
							.add("date",date)
							.add("msg_id", msgid)
							.add("contents",msg)
							.build();
					jswr.writeObject(repmsg);
					}
					else if(msgid>tabmsgId[indice].getmsgid()){ //verifie si msgid superieur a  l'id le plus ancien
						JsonObject msg = tabmsgId[indice].getTabid(tabmsgId[indice].getmsgid()-1); // message le plus recent
						JsonObject repmsg = Json.createObjectBuilder()
								.add("type","get")
								.add("ack", Json.createObjectBuilder().add("resp", 0))
								.add("date",date)
								.add("msg_id", msgid)
								.add("contents",msg)
								.build();
						jswr.writeObject(repmsg); 
					}
					else if(tabmsgId[indice].getTabid(0) == null){ //verifie si le tableau des messages est vide
						JsonObject reponse = Json.createObjectBuilder()
								.add("type", "get")
								.add("ack", Json.createObjectBuilder().add("resp",404))
								.build();
						jswr.writeObject(reponse);
					}
					else{
					JsonObject msg = tabmsgId[indice].getTabid(msgid);//recupere le message souhaite
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
			e.printStackTrace();
		}
		
	}
	
	public void getLast(){
		Socket socket;
		try {
			socket = new Socket(InetAddress.getLocalHost(), 2020);
			OutputStream out = socket.getOutputStream();
			JsonWriter jswr = Json.createWriter(out);
			InputStream in = socket.getInputStream();
			JsonReader jsonread = Json.createReader(in);
			JsonObject jsonObjrd = jsonread.readObject();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");  //recuperation de la date en milliseconde
			String dateInString = sdf.format(new Date());
			Date datepars = sdf.parse(dateInString);
			int date = (int)datepars.getTime();
			if (list_capteur.contains(jsonObjrd.getInt("sender_id"))){ // verification existance de l'id dans la liste
					int indice = indexcapt(jsonObjrd.getInt("sender_id")); //indice du capteur dans le tableau capteur-message
					JsonObject msg = tabmsgId[indice].getTabid(tabmsgId[indice].getmsgid());//recupere le message demande d'un capteur
					int msgid = tabmsgId[indice].getmsgid();
					JsonObject repmsg = Json.createObjectBuilder()
							.add("type","get")
							.add("ack", Json.createObjectBuilder().add("resp", 0))
							.add("date",date)
							.add("msg_id", msgid)
							.add("contents",msg)
							.build();
					jswr.writeObject(repmsg);
					}
				
			else {
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
			//JsonObject jsonObj = jsonread.readObject();  // ne sert a rien pour le moment.
			jsonread.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
