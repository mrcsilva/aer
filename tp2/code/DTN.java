import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.DatagramSocket;
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
import java.net.DatagramPacket;
import java.util.Scanner;

class DTN {

    // Tabela com os nos atualmente adjacentes
    private static Map<InetAddress,No> tabela;
    // Lista de mensagens para outros nos
    private static Map<InetAddress,List<Message>> messages;
    // Lista de mensagens a enviar
    private static Map<Message, Integer> toSend;
    // Associacao mensagens_enviadas -> IPs enviados
    private static Map<Message, List<InetAddress>> sent;
    // Lista de mensagens respondidas
    private static List<String> received;



    public static void main(String args[]) throws Exception {

        MulticastSocket socket = new MulticastSocket(9999);
        DatagramSocket socket2 = new DatagramSocket(6666);
        tabela = new HashMap<InetAddress, No>();
        messages = new HashMap<InetAddress, List<Message>>();
        toSend = new HashMap<Message, Integer>();
        sent = new HashMap<Message, List<InetAddress>>();
        received = new ArrayList<String>();

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
                        No no = new No(temp, 0, 0, null,0);
                        tabela.put(addr,no);
                    }
                }
            }
        }
        catch (SocketException e) {
            socket.close();
            socket2.close();
            throw new RuntimeException(e);
        }

        HelloSendThread hs = new HelloSendThread(socket);
        hs.start();

        MulticastReceiveThread mr = new MulticastReceiveThread(socket, socket2, tabela, messages, toSend, sent);
        mr.start();

        UnicastReceiveThread ur = new UnicastReceiveThread(socket2, tabela, messages);
        ur.start();

        TCPThread tt = new TCPThread(socket2, tabela, toSend, sent, received);
        tt.start();

        MessageCleanerThread mt = new MessageCleanerThread(messages, toSend, sent);
        mt.start();

        int op = -1;
        Scanner s = new Scanner(System.in);
        do {
            System.out.println("------OPCOES------");
            System.out.println("1 - Imprimir tabela de vizinhos");
            System.out.println("0 - Sair");
            op = s.nextInt();
            switch(op) {
                case 1: PrintThread pt = new PrintThread(tabela);
                        pt.start();
                        try {
                            Thread.sleep(10);
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                        break;
                case 2: System.out.println("------ MENSAGENS ------");
                        for(Map.Entry<InetAddress, List<Message>> entry : messages.entrySet()) {
                            System.out.println("IP: " + entry.getKey().getHostAddress());
                            for(Message ms : entry.getValue()) {
                                System.out.println("\t" + ms.toString());
                            }
                        }
                        break;
                case 3: System.out.println("------ MY MENSAGENS ------");
                        for(Map.Entry<Message, Integer> entry : toSend.entrySet()) {
                            System.out.println("Mensagem: " + entry.getKey().toString());
                            System.out.println("\tTotal: " + entry.getValue());
                        }
                        break;
                case 0: hs.interrupt();
                        mr.interrupt();
                        tt.interrupt();
                        break;
            }
        } while (op != 0);
        System.exit(0);
    }
}
