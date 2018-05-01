import java.util.Map;
import java.net.InetAddress;
import java.io.BufferedReader;
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
import java.net.DatagramSocket;
import java.util.List;
import java.util.ArrayList;


class TCPThread extends Thread {

    private Map<InetAddress, No> tabela;
    private Map<Message, Integer> toSend;
    private Map<Message, List<InetAddress>> sent;
    private List<String> received;
    private ServerSocket ss;
    private DatagramSocket ds;
    private Socket cliente = null;
    private Socket server = null;
    int copias;

    public TCPThread(DatagramSocket ds, Map<InetAddress, No> tabela, Map<Message, Integer> toSend, Map<Message, List<InetAddress>> sent, List<String> received) {
        this.ds = ds;
        this.tabela = tabela;
        this.received = received;
        this.copias = 5;
    }

    @Override
    public void run() {
        Socket socket = null;
        BufferedReader in;
        String re = "";
        InetAddress dip = null;
        String source = "";
        String dest = "";
        String data = "";
        byte[] b = new byte[1000];
        DatagramPacket packet = null;
        try {
            ss = new ServerSocket(9999);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        while(true) {
            // Aceitas ligações TCP para receber os dados
            try {
                socket = ss.accept();
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                re = in.readLine();
            }
            catch(IOException e) {
                e.printStackTrace();
            }

            if(re.split(" ")[0].equals("SERVER")) {
                server = socket;
            }
            else if(re.split(" ")[0].equals("CLIENT")) {
                cliente = socket;
            }
            else {
                // Se o que recebeu foi um GET_NEWS_FROM ou um NEWS_FOR
                // Se receber um GET_NEWS_FROM:
                // - Ele != Destino -> Faz N copias e envia por UDP
                // - Ele == Destino -> Entrega ao servidor
                // Se receber um NEWS_FOR:
                // - Ele != Destino -> Faz N copias e envia por UDP
                // - Ele == Destino -> Entrega ao servidor

                try {
                    source = InetAddress.getByName(re.split(" ")[1]).getHostAddress();
                    dest = InetAddress.getByName(re.split(" ")[2]).getHostAddress();
                    dip = InetAddress.getByName(dest);
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
                // Se e ele proprio ou nao
                if(tabela.containsKey(dip) && tabela.get(dip).getSaltos() == 0) {

                    try{
                        if(re.split(" ")[0].equals("GET_NEWS_FROM")) {
                            // Entrega ao servidor
                            if(server != null && !received.contains(re)) {
                                PrintWriter out = new PrintWriter(server.getOutputStream(), true);
                                out.println(re);
                            }
                        }
                        else if(re.split(" ")[0].equals("NEWS_FOR")) {
                            // Entrega ao cliente
                            if(cliente != null && !received.contains(re)) {
                                PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);
                                out.println(re);
                            }
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
                else {
                    try{
                        // Faz N copias e envia por UDP
                        InetAddress sip = InetAddress.getByName(source);
                        Message m = new Message(sip, dip, "", 0, true);
                        if(re.split(" ")[0].equals("NEWS_FOR")) {
                            m.setMess(re.split(" ")[3]);
                        }
                        No menor = null;
                        No menor2 = null;
                        for(No n : tabela.values()) {
                            if(menor != null && n.getNumHellos() < menor.getNumHellos()) {
                                menor2 = menor;
                                menor = n;
                            }
                            else if(menor2 != null && n.getNumHellos() < menor2.getNumHellos()) {
                                menor2 = n;
                            }
                            else if(menor == null) {
                                menor = n;
                            }
                            else if(menor2 == null) {
                                menor2 = n;
                            }
                        }
                        b = m.toString().getBytes();
                        packet = new DatagramPacket(b, b.length, InetAddress.getByName(dest), 6666);
                        if(menor != null) {
                            ds.send(packet);
                            if(menor2 != null) {
                                ds.send(packet);
                                copias--;
                            }
                            copias--;
                        }
                        toSend.put(m, copias);
                        sent.put(m, new ArrayList<InetAddress>());
                        copias = 5;
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
