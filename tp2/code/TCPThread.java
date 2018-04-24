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


class TCPThread extends Thread {

    private Map<InetAddress, No> tabela;
    private ServerSocket ss;
    private DatagramSocket ds;
    private Socket cliente = null;
    private Socket server = null;
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
            ds = new DatagramSocket(9999);
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
                    if(re.split(" ")[0].equals("GET_NEWS_FROM")) {
                        // Entrega ao servidor
                        if(server != null) {

                        }
                    }
                    else if(re.split(" ")[0].equals("NEWS_FOR")) {
                        // Entrega ao cliente
                        if(cliente != null) {

                        }
                    }
                }
                else {
                    // Faz N copias e envia por UDP
                    if(re.split(" ")[0].equals("GET_NEWS_FROM")) {

                    }
                    else if(re.split(" ")[0].equals("NEWS_FOR")) {

                    }

                }
            }
        }
    }
}
