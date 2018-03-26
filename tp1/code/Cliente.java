import java.io.BufferedReader;
import java.net.Socket;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.io.DataOutputStream;


class Cliente {


	public static void main(String args[]) throws Exception {


			Socket socket;
			String ip;

			int opcao = 1;
            Scanner reader = new Scanner(System.in);

          	while(opcao != 0) {
                System.out.println("\nOpções");
                System.out.println("1 - Enviar GET_NEWS_FROM");
                System.out.println("0 - Sair\n");
                opcao = reader.nextInt();
                reader.nextLine();
                switch (opcao) {
                    case 0: System.exit(0);
                            break;
                    case 1: System.out.println("Insira o endereço que pretende receber noticias:");
                    		ip = reader.nextLine();
                    		socket = new Socket("localhost",9999);
                    		GetNewsThread gn = new GetNewsThread(socket,ip);
        					gn.start();
                            break;
                    default: System.out.println("Opção inválida!");
                }
            }
            reader.close();

	}
}




class GetNewsThread extends Thread{

    private Socket socket;
    private String ip;


    public GetNewsThread(Socket socket,String ip) {
    	this.socket = socket;
    	this.ip = ip;
        
    }

     @Override
    public void run() {


    	String news;

        String data = "GET_NEWS_FROM " + this.ip + "\n";

        try{	
        DataOutputStream send = new DataOutputStream(socket.getOutputStream());
        BufferedReader receive = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        send.writeBytes(data);

        socket.setSoTimeout(5000);

        news = receive.readLine();
        if(news.equals("Non Reachable")){
        	System.out.println(ip + " : " + news + "\n");
        }
        else{
        	System.out.println("News From " + ip + " = " + news + "\n");
        }
        socket.shutdownOutput();
        socket.shutdownInput();
        socket.close();

    	}catch(Exception e){}

	}
}