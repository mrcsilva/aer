import java.io.BufferedReader;
import java.net.Socket;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.net.ServerSocket;
import java.io.DataOutputStream;


class Servidor {


	public static void main(String args[]) throws Exception {


			Socket socket;
			ServerSocket ss = new ServerSocket(9999);
			String clienteM;
			String clienteNews;


			while (true) {
   				socket = ss.accept();
  				BufferedReader receive =
    			new BufferedReader(new InputStreamReader(socket.getInputStream()));
   				DataOutputStream send = new DataOutputStream(socket.getOutputStream());

   				System.out.println("Aceitou");
   				clienteM = receive.readLine();
   				System.out.println("Received: " + clienteM);
   				clienteNews = "Almeida" + '\n';
   				send.writeBytes(clienteNews);
   				System.out.println("Mandou");
  }

	}
}