import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
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
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;

public class Bus {

	private Collection<Capteur> list_capteur; // liste de capteurs presents dans le bus
	//Collection<JsonObject> list_jsonObject; // liste de message json presents dans le bus
	private static int MAXTAILLE = 150;
	int idtab =0;
	private SenderbyCapteur []tabmsgId; //tableau des messages dans le bus
	//private static int id_msg =0;
	//Collection<JsonObject> list_jsonObject; // liste de message json presents dans le bus
	private int id = 0; // id unique par capteur , incremente de 1 a chaque nouvelle connexion 

	public Bus() {
		super();
		this.tabmsgId = new SenderbyCapteur[MAXTAILLE];
		this.list_capteur = new ArrayList<Capteur>();
	}


	// parcourt la liste list_capteur et ajoute les caracteristiques de chaque capteurs present
	// dans la liste a l'objet result
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
			System.out.println(senderClass + "\t" + senderName + "\t" + senderId);
			if ( (type.equals("sender_class") && data.equals(senderClass)) 
					|| (type.equals("sender_name") && data.equals(senderName))
					|| (type.equals("sender_id") && data.equals(Integer.toString(senderId)))
					|| (type.equals("") && data.equals(""))){
				result = merge(result , jsonTmp);	
			}
		}
		System.out.println(result);
		return result;
	}
	
	// concatenate two JsonObject
	public static JsonArray merge(JsonArray oldJsonArray, JsonObject newJsonObject) {
	JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
		jsonArrayBuilder.add(newJsonObject);
		if (!oldJsonArray.isEmpty()){
			for (Iterator<JsonValue> iterator = oldJsonArray.iterator() ; iterator.hasNext() ; ){
				JsonValue jsonValue = (JsonValue) iterator.next();
				jsonArrayBuilder.add(jsonValue);
			}
		}
		return jsonArrayBuilder.build();
	}
	
	public boolean is_in(int id){
		for (Iterator<Capteur> iterator = list_capteur.iterator(); iterator.hasNext();) {
			Capteur capteur = (Capteur) iterator.next();
			int sender_id = capteur.getSender_id();
			if ( sender_id == id)
				return true;
		}
		return false;
	}

	public boolean is_suppr(int id){
		for (Iterator<Capteur> iterator = list_capteur.iterator(); iterator.hasNext();) {
			Capteur capteur = (Capteur) iterator.next();
			int sender_id = capteur.getSender_id();
			if ( sender_id == id)
				if (list_capteur.remove(capteur))
					return true;
		}
		return false;
	}

public void list(JsonObject object , Socket socket){ // list all capteur in bus
	String s_class = "";
	String s_name = "";
	JsonObjectBuilder ack = Json.createObjectBuilder();
	ack.add("type", "list");
	ack.add("ack", Json.createObjectBuilder().add("resp","ok")); // changer le resp (ici action par default)

	try{
		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
		BufferedWriter wr = new BufferedWriter(out);

		try { // try catch si la chiane de caractere recherchee dans l'objet n'est pas trouvee
			s_class = object.getString("sender_class");
		} catch (NullPointerException e) {}
		try{ //idem
			s_name = object.getString("sender_name");
		} catch (NullPointerException e) {}
		JsonArray list;
		if (! s_class.equals("")){ // on teste si la chaine a bien ete trouvee
			System.out.println("Oui !! ");
			list = list_capteurs("sender_class", s_class);
		} else if ( ! s_name.equals("")){ // idem
			System.out.println("Oui !! ");
			list = list_capteurs("sender_name", s_name);
		} else { // cas par default on renvoie tous capteurs presents dans list_capteur
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


public void checkIn(JsonObject jsonObject, Socket socket) { // enregistrement cote bus
	//list_capteur = new ArrayList<Capteur>();

	JsonObject ack = Json.createObjectBuilder()
			.add("type", "register")
			.add("sender_id",++id)
			.add("ack", Json.createObjectBuilder().add("resp", "ok"))
			.build();
	String jsonText = ack.toString();

	try {

		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
		BufferedWriter wr = new BufferedWriter(out);

		String name = jsonObject.getString("name"); // lecture du nom provenant du capteur
		String type = jsonObject.getString("class"); // lecture de la classe provenant du capteur
		Capteur cap = new Capteur(type, name); // creation d'un capteur
		cap.setSender_id(id); // attribution de l'id puis incrementation de celui-ci
		list_capteur.add(cap); // ajout du capteur cree ci-avant dans la liste de capteurs

		wr.write(jsonText); //envoie le message ack
		wr.newLine();
		wr.flush();

		socket.close();

	} catch (UnknownHostException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}

}

public void checkOut(JsonObject object , Socket socket) { // desenregistrement cote bus
	try {
		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
		BufferedWriter wr = new BufferedWriter(out);

		int sender_id = object.getInt("sender_id");
		// if "sender_id" is in list_capteur , is remove
		if (is_suppr(sender_id)){ 
			JsonObject reponse = Json.createObjectBuilder()
					.add("type", "deregister")
					.add("ack", Json.createObjectBuilder().add("resp", 0))
					.build();
			String jsonText = reponse.toString();
			wr.write(jsonText);
			wr.newLine();
			wr.flush();
		}
		else { // if "sender_id" isn't in list_capteur , an error code is send
			JsonObject reponse = Json.createObjectBuilder()
					.add("type", "deregister")
					.add("ack", Json.createObjectBuilder().add("resp",428))
					.build();
			String jsonText = reponse.toString();
			wr.write(jsonText);
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


public void ackSend(JsonObject object , Socket socket) { // ack send 
	try {

		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
		BufferedWriter wr = new BufferedWriter(out);

		int sendid =object.getInt("sender_id");
		if (is_in(sendid)){ // verification existance de l'id

			JsonObject  reponse = Json.createObjectBuilder()
					.add("type", "send")
					.add("ack", Json.createObjectBuilder().add("resp", 0))
					.build();
			String jsonText = reponse.toString();
			wr.write(jsonText);
			wr.newLine();
			wr.flush();

			tabmsgId[idtab].setSender_id(sendid); //stocke l'id capteur
			tabmsgId[idtab].setTabid(object.getJsonObject("msg")); //stocke le message
			idtab++;

		} else { // if "sender_id" is'nt know , return an error code
			JsonObject reponse = Json.createObjectBuilder()
					.add("type", "send")
					.add("ack", Json.createObjectBuilder().add("resp",438))
					.build();
			String jsonText = reponse.toString();
			wr.write(jsonText);
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

public int indexcapt(int sender_id){
	for(int i=0;i<MAXTAILLE;i++)
		if(sender_id == tabmsgId[i].getSender_id())
			return i;
	return 0;
}


public void getInformation() { // Envoie des messages au client 
	Socket socket;
	try {
		socket = new Socket(InetAddress.getLocalHost(), 8888);

		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
		BufferedWriter wr = new BufferedWriter(out);

		InputStreamReader in = new InputStreamReader(socket.getInputStream());
		BufferedReader rd = new BufferedReader(in);

		String jsonResp = rd.readLine();

		JsonReader jsonReader = Json.createReader(new StringReader(jsonResp));
		JsonObject object = jsonReader.readObject();
		int msgid = object.getInt("msg_id");
		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss"); // recuperation de la date en milliseconde
		String dateInString = sdf.format(new Date());
		Date datepars = sdf.parse(dateInString);
		int date = (int)datepars.getTime();
		if (list_capteur.contains(object.getInt("sender_id"))){ // verification existance de l'id dans la liste
			if(msgid>=0 && msgid<MAXTAILLE){
				int indice = indexcapt(object.getInt("sender_id")); //indice du capteur dans le tableau capteur-message
				if(msgid < tabmsgId[indice].getmsgid()){ //verifie si msgid inferieur a  l'id le plus ancien
					JsonObject msg = tabmsgId[indice].getTabid(0); //message le plus ancien dans la table des messages
					JsonObject repmsg = Json.createObjectBuilder()
							.add("type","get")
							.add("ack", Json.createObjectBuilder().add("resp", 0))
							.add("date",date)
							.add("msg_id", msgid)
							.add("contents",msg)
							.build();
					String jsonText = repmsg.toString();
					wr.write(jsonText);
					wr.newLine();
					wr.flush();
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
					String jsonText = repmsg.toString();
					wr.write(jsonText);
					wr.newLine();
					wr.flush();
				}
				else if(tabmsgId[indice].getTabid(0) == null){ //verifie si le tableau des messages est vide
					JsonObject reponse = Json.createObjectBuilder()
							.add("type", "get")
							.add("ack", Json.createObjectBuilder().add("resp",404))
							.build();
					String jsonText = reponse.toString();
					wr.write(jsonText);
					wr.newLine();
					wr.flush();
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
					String jsonText = repmsg.toString();
					wr.write(jsonText);
					wr.newLine();
					wr.flush();
				}
			}
		} else {
			JsonObject reponse = Json.createObjectBuilder()
					.add("type", "get")
					.add("ack", Json.createObjectBuilder().add("resp",404))
					.build();
			String jsonText = reponse.toString();
			wr.write(jsonText);
			wr.newLine();
			wr.flush();	
		}
		jsonReader.close();
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
		socket = new Socket(InetAddress.getLocalHost(), 8888);

		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
		BufferedWriter wr = new BufferedWriter(out);

		InputStreamReader in = new InputStreamReader(socket.getInputStream());
		BufferedReader rd = new BufferedReader(in);

		String jsonResp = rd.readLine();

		JsonReader jsonReader = Json.createReader(new StringReader(jsonResp));
		JsonObject object = jsonReader.readObject();

		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");  //recuperation de la date en milliseconde
		String dateInString = sdf.format(new Date());
		Date datepars = sdf.parse(dateInString);
		int date = (int)datepars.getTime();
		if (list_capteur.contains(object.getInt("sender_id"))){ // verification existance de l'id dans la liste
			int indice = indexcapt(object.getInt("sender_id")); //indice du capteur dans le tableau capteur-message
			JsonObject msg = tabmsgId[indice].getTabid(tabmsgId[indice].getmsgid());//recupere le message demande d'un capteur
			int msgid = tabmsgId[indice].getmsgid();
			JsonObject repmsg = Json.createObjectBuilder()
					.add("type","get")
					.add("ack", Json.createObjectBuilder().add("resp", 0))
					.add("date",date)
					.add("msg_id", msgid)
					.add("contents",msg)
					.build();
			String jsonText = repmsg.toString();
			wr.write(jsonText);
			wr.newLine();
			wr.flush();
		}

		else {
			JsonObject reponse = Json.createObjectBuilder()
					.add("type", "get")
					.add("ack", Json.createObjectBuilder().add("resp",404))
					.build();
			String jsonText = reponse.toString();
			wr.write(jsonText);
			wr.newLine();
			wr.flush();
		}
		jsonReader.close();
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
			System.out.println(jsonResp);

			try {
				JsonReader jsonReader = Json.createReader(new StringReader(jsonResp));
				JsonObject object = jsonReader.readObject();
				jsonResp = object.getString("type");
				if (jsonResp.equals("register"))
					bus.checkIn(object, socketduserveur);
				if (jsonResp.equals("send"))
					bus.ackSend(object , socketduserveur);
				if (jsonResp.equals("deregister"))
					bus.checkOut(object, socketduserveur);
				if (jsonResp.equals("list"))
					bus.list(object, socketduserveur);
				jsonReader.close();

			} catch (NullPointerException e) {}
			in.close();
			socketserver.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
}
