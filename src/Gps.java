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
import javax.json.JsonReader;

public class Gps extends Capteur {

	private double latitude;
	private double longitude;
	
	public Gps(){
		super("Gps", "default_Gps");
		this.latitude = 0;
		this.longitude = 0;
	}

	public Gps(String sender_class, String sender_name, double latitude,
			double longitude) {
		super(sender_class, sender_name);
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	/**
	 * Retourne la latitude.
	 * @return Latitude.
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Modifie la latitude.
	 * @param latitude, un double.
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * Retourne la longitude.
	 * @return Longitude.
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Modifie la longitude.
	 * @param longitude, un double.
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	
	/**
	 * 
	 * @param sender_id, un int.
	 * @param contents, un JsonObject.
	 */
	public  void send(int sender_id,JsonObject contents){
		Socket socket;
		try {
			JsonObject msg = Json.createObjectBuilder()
					.add("type","send")
					.add("sender_id", sender_id)
					.add("contents",contents)
					.build();
			
			String jsonText = msg.toString();
			
			socket = new Socket(InetAddress.getLocalHost(), 8888);
			
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
			BufferedWriter wr = new BufferedWriter(out);
			
			wr.write(jsonText);
			wr.newLine();
			wr.flush();

			InputStreamReader in = new InputStreamReader(socket.getInputStream());
			BufferedReader rd = new BufferedReader(in);
			String jsonResp = rd.readLine();
			
		    JsonReader jsonReader = Json.createReader(new StringReader(jsonResp));
		    JsonObject object = jsonReader.readObject();
		    JsonObject ack = object.getJsonObject("ack");
			int res = ack.getInt("resp");
			socket.close();
			jsonReader.close();
			if ( res == 0 ){
				System.out.println("Send success !");	
			}
			else{
				System.out.println("Error : " + codeError(res));
				
			}	

		}catch (UnknownHostException e) {
			e.printStackTrace();

		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void main(String[] args) {

		Socket socket;
		int numport=8888;
		try {
			socket = new Socket(InetAddress.getLocalHost(), numport);
			Gps gps = new Gps("Gps","myGps",48.5898,2.78213);
			gps.registerSender(gps.getSender_class(), gps.getSender_name(), socket);
			
			JsonObject jsonObj = Json.createObjectBuilder()
					.add("latitude",gps.getLatitude()).add("longitude", gps.getLongitude())
						.build();
			gps.send(gps.getSender_id(),jsonObj);
			socket.close();

		}catch (UnknownHostException e) {
			e.printStackTrace();
		}catch (IOException e) {

			e.printStackTrace();
		}
	}
	
}
