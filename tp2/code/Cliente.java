import java.io.BufferedReader;
import java.net.Socket;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.lang.Exception;

class Cliente {


    public static void main(String args[]) throws Exception {


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
                            GetNewsThread gn = new GetNewsThread(ip);
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


    public GetNewsThread(String ip) {
        this.ip = ip;
    }

     @Override
    public void run() {
        String source = "";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if(addr.isLinkLocalAddress()){
                        source = InetAddress.getByName(addr.getHostAddress().split("\\%")[0]).getHostAddress();
                    }
                }
            }
        }
        catch (Exception e) {
            // throw new RuntimeException(e);
        }


        String data = "GET_NEWS_FROM " + source + " " + this.ip;

        try{
            socket = new Socket("localhost",9999);
            PrintWriter send = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            send.println(data);
            System.out.println("Sent: " + data);
            socket.setSoTimeout(10000);
            String temp = in.readLine();
            System.out.println("Got news from: " + ip + "!\nNews:\n\t" + temp);
            socket.close();
        }
        catch(Exception e){
            System.out.println("Erro! " + e.getMessage());
        }

    }
}
