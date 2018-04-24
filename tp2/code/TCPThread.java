import java.util.Map;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.net.MulticastSocket;
import java.net.Socket;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.lang.Exception;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.io.IOException;
import java.net.UnknownHostException;
import java.lang.InterruptedException;
import java.util.HashMap;

class TCPThread extends Thread {

    private Map<InetAddress, No> tabela;
    private ServerSocket ss;
    private MulticastSocket ms;
    private Map<String, Socket> soMap = new HashMap<>();
    private String noticia = "";

    public TCPThread(Map<InetAddress, No> tabela) {
        this.tabela = tabela;
    }

    public void setNoticia(String noticia) {
        this.noticia = noticia;
    }
    /*
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
                // System.out.println(data);
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
    */
    @Override
    public void run() {
        Socket socket = null;
        BufferedReader in;
        String re = "";
        InetAddress group = null;
        InetAddress dip = null;
        String source = "";
        String viz = "";
        String dest = "";
        String data = "";
        byte[] b = new byte[1000];
        DatagramPacket packet = null;
        try {
            group = InetAddress.getByName("FF02::1");
            ss = new ServerSocket(9999);
            ms = new MulticastSocket(9999);
        }
        catch(Exception e) {
            System.out.println("Group TCP: ");
            e.printStackTrace();
        }
        while(true) {
            // Aceitas ligações TCP para receber os dados
            try {
                socket = ss.accept();
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                re = in.readLine();
                // System.out.println("Received: " + re);
            }
            catch(IOException e) {
                System.out.println("Socket Accept: ");
                e.printStackTrace();
            }
            // Se o que recebeu foi um GET_NEWS_FROM ou um NEWS_FOR
            if(re.split(" ")[0].equals("GET_NEWS_FROM")) {
                try {
                    source = InetAddress.getByName(re.split(" ")[1]).getHostAddress();
                    dest = InetAddress.getByName(re.split(" ")[2]).getHostAddress();
                    ms.joinGroup(group);
                    dip = InetAddress.getByName(dest);
                }
                catch(IOException e) {
                    System.out.println("GET DIP IO: ");
                    e.printStackTrace();
                }
                // Se a tabela já contem o destino ou não
                if(tabela.containsKey(dip) && tabela.get(dip).getSaltos() == 0) {
                    try {
                        if(tabela.containsKey(InetAddress.getByName(source))) {
                            viz = "";
                            // Obtem o endereço do vizinho
                            for(No n : tabela.values()) {
                                if (n.getIp().equals(InetAddress.getByName(source))) {
                                    viz = n.getIpVizinho().getHostAddress();
                                    break;
                                }
                            }
                            data = "NEWS_FOR " + dest + " " + source + " " + viz + " " + noticia;
                            b = data.getBytes();
                            packet = new DatagramPacket(b, b.length, group, 9999);
                            ms.send(packet);
                            ms.leaveGroup(group);
                            // System.out.println("Sent NEWS_FOR: " + data);
                        }
                        else {
                            int tempo = 200;
                            int saltos = 6;
                            InetAddress vizip = null;
                            try {
                                vizip = InetAddress.getByName(source);
                            }
                            catch(Exception ee) {
                                System.out.println("GET Vizinho: ");
                                ee.printStackTrace();
                            }
                            // Enquanto não for colocada na tabela a entrada com o caminho para o destino do pedido
                            // Enviamos ROUTE_REQUEST com um aumento de tempo e de saltos
                            do {
                                tempo += 150;
                                saltos += 1;
                                sendRR(vizip, tempo, saltos);
                                try {
                                    Thread.sleep(tempo+50);
                                }
                                catch(InterruptedException e) {
                                    System.out.println("Erro ao Dormir GET N-contem: ");
                                    e.printStackTrace();
                                }
                            } while (!tabela.containsKey(vizip) && saltos <= 38 && tempo <= 5000);
                            if(saltos <= 38 && tempo <= 5000) {
                                viz = "";
                                // Obtem o ip do vizinho e dele próprio
                                for(No n : tabela.values()) {
                                    if (n.getIp().equals(vizip)) {
                                        viz = n.getIpVizinho().getHostAddress();
                                        break;
                                    }
                                }
                                data = "NEWS_FOR " + dest + " " + source + " " + viz + " " + noticia;
                                b = data.getBytes();
                                packet = new DatagramPacket(b, b.length, group, 9999);
                                ms.send(packet);
                                ms.leaveGroup(group);
                                // System.out.println("Sent NEWS_FOR: " + data);
                            }
                        }
                    }
                    catch(Exception e) {
                        System.out.println("TCP GET Contem: ");
                        e.printStackTrace();
                    }
                }
                else if (tabela.containsKey(dip) && tabela.get(dip).getSaltos() != 0) {
                    soMap.put(dest, socket);
                    try {
                        viz = "";
                        // Obtem o seu próprio endereço e o do vizinho
                        for(No n : tabela.values()) {
                            if (n.getIp().equals(InetAddress.getByName(dest))) {
                                viz = n.getIpVizinho().getHostAddress();
                                break;
                            }
                        }
                        data = "GET_NEWS_FROM " + source + " " + dest + " " + viz;
                        b = data.getBytes();
                        packet = new DatagramPacket(b, b.length, group, 9999);
                        ms.send(packet);
                        ms.leaveGroup(group);
                        // System.out.println("Sent GET_NEWS_FROM: " + data);
                    }
                    catch(Exception e) {
                        System.out.println("TCP GET Contem: " + e.getMessage());
                    }
                }
                else {
                    int tempo = 200;
                    int saltos = 6;
                    InetAddress vizip = null;
                    try {
                        vizip = InetAddress.getByName(dest);
                    }
                    catch(Exception ee) {
                        System.out.println("GET Vizinho: ");
                        ee.printStackTrace();
                    }
                    // Enquanto não for colocada na tabela a entrada com o caminho para o destino do pedido
                    // Enviamos ROUTE_REQUEST com um aumento de tempo e de saltos
                    do {
                        tempo += 150;
                        saltos += 1;
                        sendRR(vizip, tempo, saltos);
                        try {
                            Thread.sleep(tempo+50);
                        }
                        catch(InterruptedException e) {
                            System.out.println("Erro ao Dormir GET N-contem: ");
                            e.printStackTrace();
                        }
                    } while (!tabela.containsKey(vizip) && saltos <= 38 && tempo <= 5000);
                    try {
                        // Se o encontrou ou não
                        if(saltos <= 38 && tempo <= 5000) {
                            soMap.put(dest, socket);
                            viz = "";
                            // Obtem o ip do vizinho e dele próprio
                            for(No n : tabela.values()) {
                                if (n.getIp().equals(InetAddress.getByName(dest))) {
                                    viz = n.getIpVizinho().getHostAddress();
                                    break;
                                }
                            }
                            data = "GET_NEWS_FROM " + source + " " + dest + " " + viz;
                            b = data.getBytes();
                            packet = new DatagramPacket(b, b.length, group, 9999);
                            ms.send(packet);
                            ms.leaveGroup(group);
                            // System.out.println("Sent GET_NEWS_FROM: " + data);
                        }
                        else {
                            Socket s = new Socket("localhost", 9999);
                            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                            out.println("Não foi encontrado nenhum servidor com esse endereço");
                            s.shutdownOutput();
                            s.shutdownInput();
                            s.close();
                        }
                    }
                    catch(Exception e) {
                        System.out.println("TCP N-contem: ");
                        e.printStackTrace();
                    }
                }
            }
            else if(re.split(" ")[0].equals("NEWS_FOR")) {
                String temp = "";
                for(int i = 2; i < re.split(" ").length; i++) {
                    temp += re.split(" ")[i] + " ";
                }
                Socket se = soMap.get(re.split(" ")[1]);
                System.out.println(re);
                try {
                    PrintWriter out = new PrintWriter(se.getOutputStream(), true);
                    out.println(temp);
                    se.close();
                    soMap.remove(re.split(" ")[1]);
                }
                catch(Exception e) {
                    System.out.println("Erro ao enviar noticias!");
                    e.printStackTrace();
                }
                // System.out.println("Got News!\nNews:\n\t" + temp);
            }
        }
    }
}
