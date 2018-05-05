import java.io.BufferedReader;
import java.net.Socket;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.net.NetworkInterface;
import java.net.InetAddress;

class Server {

    private static String noticia;

    public static void main(String[] args) {
        Socket s = null;
        int opcao = 1;
        Scanner reader = new Scanner(System.in);
        String source = "";
        noticia = "";
        try {
            s = new Socket("localhost", 9999);
            PrintWriter send = new PrintWriter(s.getOutputStream(), true);
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
            send.println("SERVER");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("IP: " + source);
        HandleRequest hr = new HandleRequest(s, noticia);
        hr.start();
        while(opcao != 0) {
            System.out.println("\nOpções");
            System.out.println("1 - Modificar noticia");
            System.out.println("0 - Sair\n");
            opcao = reader.nextInt();
            reader.nextLine();
            switch (opcao) {
                case 0: System.exit(0);
                        break;
                case 1: System.out.println("Insira a noticia:");
                        noticia = reader.nextLine();
                        hr.setNoticia(noticia);
                        break;
                default: System.out.println("Opção inválida!");
            }
        }
        reader.close();
    }

}

class HandleRequest extends Thread {

    private Socket s;
    private String noticia;

    public HandleRequest(Socket s, String noticia) {
        this.s = s;
        this.noticia = noticia;
    }

    /**
     * @param noticia the noticia to set
     */
    public void setNoticia(String noticia) {
    	this.noticia = noticia;
    }

    @Override
    public void run() {
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            String temp;
            while(true) {
                temp = in.readLine();
                String[] split = temp.split(" ");
                if(split[0].equals("GET_NEWS_FROM")) {
                    out.println("NEWS_FOR " + split[2] + " " + split[1] + " " + noticia);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}
