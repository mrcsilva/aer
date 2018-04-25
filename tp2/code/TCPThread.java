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
    private Map<InetAddress, List<Message>> messages;
    private ServerSocket ss;
    private DatagramSocket ds;
    private Socket cliente = null;
    private Socket server = null;
    private String noticia = "";

    public TCPThread(Map<InetAddress, No> tabela, Map<InetAddress, List<Message>> messages) {
        this.tabela = tabela;
        this.messages = messages;
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
                            PrintWriter out = new PrintWriter(server.getOutputStream(), true);
                            out.println(re);
                        }
                    }
                    else if(re.split(" ")[0].equals("NEWS_FOR")) {
                        // Entrega ao cliente
                        if(cliente != null) {
                            PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);
                            out.println(re);
                        }
                    }
                }
                else {
                    // Faz N copias e envia por UDP
                    if(re.split(" ")[0].equals("GET_NEWS_FROM")) {
                        InetAddress sip = InetAddress.getByName(source);
                        Message m = new Message(sip, dip, re.split(" ")[3], 0, true);
                    }
                    else if(re.split(" ")[0].equals("NEWS_FOR")) {
                        InetAddress sip = InetAddress.getByName(source);
                        Message m = new Message(sip, dip, re.split(" ")[3], 0, false);
                    }

                }
            }
        }
    }
}
