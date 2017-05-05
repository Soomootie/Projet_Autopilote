import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
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
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;

public class Bus {

	private Collection<Capteur> list_capteur; 	/* Liste de capteurs presents dans le Bus */
	private static int MAXTAILLE = 150; 	 			/* Taille maximum autorisée					  */
	int idtab = 0;													/* Indice du tableau								  */
	private SenderbyCapteur []tabmsgId;			/* Tableau des messages dans le Bus 	  */
	private int id = 0;											/* Id unique par capteur , incremente de
	 * 1 a chaque nouvelle connexion			  */ 

	public Bus() {
		super();
		this.tabmsgId = new SenderbyCapteur[MAXTAILLE];
		for (int i = 0; i < MAXTAILLE; i++) {
			this.tabmsgId[i] = new SenderbyCapteur();
		}
		this.list_capteur = new ArrayList<Capteur>();
	}

	/**
	 * Parcourt la liste "list_capteur" et ajoute les caracteristiques de chaque capteurs
	 * present dans la liste a l'objet JsonArray result.
	 * @param type, un String.
	 * @param data, un String.
	 * @return JsonArray.
	 */
	public JsonArray list_capteurs (String type, String data){
		JsonArray result = Json.createArrayBuilder().build();
		for (Iterator<Capteur> iterator = list_capteur.iterator(); iterator.hasNext();) {
			Capteur capteur = (Capteur) iterator.next();
			System.out.println("Capteur " + capteur);
			String senderClass = capteur.getSender_class();
			String senderName = capteur.getSender_name();
			int senderId = capteur.getSender_id();
			JsonObject jsonTmp = Json.createObjectBuilder()
					.add("sender_id", senderId)
					.add("sender_class", senderClass)
					.add("sender_name", senderName)
					.build();
			/* On teste si les strings passees en parametre 
			 * correspondent a un champ du capteur. */
			if ( (type.equals("sender_class") && data.equals(senderClass)) 
					|| (type.equals("sender_name") && data.equals(senderName))
					|| (type.equals("sender_id") && data.equals(Integer.toString(senderId)))
					|| (type.equals("") && data.equals("")))
				result = merge(result , jsonTmp);	
		}
		return result;
	}

	/**
	 * Fonction retournant un tableau contenant le tableau passe en 
	 * parametre ainsi qu'un JsonObject.
	 * @param oldJsonArray, un JsonArray..
	 * @param newJsonObject, un JsonObject..
	 * @return JsonArray, le tableau concatene.
	 */
	public static JsonArray merge(JsonArray oldJsonArray, JsonObject newJsonObject) {
		JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
		jsonArrayBuilder.add(newJsonObject);
		if ( ! oldJsonArray.isEmpty() ){
			for (Iterator<JsonValue> iterator = oldJsonArray.iterator() ; iterator.hasNext() ; ){
				JsonValue jsonValue = (JsonValue) iterator.next();
				jsonArrayBuilder.add(jsonValue);
			}
		}
		return jsonArrayBuilder.build();
	}

	/**
	 * Verifie si l'entier id est un id present dans la liste de capteur (list_capteur)
	 * @param id, un int.
	 * @return true si l'id est present , false sinon.
	 */
	public boolean is_in(int id){
		for (Iterator<Capteur> iterator = list_capteur.iterator() ; iterator.hasNext() ; ) {
			Capteur capteur = (Capteur) iterator.next();
			int sender_id = capteur.getSender_id();
			if ( sender_id == id )
				return true;
		}
		return false;
	}

	/**
	 * Verifie si l'entier id est un id present dans la list_capteur (list_capteur)
	 * et le supprime si il est present
	 * @param id , un int.
	 * @return true si l'id present a ete supprime, false sinon.
	 */
	public boolean is_suppr(int id){
		for (Iterator<Capteur> iterator = list_capteur.iterator() ; iterator.hasNext() ; ) {
			Capteur capteur = (Capteur) iterator.next();
			int sender_id = capteur.getSender_id();
			if ( sender_id == id )
				if ( list_capteur.remove(capteur) )
					return true;
		}
		return false;
	}

	/**
	 * Liste tous les capteurs presents dans le bus
	 * @param object, un JsonObject.
	 * @param socket, une socket.
	 */
	public void list(JsonObject object, Socket socket){
		String s_class = "";
		String s_name = "";
		JsonArray list;
		JsonObjectBuilder ack = Json.createObjectBuilder();
		ack.add("type", "list");
		ack.add("ack", Json.createObjectBuilder().add("resp", "ok")); 	// changer le resp (ici action par default)

		try{
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
			BufferedWriter wr = new BufferedWriter(out);
			
			try { /* "try catch" si la chaine de caractere recherchee dans l'objet n'est pas trouvee */
				s_class = object.getString("sender_class");
			} catch (NullPointerException e) {}
			try{ /* Idem																													*/
				s_name = object.getString("sender_name");
			} catch (NullPointerException e) {}
			
			if ( ! s_class.equals("") ){  /* On teste si la chaine a bien ete trouvee							*/
				list = list_capteurs("sender_class", s_class);
			} else if ( ! s_name.equals("") ){ /* idem																		*/
				list = list_capteurs("sender_name", s_name);
			} else { /* Cas par default on renvoie tous capteurs presents dans list_capteur			*/
				list = list_capteurs("", "");
			}
			ack.add("results", list);

			String jsonText = ack.build().toString();
			wr.write(jsonText);
			wr.newLine();
			wr.flush();

		}
		catch (UnknownHostException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fonction d'enregistrement d'un capteur dans le bus.
	 * @param object , un JsonObject.
	 * @param socket, une socket.
	 */
	public void checkIn(JsonObject object, Socket socket) {
		JsonObjectBuilder ack = Json.createObjectBuilder();
		ack.add("type", "register");
		ack.add("sender_id", ++id);

		try {
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
			BufferedWriter wr = new BufferedWriter(out);

			String name = object.getString("name"); /* Lecture du nom provenant du capteur  	*/
			String type = object.getString("class"); /*  Lecture de la classe provenant du capteur */
			Capteur cap = new Capteur(type, name); /* Creation d'un capteur								*/
			cap.setSender_id(id); /* Attribution de l'id puis incrementation de celui-ci					*/
			if ( list_capteur.add(cap) ){ /* Ajout du capteur cree ci-avant dans la liste de capteurs	*/
				ack.add("ack", Json.createObjectBuilder().add("resp", "ok"));
				wr.write(ack.build().toString()); /* Envoie le message ack											*/
				wr.newLine();
				wr.flush();
			} else { /* Si le capteur n'a pas ete ajoute a la liste 															*/
				ack.add("ack", Json.createObjectBuilder().add("resp", "error").add("error_id", 430));
				wr.write(ack.build().toString());
				wr.newLine();
				wr.flush();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fonction de desenregistrement d'un capteur dans le bus.
	 * @param object , un JsonObject.
	 * @param socket, une socket.
	 */
	public void checkOut(JsonObject object , Socket socket) {
		JsonObjectBuilder reponse = Json.createObjectBuilder();
		reponse.add("type", "deregister");
		try {
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
			BufferedWriter wr = new BufferedWriter(out);

			int sender_id = object.getInt("sender_id");
			if ( is_suppr(sender_id) ){ 
				reponse.add("ack", Json.createObjectBuilder().add("resp", "ok"));
				wr.write(reponse.build().toString());
				wr.newLine();
				wr.flush();
			}
			else { /* Si "sender_id" n'est pas connu , renvoie un code d'erreur.			*/
				reponse.add("ack", Json.createObjectBuilder().add("resp", "error").add("error_id", 428));
				wr.write(reponse.build().toString());
				wr.newLine();
				wr.flush();
			}
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Envoie un certain ack en fonction de l'id.
	 * @param object , un JsonObject.
	 * @param socket, une socket.
	 */
	
	 
		public boolean foundTab(int sender_id){
			for(int i = 0 ; i < MAXTAILLE ; i++)
				if( sender_id == tabmsgId[i].getSender_id() )
					return true;
			 return false;
		}
	
	public void ackSend(JsonObject object, Socket socket) {
		JsonObjectBuilder reponse = Json.createObjectBuilder();
		reponse.add("type", "send");
		try {
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
			BufferedWriter wr = new BufferedWriter(out);

			int sendid =object.getInt("sender_id");
			if ( is_in(sendid) ){ /* Verification existance de l'id															*/
				reponse.add("ack", Json.createObjectBuilder().add("resp", "ok"));
				wr.write(reponse.build().toString());
				wr.newLine();
				wr.flush();
				if(!foundTab(sendid)){ /*le capteur en est a son premier message  */
				tabmsgId[idtab].setSender_id(sendid); /* Stocke l'id capteur										*/
				tabmsgId[idtab].setTabid(object.getJsonObject("contents")); /* Stocke le message	*/
				idtab++;}
				else{ /* le capteur a deja envoye au moins une fois */
					int index = indexcapt(sendid) ;
					tabmsgId[index].setTabid(object.getJsonObject("contents")); /* Stocke le message	*/
					
				}

			} else { /* Si "sender_id" n'est pas connu , revoie un code d'erreur.									*/ 
						reponse.add("ack", Json.createObjectBuilder().add("resp", "error").add("error_id", 438));
				wr.write(reponse.build().toString());
				wr.newLine();
				wr.flush();
			}
		}	
		catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retourne un indice du tableau "tabmsgl" si
	 * sender_id est present dans ce dernier.
	 * @param sender_id, un int.
	 * @return int .
	 */
	public int indexcapt(int sender_id){
		for(int i = 0 ; i < MAXTAILLE ; i++)
			if( sender_id == tabmsgId[i].getSender_id() )
				return i;
		return 0;
	}

	/**
	 * Envoie un certain messages au client.
	 * @param object, un JsonObject.
	 * @param socket, une socket.
	 */
	public void getInformation(JsonObject object, Socket socket) {
		JsonObjectBuilder repmsg = Json.createObjectBuilder();
		repmsg.add("type", "get");
		try {
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
			BufferedWriter wr = new BufferedWriter(out);

			int msgid = object.getInt("msg_id");
			SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss"); /* Date en milliseconde		*/
			String dateInString = sdf.format(new Date());
			Date datepars = sdf.parse(dateInString);
			int date = (int) datepars.getTime();
			if ( is_in(object.getInt("sender_id")) ){ /* Verification existance de l'id dans la liste									*/
				if(msgid < MAXTAILLE ){
					int indice = indexcapt(object.getInt("sender_id"));
					if(tabmsgId[indice].getTabid(0) == null ){  /*Verifie si le tableau des messages est vide*/			
						repmsg.add("ack", Json.createObjectBuilder().add("resp", "error").add("error_id", 404));
						wr.write(repmsg.build().toString());
						wr.newLine();
						wr.flush();
					}	
					else{	/*table de message non vide */
					 /* Indice du capteur dans le tableau capteur-message							*/
					if( msgid < 0 ){ /* Verifie si msgid inferieur a l'id le plus ancien				*/
						JsonObject msg = tabmsgId[indice].getTabid(0); /* Le plus ancien messages								*/
						repmsg.add("ack", Json.createObjectBuilder().add("resp", "ok"));
						repmsg.add("date", date);
						repmsg.add("msg_id", msgid);
						repmsg.add("contents", msg);
						wr.write(repmsg.build().toString());
						wr.newLine();
						wr.flush();
					}
					if( msgid >= tabmsgId[indice].getmsgid() ){ /* Verifie si msgid superieur a l'id le plus ancien	*/
						JsonObject msg = tabmsgId[indice].getTabid(tabmsgId[indice].getmsgid() - 1); /* Message le plus recent */
						repmsg.add("ack", Json.createObjectBuilder().add("resp", "ok"));
						repmsg.add("date", date);
						repmsg.add("msg_id", msgid);
						repmsg.add("contents", msg);
						wr.write(repmsg.build().toString());
						wr.newLine();
						wr.flush();
					}
					
					if(msgid >= 0 && msgid <= tabmsgId[indice].getmsgid()){
						JsonObject msg = tabmsgId[indice].getTabid(msgid); /* Recupere le message souhaite				*/
						repmsg.add("ack", Json.createObjectBuilder().add("resp", "ok"));
						repmsg.add("date", date);
						repmsg.add("msg_id", msgid);
						repmsg.add("contents", msg);
						wr.write(repmsg.build().toString());
						wr.newLine();
						wr.flush();
					}
					}	
			} else {
				repmsg.add("ack", Json.createObjectBuilder().add("resp", "error").add("error_id", 404));
				wr.write(repmsg.build().toString());
				wr.newLine();
				wr.flush();	
			}
			
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

	/**
	 * Envoie au client , le dernier message recu par le bus.
	 * @param object, un JsonObject.
	 * @param socket, une socket.
	 */
	public void getLast(JsonObject object,Socket socket){
		JsonObjectBuilder repmsg = Json.createObjectBuilder();
		repmsg.add("type", "get");
		try {
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
			BufferedWriter wr = new BufferedWriter(out);

			SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");  /* Date en milliseconde				*/
			String dateInString = sdf.format(new Date());
			Date datepars = sdf.parse(dateInString);
			int date = (int) datepars.getTime();
			if ( is_in(object.getInt("sender_id")) ){ /* Verification existance de l'id dans la liste												*/
				int indice = indexcapt(object.getInt("sender_id")); /* Indice du capteur dans le tableau capteur-message 	*/
				int msgid_last = tabmsgId[indice].getmsgid() - 1;
				System.out.println("dernier id " + msgid_last);
				JsonObject msg = tabmsgId[indice].getTabid(msgid_last); /* Recupere le dernier message d'un capteur		*/
				repmsg.add("ack", Json.createObjectBuilder().add("resp", "ok"));
				repmsg.add("date", date);
				repmsg.add("msg_id", msgid_last);
				repmsg.add("contents", msg);
				wr.write(repmsg.build().toString());
				wr.newLine();
				wr.flush();
			}
			else {
				repmsg.add("ack", Json.createObjectBuilder().add("resp", "error").add("error_id", 404));
				wr.write(repmsg.build().toString());
				wr.newLine();
				wr.flush();
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
		Bus bus = new Bus();
		while (true){
			try {

				socketserver = new ServerSocket(Integer.parseInt(args[0]));
				socketduserveur = socketserver.accept();

				InputStreamReader in = new InputStreamReader(socketduserveur.getInputStream());
				BufferedReader rd = new BufferedReader(in);

				String jsonResp = rd.readLine();
				System.out.println("requete recue : " + jsonResp);

				try {
					JsonReader jsonReader = Json.createReader(new StringReader(jsonResp));
					JsonObject object = jsonReader.readObject();
					jsonResp = object.getString("type");
					if ( jsonResp.equals("register") )
						bus.checkIn(object, socketduserveur);
					if ( jsonResp.equals("send") )
						bus.ackSend(object , socketduserveur);
					if ( jsonResp.equals("deregister") )
						bus.checkOut(object, socketduserveur);
					if ( jsonResp.equals("list") )
						bus.list(object, socketduserveur);
					jsonReader.close();
					if(jsonResp.equals("get"))
						bus.getInformation(object, socketduserveur);
					if(jsonResp.equals("getlast"))
						bus.getLast(object, socketduserveur);

				} catch (NullPointerException e) {}
				in.close();
				socketserver.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
} 
