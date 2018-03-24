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
import java.net.DatagramPacket;

class Adhoc {

    private static Map<InetAddress,No> tabela;


    public static void main(String args[]) throws Exception {

        MulticastSocket socket = new MulticastSocket(9999);
        tabela = new HashMap<InetAddress, No>();

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

        HelloSendThread hs = new HelloSendThread(socket,tabela);
        hs.start();

        MulticastReceiveThread mr = new MulticastReceiveThread(socket,tabela);
        mr.start();

        TCPThread tt = new TCPThread(tabela);
        tt.start();

        while(hs.isAlive() || mr.isAlive() || tt.isAlive()){
            Thread.sleep(5*60*1000);
        }

    }
}
