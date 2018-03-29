import java.util.Map;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.MulticastSocket;
import java.lang.Exception;


class NewsThread extends Thread {

    // Formato do data
    // GET_NEWS_FROM <IP_ORIGEM> <IP_DESTINO> <IP_NEXT>
    // NEWS_FOR <IP_ORIGEM> <IP_DESTINO> (na ordem inversa do de cima) <IP_NEXT> <NEWS>
    private String[] data;
    private Map<InetAddress, No> tabela;
    private int flag;

    public NewsThread(String[] data, Map<InetAddress, No> tabela, int flag) {
        this.data = data;
        this.tabela = tabela;
        this.flag = flag;
    }

    // Envia ROUTE_REQUEST
    // @ip Endereço a descobrir
    // @t Tempo até ao timeout
    // @saltos Numero máximo de saltos que o pedido pode dar
    private void sendRR(InetAddress ip, long t, int saltos) {
        try{
            No no = new No(ip, ip, -1, null, 0);
            if(!tabela.containsKey(ip)) {
                tabela.put(ip, no);
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
                String data = "ROUTE_REQUEST " + source + " " + ip.getHostAddress() + " " + saltos + " " + t;
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
            System.out.println("sendRR: ");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        MulticastSocket so = null;
        InetAddress group = null;
        InetAddress dip = null;
        byte[] buf = new byte[1000];
        DatagramPacket p = null;
        String source = "";
        String dest = "";
        String viz = "";
        String self = "";
        String dataTcp = "";
        String temp = "";
        try {
            so = new MulticastSocket(9999);
            group = InetAddress.getByName("FF02::1");
        }
        catch(Exception e) {
            System.out.println("Socket and group: " + e.getMessage());
        }

        // flag=1 => NEWS_FOR | flag=0 => GET_NEWS_FROM
        if(flag == 1) {
            try {
                source = InetAddress.getByName(data[1]).getHostAddress();
                dest = InetAddress.getByName(data[2]).getHostAddress();
                viz = InetAddress.getByName(data[3]).getHostAddress();
            }
            catch(Exception e) {
                System.out.println("Inets NEWS_FOR");
                e.printStackTrace();
            }
            // Obtem o seu endereço
            for(No n : tabela.values()) {
                if(n.getSaltos() == 0) {
                    self = n.getIp().getHostAddress();
                    break;
                }
            }
            // Coloca numa string o conteudo a enviar
            for (int i = 4; i < data.length; i++) {
                dataTcp += data[i]+" ";
            }
            // Se o vizinho for o desejado
            if(self.equals(viz)) {
                try {
                    dip = InetAddress.getByName(dest);

                    // Se o destino estiver na tabela e não for o próprio
                    // Se estiver e for o proprio
                    // Se não estiver
                    if(tabela.containsKey(dip) && tabela.get(dip).getSaltos() != 0){
                        so.joinGroup(group);
                        temp = "NEWS_FOR " + source + " " + dest + " " + tabela.get(dip).getIpVizinho().getHostAddress() + " " + dataTcp;
                        buf = temp.getBytes();
                        p = new DatagramPacket(buf, buf.length, group, 9999);
                        so.send(p);
                        System.out.println("Sent NEWS_FOR: " + temp);
                        so.leaveGroup(group);
                        so.close();
                    }
                    else if (tabela.containsKey(dip) && tabela.get(dip).getSaltos() == 0) {
                        Socket s = new Socket("localhost", 9999);
                        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                        out.println("NEWS_FOR " + source + " " + dataTcp);
                        s.close();
                    }
                    else if(!tabela.containsKey(dip)) {
                        int tempo = 200;
                        int saltos = 6;
                        do {
                            tempo += 150;
                            saltos += 1;
                            sendRR(dip, tempo, saltos);
                            Thread.sleep(tempo+50);
                        } while (!tabela.containsKey(dip) && saltos <= 38 && tempo <= 5000);
                        if(saltos <= 38 && tempo <= 5000) {
                            // Parte de cima para adicionar o caminho até onde é preciso
                            // Agora tratar de reencaminhar
                            temp = "NEWS_FOR " + source + " " + dest + " " + tabela.get(dip).getIpVizinho().getHostAddress() + " " + dataTcp;
                            buf = temp.getBytes();
                            p = new DatagramPacket(buf, buf.length, group, 9999);
                            so.send(p);
                            System.out.println("Sent NEWS_FOR: " + temp);
                            so.leaveGroup(group);
                            so.close();
                        }
                    }
                }
                catch(Exception e) {
                    System.out.println("Vizinho OK");
                    e.printStackTrace();
                }
            }
        }
        else {
            try {
                source = InetAddress.getByName(data[1]).getHostAddress();
                dest = InetAddress.getByName(data[2]).getHostAddress();
                viz = InetAddress.getByName(data[3]).getHostAddress();
            }
            catch(Exception e) {
                System.out.println("Inets");
                e.printStackTrace();
            }
            // Obtem o seu endereço
            for(No n : tabela.values()) {
                if(n.getSaltos() == 0) {
                    self = n.getIp().getHostAddress();
                    break;
                }
            }
            // Se ele for o vizinho pretendido
            if(self.equals(viz)) {
                try {
                    dip = InetAddress.getByName(dest);
                }
                catch(Exception e) {
                    System.out.println("Get DIP: " + e.getMessage());
                }
                // Ainda nao e o destino mas esta na tabela
                // E o ultimo e esta na tabela
                // Nao esta na tabela
                if(tabela.containsKey(dip) && tabela.get(dip).getSaltos() != 0){
                    try{
                        so.joinGroup(group);
                        temp = "GET_NEWS_FROM " + source + " " + dest + " " + tabela.get(dip).getIpVizinho().getHostAddress();
                        buf = temp.getBytes();
                        p = new DatagramPacket(buf, buf.length, group, 9999);
                        so.send(p);
                        System.out.println("Sent GET_NEWS_FROM: " + temp);
                        so.leaveGroup(group);
                        so.close();
                    }
                    catch(Exception e) {
                        System.out.println("NT Contem e N-0: ");
                        e.printStackTrace();
                    }
                }
                else if (tabela.containsKey(dip) && tabela.get(dip).getSaltos() == 0) {
                    try {
                        Socket s = new Socket("localhost", 9999);
                        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                        out.println("GET_NEWS_FROM " + source + " " + dest);
                        s.close();
                    }
                    catch(Exception e) {
                        System.out.println("NT Contem e 0: " + e.getMessage());
                    }
                }
                else if(!tabela.containsKey(dip)) {
                    int tempo = 200;
                    int saltos = 6;
                    try {
                        do {
                            tempo += 150;
                            saltos += 1;
                            sendRR(dip, tempo, saltos);
                            Thread.sleep(tempo+50);
                        } while (!tabela.containsKey(dip) && saltos <= 38 && tempo <= 5000);
                        if(saltos <= 38 && tempo <= 5000) {
                            // Parte de cima para adicionar o caminho até onde é preciso
                            // Agora tratar de reencaminhar
                            temp = "GET_NEWS_FROM " + source + " " + dest + " " + tabela.get(dip).getIpVizinho().getHostAddress();
                            buf = temp.getBytes();
                            p = new DatagramPacket(buf, buf.length, group, 9999);
                            so.send(p);
                            so.leaveGroup(group);
                            System.out.println("Sent GET_NEWS_FROM: " + temp);
                        }
                    }
                    catch(Exception e) {
                        System.out.println("NT N-contem: " + e.getMessage());
                    }
                }
            }
        }
    }

}
