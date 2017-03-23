import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class Bus {
		public static void main(String[] args) {
			ServerSocket socketserver  ;
			Socket socketduserveur ;
				try {
					socketserver = new ServerSocket(Integer.parseInt(args[0]));
					socketduserveur = socketserver.accept(); 
					InputStream in=socketduserveur.getInputStream();
					//BufferedReader read = new BufferedReader(new InputStreamReader(in));
					JsonReader jsonread = Json.createReader(in);
					JsonObject jsonObj = jsonread.readObject();
					jsonread.close();
					in.close();
					Accelerometer acc = new Accelerometer();
					acc.setSender_class(jsonObj.getString("sender_class"));
					acc.setSender_name(jsonObj.getString("sender_name"));
					acc.setX(jsonObj.getInt("x"));
					acc.setY(jsonObj.getInt("y"));
					acc.setZ(jsonObj.getInt("z"));
					System.out.println(acc);
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
		}


