import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.lang.Exception;
import java.net.SocketException;
import java.lang.RuntimeException;
import java.util.Scanner;
import java.net.DatagramPacket;

class Adhoc {

    private static Map<InetAddress,No> tabela;

    private static void sendRR(Scanner s) {
        try{
            System.out.println("Insira o endereço a procurar:");
            String ip = s.nextLine();
            System.out.println("Insira o tempo máximo de espera: (milisegundos)");
            long t = s.nextLong();
            System.out.println("Insira o número máximo de saltos:");
            int saltos = s.nextInt();
            No no = new No(InetAddress.getByName(ip), InetAddress.getByName(ip), -1, null, 0);
            if(!tabela.containsKey(no.getIp())) {
                tabela.put(InetAddress.getByName(ip), no);
                MulticastSocket so = new MulticastSocket(9999);
                InetAddress group = InetAddress.getByName("FF02::1");
                so.joinGroup(group);
                String source = "";
                for(No n : tabela.values()) {
                    if(n.getSaltos() == 0) {
                        source = n.getIp().getHostAddress();
                        break;
                    }
                }
                String data = "ROUTE_REQUEST " + source + " " + InetAddress.getByName(ip).getHostAddress() + " " + saltos + " " + t;
                System.out.println(data);
                byte[] buf = new byte[500];
                buf = data.getBytes();
                DatagramPacket p = new DatagramPacket(buf, buf.length, group, 9999);
                so.send(p);
                so.leaveGroup(group);
                so.close();
            }
        }
        catch(Exception e) {
            System.out.println("sendRR: " + e.getMessage());
        }
    }

    public static void main(String args[]) throws Exception {


        MulticastSocket socket = new MulticastSocket(9999);
        tabela = new HashMap<InetAddress, No>();

        /*byte bytes[] = InetAddress.getByName("FF02::1").getAddress();
        InetAddress a = Inet6Address.getByAddress("",bytes,35);
        System.out.println("IP:" + a.getHostAddress());*/

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
                        InetAddress temp = InetAddress.getByName(addr.getHostAddress().split("\\%")[0]);
                        No no = new No(temp, temp, 0, null, 0);
                        tabela.put(addr,no);
                    }
                }
            }
        }
        catch (SocketException e) {
            socket.close();
            throw new RuntimeException(e);
        }

            /*InetAddress inetAddress = InetAddress.getLocalHost();
            System.out.println("Address:- " + inetAddress);
            System.out.println("IP Address:- " + inetAddress.getHostAddress());
            System.out.println("Host Name:- " + inetAddress.getHostName());*/

            //No no = new No(ip,ip,0,null);

            HelloSendThread hs = new HelloSendThread(socket,tabela);
            hs.start();

            MulticastReceiveThread mr = new MulticastReceiveThread(socket,tabela);
            mr.start();

            int opcao = 1;
            Scanner reader = new Scanner(System.in);
            while(opcao != 0) {
                Thread.sleep(5);
                System.out.println("Opções");
                System.out.println("1 - Imprimir tabela");
                System.out.println("2 - Enviar ROUTE_REQUEST");
                System.out.println("0 - Sair");
                opcao = reader.nextInt();
                reader.nextLine();
                switch (opcao) {
                    case 0: hs.interrupt();
                            mr.interrupt();
                            System.exit(0);
                            break;
                    case 1: PrintThread p = new PrintThread(tabela);
                            p.start();
                            break;
                    case 2: sendRR(reader);
                            break;
                    default: System.out.println("Opção inválida!");
                }
            }
            reader.close();
        }
}
