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
        try {
            group = InetAddress.getByName("FF02::1");
            ss = new ServerSocket(9999);
            ms = new MulticastSocket(9999);
        } catch(Exception e) {
            System.out.println("Group TCP: " + e.getMessage());
        }
        try {
            while((socket = ss.accept()) != null) {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                re = in.readLine();
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
                if(re.split(" ")[0].equals("GET_NEWS_FROM")) {
                    String dest = re.split(" ")[1];
                    ms.joinGroup(group);
                    if(tabela.containsKey(InetAddress.getByName(dest))) {
                        try {
                            String source = "";
                            String viz = "";
                            for(No n : tabela.values()) {
                                if(n.getSaltos() == 0) {
                                    source = n.getIp().getHostAddress();
                                }
                                else if (n.getIp().getHostAddress().equals(dest)) {
                                    viz = n.getIpVizinho().getHostAddress();
                                }
                            }
                            String data = "GET_NEWS_FROM " + source + " " + dest + " " + viz;
                            byte[] b = new byte[1000];
                            b = data.getBytes();
                            DatagramPacket packet = new DatagramPacket(b, b.length, group, 9999);
                            ms.send(packet);
                            ms.leaveGroup(group);
                            ms.close();
                        }
                        catch(Exception e) {
                            System.out.println("TCP Contem: " + e.getMessage());
                        }
                    }
                    else {
                        int tempo = 200;
                        int saltos = 6;
                        do {
                            tempo += 150;
                            saltos += 1;
                            sendRR(InetAddress.getByName(dest), tempo, saltos);
                            Thread.sleep(tempo+50);
                        } while (!tabela.containsKey(InetAddress.getByName(dest)) && saltos <= 38 && tempo <= 5000);
                        try {
                            if(saltos <= 38 && tempo <= 5000) {
                                String source = "";
                                String viz = "";
                                for(No n : tabela.values()) {
                                    if(n.getSaltos() == 0) {
                                        source = n.getIp().getHostAddress();
                                    }
                                    else if (n.getIp().getHostAddress().equals(dest)) {
                                        viz = n.getIpVizinho().getHostAddress();
                                    }
                                }
                                String data = "GET_NEWS_FROM " + source + " " + dest + " " + viz;
                                byte[] b = new byte[1000];
                                b = data.getBytes();
                                DatagramPacket packet = new DatagramPacket(b, b.length, group, 9999);
                                ms.send(packet);
                                ms.leaveGroup(group);
                                ms.close();
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
                    String dest = re.split(" ")[1];
                    ms.joinGroup(group);
                    dip = null;
                    try {
                        dip = InetAddress.getByName(dest);
                    } catch(Exception e) {
                        System.out.println("DIP TCP NEWS: " + e.getMessage());
                    }
                    if(tabela.containsKey(dip)) {
                        try {
                            String source = "";
                            String viz = "";
                            for(No n : tabela.values()) {
                                if(n.getSaltos() == 0) {
                                    source = n.getIp().getHostAddress();
                                }
                                else if (n.getIp().getHostAddress().equals(dest)) {
                                    viz = n.getIpVizinho().getHostAddress();
                                }
                            }
                            String data = "NEWS_FOR " + source + " " + dest + " " + viz;
                            byte[] b = new byte[1000];
                            b = data.getBytes();
                            DatagramPacket packet = new DatagramPacket(b, b.length, group, 9999);
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
                        do {
                            tempo += 150;
                            saltos += 1;
                            sendRR(dip, tempo, saltos);
                            Thread.sleep(tempo+50);
                        } while (!tabela.containsKey(dip) && saltos <= 38 && tempo <= 5000);
                        try {
                            if(saltos <= 38 && tempo <= 5000) {
                                String source = "";
                                String viz = "";
                                for(No n : tabela.values()) {
                                    if(n.getSaltos() == 0) {
                                        source = n.getIp().getHostAddress();
                                    }
                                    else if (n.getIp().getHostAddress().equals(dest)) {
                                        viz = n.getIpVizinho().getHostAddress();
                                    }
                                }
                                String data = "NEWS_FOR " + source + " " + dest + " " + viz;
                                byte[] b = new byte[1000];
                                b = data.getBytes();
                                DatagramPacket packet = new DatagramPacket(b, b.length, group, 9999);
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
        catch(Exception e) {
            System.out.println("Accept: " + e.getMessage());
        }
    }
}
