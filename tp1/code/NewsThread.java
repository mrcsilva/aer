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
        String dest = "";
        InetAddress dip = null;
        byte[] buf = new byte[500];
        DatagramPacket p = null;
        String source = "";
        String self = "";
        String dataTcp = "";
        try {
            so = new MulticastSocket(9999);
            group = InetAddress.getByName("FF02::1");
        }
        catch(Exception e) {
            System.out.println("Socket and group: " + e.getMessage());
        }

        // flag=1 => NEWS_FOR | flag=0 => GET_NEWS_FROM
        if(flag == 1) {
            for(No n : tabela.values()) {
                if(n.getSaltos() == 0) {
                    self = n.getIp().getHostAddress();
                    break;
                }
            }
            // Coloca numa string o conteudo a enviar
            for (int i = 3; i < data.length; i++) {
                dataTcp += data[i]+" ";
            }
            // Se o vizinho for o desejado
            if(self.equals(data[3])) {
                try {
                    dest = data[2];
                    try {
                        dip = InetAddress.getByName(dest);
                    }
                    catch(Exception e) {
                        System.out.println("Get DIP: " + e.getMessage());
                    }
                    // Se o destino estiver na tabela e não for o próprio
                    // Se estiver e for o proprio
                    // Se não estiver
                    if(tabela.containsKey(dip) && tabela.get(dip).getSaltos() != 0){
                        try{
                            String temp = "NEWS_FOR " + data[1] + " " + data[2] + " " + tabela.get(dip).getIpVizinho().getHostAddress() + " " + dataTcp;
                            buf = temp.getBytes();
                            p = new DatagramPacket(buf, buf.length, group, 9999);
                            so.send(p);
                            so.leaveGroup(group);
                            so.close();
                            System.out.println("Sent NEWS:FOR to: " + tabela.get(dip).getIpVizinho().getHostAddress());
                        }
                        catch(Exception e) {
                            System.out.println("NT Send e N!=0: " + e.getMessage());
                        }
                    }
                    else if (tabela.containsKey(dip) && tabela.get(dip).getSaltos() == 0) {
                        try {
                            InetAddress sip = InetAddress.getByName(data[1]);
                            Socket s = new Socket("localhost", 9999);
                            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                            out.println("NEWS:\n" + dataTcp);
                            System.out.println("Delivered NEWS to: " + data[2]);
                        }
                        catch(Exception e) {
                            System.out.println("NT Send e 0: " + e.getMessage());
                        }
                    }
                    else if(!tabela.containsKey(dip)) {
                        try {
                            No no = new No(dip, dip, -1, null, 0);
                            tabela.put(dip, no);
                            so.joinGroup(group);
                            for(No n : tabela.values()) {
                                if(n.getSaltos() == 0) {
                                    source = n.getIp().getHostAddress();
                                    break;
                                }
                            }
                            String temp = "ROUTE_REQUEST " + source + " " + dip.getHostAddress() + " " + 10 + " " + 500;
                            buf = temp.getBytes();
                            DatagramPacket p = new DatagramPacket(buf, buf.length, group, 9999);
                            so.send(p);
                            so.leaveGroup(group);
                            so.close();
                            Thread.sleep(550);
                            // Parte de cima para adicionar o caminho até onde é preciso
                            // Agora tratar de reencaminhar
                            temp = "NEWS_FOR " + data[1] + " " + data[2] + " " + tabela.get(dip).getIpVizinho().getHostAddress() + " " + dataTcp;
                            buf = temp.getBytes();
                            p = new DatagramPacket(buf, buf.length, group, 9999);
                            so.send(p);
                            so.leaveGroup(group);
                            so.close();
                            System.out.println("Sent NEWS_FOR to: " + tabela.get(dip).getIpVizinho().getHostAddress());
                        }
                        catch(Exception e) {
                            System.out.println("NT Send N-contem: " + e.getMessage());
                        }
                    }
                }
                catch(Exception e) {

                }
            }
        }
        else {
            for(No n : tabela.values()) {
                if(n.getSaltos() == 0) {
                    self = n.getIp().getHostAddress();
                    break;
                }
            }
            // Se ele for o vizinho pretendido
            if(self.equals(data[3])) {
                dest = data[2];
                try {
                    dip = InetAddress.getByName(dest);
                }
                catch(Exception e) {
                    System.out.println("Get DIP: " + e.getMessage());
                }
                if(tabela.containsKey(dip) && tabela.get(dip).getSaltos() != 0){
                    try{
                        String temp = "GET_NEWS_FROM " + data[1] + " " + data[2] + " " + tabela.get(dip).getIpVizinho().getHostAddress();
                        buf = temp.getBytes();
                        p = new DatagramPacket(buf, buf.length, group, 9999);
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
                        for(No n : tabela.values()) {
                            if(n.getSaltos() == 0) {
                                source = n.getIp().getHostAddress();
                                break;
                            }
                        }
                        String temp = "ROUTE_REQUEST " + source + " " + dip.getHostAddress() + " " + 10 + " " + 500;
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
