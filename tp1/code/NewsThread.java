import java.util.Map;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.MulticastSocket;
import java.lang.Exception;


class NewsThread extends Thread {

    // Formato do data
    // GET_NEWS_FROM IP_ORIGEM IP_DESTINO IP_NEXT
    // NEWS_FOR IP_ORIGEM IP_DESTINO (na ordem inversa do de cima) IP_NEXT
    private String[] data;
    private Map<InetAddress, No> tabela;
    private int flag;

    public NewsThread(String[] data, Map<InetAddress, No> tabela, int flag) {
        this.data = data;
        this.tabela = tabela;
        this.flag = flag;
    }

    @Override
    public void run() {
        MulticastSocket so = null;
        InetAddress group = null;
        try {
            so = new MulticastSocket(9999);
            group = InetAddress.getByName("FF02::1");
        }
        catch(Exception e) {
            System.out.println("Socket and group: " + e.getMessage());
        }

        // flag=1 => NEWS_FOR | flag=0 => GET_NEWS_FROM
        if(flag == 1) {

        }
        else {
            String self = "";
            for(No n : tabela.values()) {
                if(n.getSaltos() == 0) {
                    self = n.getIp().getHostAddress();
                    break;
                }
            }
            if(self.equals(data[3])) {
                String dest = data[2];
                InetAddress dip = null;
                try {
                    dip = InetAddress.getByName(dest);
                }
                catch(Exception e) {
                    System.out.println("Get DIP: " + e.getMessage());
                }
                if(tabela.containsKey(dip) && tabela.get(dip).getSaltos() != 0){
                    try{
                        String temp = "GET_NEWS_FROM " + data[1] + " " + data[2] + " " + tabela.get(dip).getIpVizinho().getHostAddress();
                        byte[] buf = new byte[500];
                        buf = temp.getBytes();
                        DatagramPacket p = new DatagramPacket(buf, buf.length, group, 9999);
                        so.send(p);
                        so.leaveGroup(group);
                        so.close();
                    }
                    catch(Exception e) {
                        System.out.println("NT Contem e N-0: " + e.getMessage());
                    }
                }
                else if (tabela.containsKey(dip) && tabela.get(dip).getSaltos() == 0) {
                    try {
                        InetAddress sip = InetAddress.getByName(data[1]);
                        Socket s = new Socket("localhost", 9999);
                        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                        out.println("GET_NEWS");
                    }
                    catch(Exception e) {
                        System.out.println("NT Contem e 0: " + e.getMessage());
                    }
                }
                else if(!tabela.containsKey(dip)) {
                    try {
                        No no = new No(dip, dip, -1, null, 0);
                        tabela.put(dip, no);
                        so.joinGroup(group);
                        String source = "";
                        for(No n : tabela.values()) {
                            if(n.getSaltos() == 0) {
                                source = n.getIp().getHostAddress();
                                break;
                            }
                        }
                        String temp = "ROUTE_REQUEST " + source + " " + dip.getHostAddress() + " " + 10 + " " + 500;
                        byte[] buf = new byte[500];
                        buf = temp.getBytes();
                        DatagramPacket p = new DatagramPacket(buf, buf.length, group, 9999);
                        so.send(p);
                        so.leaveGroup(group);
                        so.close();
                        Thread.sleep(550);
                        // Parte de cima para adicionar o caminho até onde é preciso
                        // Agora tratar de reencaminhar
                        temp = "GET_NEWS_FROM " + data[1] + " " + data[2] + " " + tabela.get(dip).getIpVizinho().getHostAddress();
                        buf = temp.getBytes();
                        p = new DatagramPacket(buf, buf.length, group, 9999);
                        so.send(p);
                        so.leaveGroup(group);
                        so.close();
                    }
                    catch(Exception e) {
                        System.out.println("NT N-contem: " + e.getMessage());
                    }
                }
            }
        }
    }

}
