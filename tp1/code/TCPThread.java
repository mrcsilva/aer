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

class TCPThread extends Thread {

    private Map<InetAddress, No> tabela;
    private ServerSocket ss;
    private MulticastSocket ms;

    public TCPThread(Map<InetAddress, No> tabela) {
        this.tabela = tabela;
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
            System.out.println("sendRR: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        Socket socket;
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
            System.out.println("Group TCP: " + e.getMessage());
        }
        while(true) {
            // Aceitas ligações TCP para receber os dados
            try {
                socket = ss.accept();
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                re = in.readLine();
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
            }
            catch(IOException e) {
                System.out.println("Socket Accept: " + e.getMessage());
            }
            // Se o que recebeu foi um GET_NEWS_FROM ou um NEWS_FOR
            if(re.split(" ")[0].equals("GET_NEWS_FROM")) {
                dest = re.split(" ")[1];
                try {
                    ms.joinGroup(group);
                    dip = InetAddress.getByName(dest);
                }
                catch(IOException e) {
                    System.out.println("GET DIP IO: " + e.getMessage());
                }
                // Se a tabela já contem o destino ou não
                if(tabela.containsKey(dip)) {
                    try {
                        source = "";
                        viz = "";
                        // Obtem o seu próprio endereço e o do vizinho
                        for(No n : tabela.values()) {
                            if(n.getSaltos() == 0) {
                                source = n.getIp().getHostAddress();
                            }
                            else if (n.getIp().getHostAddress().equals(dest)) {
                                viz = n.getIpVizinho().getHostAddress();
                            }
                        }
                        data = "GET_NEWS_FROM " + source + " " + dest + " " + viz;
                        b = data.getBytes();
                        packet = new DatagramPacket(b, b.length, group, 9999);
                        ms.send(packet);
                        ms.leaveGroup(group);
                        ms.close();
                        System.out.println("SENT GET_NEWS_FROM to: " + viz);
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
                        System.out.println("GET Vizinho: " + ee.getMessage());
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
                            System.out.println("Erro ao Dormir GET N-contem: " + e.getMessage());
                        }
                    } while (!tabela.containsKey(vizip) && saltos <= 38 && tempo <= 5000);
                    try {
                        // Se o encontrou ou não
                        if(saltos <= 38 && tempo <= 5000) {
                            source = "";
                            viz = "";
                            // Obtem o ip do vizinho e dele próprio
                            for(No n : tabela.values()) {
                                if(n.getSaltos() == 0) {
                                    source = n.getIp().getHostAddress();
                                }
                                else if (n.getIp().getHostAddress().equals(dest)) {
                                    viz = n.getIpVizinho().getHostAddress();
                                }
                            }
                            data = "GET_NEWS_FROM " + source + " " + dest + " " + viz;
                            b = data.getBytes();
                            packet = new DatagramPacket(b, b.length, group, 9999);
                            ms.send(packet);
                            ms.leaveGroup(group);
                            ms.close();
                            System.out.println("Sent GET n-contem to: " + viz);
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
                        System.out.println("TCP N-contem: " + e.getMessage());
                    }
                }
            }
            else if(re.split(" ")[0].equals("NEWS_FOR")) {
                dest = re.split(" ")[1];
                try {
                    ms.joinGroup(group);
                    dip = InetAddress.getByName(dest);
                }
                catch(Exception e) {
                    System.out.println("DIP TCP NEWS: " + e.getMessage());
                }
                // Existe o destino na tabela ou não
                if(tabela.containsKey(dip)) {
                    try {
                        source = "";
                        viz = "";
                        // Obtem o seu IP e o do vizinho
                        for(No n : tabela.values()) {
                            if(n.getSaltos() == 0) {
                                source = n.getIp().getHostAddress();
                            }
                            else if (n.getIp().getHostAddress().equals(dest)) {
                                viz = n.getIpVizinho().getHostAddress();
                            }
                        }
                        data = "NEWS_FOR " + source + " " + dest + " " + viz;
                        b = data.getBytes();
                        packet = new DatagramPacket(b, b.length, group, 9999);
                        ms.send(packet);
                        ms.leaveGroup(group);
                        ms.close();
                    }
                    catch(Exception e) {
                        System.out.println("TCP Send News Contem: " + e.getMessage());
                    }
                }
                else {
                    int tempo = 200;
                    int saltos = 6;
                    dip = null;
                    try {
                        dip = InetAddress.getByName(dest);
                    }
                    catch(Exception e) {
                        System.out.println("TCP DIP: " + e.getMessage());
                    }
                    // Descoberta do destino
                    do {
                        tempo += 150;
                        saltos += 1;
                        sendRR(dip, tempo, saltos);
                        try {
                            Thread.sleep(tempo+50);
                        }
                        catch(InterruptedException e) {
                            System.out.println("Erro ao Dormir SEND N-contem: " + e.getMessage());
                        }
                    } while (!tabela.containsKey(dip) && saltos <= 38 && tempo <= 5000);
                    try {
                        // Se encontrou o destino
                        if(saltos <= 38 && tempo <= 5000) {
                            source = "";
                            viz = "";
                            String[] temp = re.split(" ");
                            String dataTcp = "";
                            for(No n : tabela.values()) {
                                if(n.getSaltos() == 0) {
                                    source = n.getIp().getHostAddress();
                                }
                                else if (n.getIp().getHostAddress().equals(dest)) {
                                    viz = n.getIpVizinho().getHostAddress();
                                }
                            }
                            for (int i = 3; i < temp.length; i++) {
                                dataTcp += temp[i]+" ";
                            }
                            data = "NEWS_FOR " + source + " " + dest + " " + viz + " " + dataTcp;
                            b = data.getBytes();
                            packet = new DatagramPacket(b, b.length, group, 9999);
                            ms.send(packet);
                            ms.leaveGroup(group);
                            ms.close();
                        }
                    }
                    catch(Exception e) {
                        System.out.println("TCP Send News N-contem: " + e.getMessage());
                    }
                }
            }
        }
    }
}
