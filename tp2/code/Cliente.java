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
import java.net.SocketTimeoutException;

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
                    case 1: System.out.println("Insira o ultimo valor endereço que pretende receber noticias:");
                            ip = "fe80::200:ff:feaa:" + reader.nextLine();
                            System.out.println(ip);
                            // Inicia uma thread que tratara do pedido de noticias
                            GetNewsThread gn = new GetNewsThread(ip);
                            gn.start();
                            break;
                    default: System.out.println("Opção inválida!");
                }
            }
            reader.close();
    }
}



class GetNewsThread extends Thread {

    private Socket socket;
    private String ip;
    private PrintWriter send;


    public GetNewsThread(String ip) {
        this.ip = ip;
    }

     @Override
    public void run() {
        try {
            socket = new Socket("localhost", 9999);
            send = new PrintWriter(socket.getOutputStream(), true);
            send.println("CLIENT");

            // Timeout de 5 minutos (para a demonstracao)
            // Numa aplicacao real teria de ser aumentado substancialmente
            // ou ajustado de acordo com os padroes de movimento
            socket.setSoTimeout(300000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

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
            e.printStackTrace();
        }

        String data = "GET_NEWS_FROM " + source + " " + this.ip;

        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            send.println(data);
            // System.out.println("Sent: " + data);
            String temp = in.readLine();
            String[] temp2 = temp.split(" ");
            temp = "";
            for(int i = 4; i < temp2.length; i++) {
                temp += temp2[i] + " ";
            }
            System.out.println("Got news from: " + ip + "!\nNews:\n\t" + temp);
            socket.close();
        }
        catch(Exception e){
            if(e.getClass().isInstance(new SocketTimeoutException())) {
                System.out.println("Request " + data + " timed out!");
            }
            else {
                e.printStackTrace();
            }
        }

    }
}
