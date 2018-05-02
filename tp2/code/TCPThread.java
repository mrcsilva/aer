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

    // Tabela com as conexoes atuais
    private Map<InetAddress, No> tabela;
    // Mensagens a enviar
    private Map<Message, Integer> toSend;
    // Associaçao de mensagens com endereços (evitar envio para IPs repetidos)
    private Map<Message, List<InetAddress>> sent;
    // Lista de mensagens respondidas
    private List<String> received;
    // Socket a espera de conexoes na porta 9999
    private ServerSocket ss;
    // Socket para envio de mensagens UDP
    private DatagramSocket ds;
    // Associação de socket do cliente com o IP destino
    // Ira ser utilizado para envio posterior da resposta
    // Permite ter varios clientes e pedidos em simultaneo
    private Map<Socket, String> clients = null;
    // Socket com conexao ao servidor
    private Socket server = null;

    public TCPThread(DatagramSocket ds, Map<InetAddress, No> tabela, Map<Message, Integer> toSend, Map<Message, List<InetAddress>> sent, List<String> received) {
        this.ds = ds;
        this.tabela = tabela;
        this.received = received;
        this.toSend = toSend;
        this.sent = sent;
        clients = new HashMap<>();
    }

    @Override
    public void run() {
        Socket socket = null;
        Socket cliente = null;
        BufferedReader in;
        String re = "";
        InetAddress dip = null;
        String source = "";
        String dest = "";
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
                // Thread que fica responsavel pela comunicacao com o servidor
                HandleSocket hs = new HandleSocket(socket, true, ds, tabela, toSend, sent, null);
                hs.start();
                server = socket;
            }
            else if(re.split(" ")[0].equals("CLIENT")) {
                // Thread responsavel pela comunicacao com o cliente
                HandleSocket hs = new HandleSocket(socket, false, ds, tabela, toSend, sent, clients);
                hs.start();
            }
            else {
                // System.out.println("Recebido TCP: " + re);

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
                            // Se ainda existir conexao com o servidor e ainda
                            // nao tiver sido enviada essa mensagem -> Send
                            if(server != null && !received.contains(re)) {
                                // System.out.println("Entregue ao servidor");
                                PrintWriter out = new PrintWriter(server.getOutputStream(), true);
                                out.println(re);
                                received.add(re);
                            }
                        }
                        else if(re.split(" ")[0].equals("NEWS_FOR")) {
                            // Obtem o socket do cliente ao qual ira entregar a mensagem
                            for(Map.Entry<Socket, String> entry : clients.entrySet()) {
                                if(entry.getValue().equals(source)) {
                                    cliente = entry.getKey();
                                    break;
                                }
                            }

                            // Se tiver encontrado e ainda nao tiver enviado essa mensagem
                            // para o cliente -> Send
                            if(cliente != null && !received.contains(re)) {
                                // System.out.println("Entregue ao cliente");
                                PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);
                                out.println(re);
                                received.add(re);
                                clients.remove(cliente);
                                cliente = null;
                            }
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

class HandleSocket extends Thread {

    private Socket server;
    private Socket client;
    private DatagramSocket ds;
    private Map<InetAddress, No> tabela;
    private Map<Message, Integer> toSend;
    private Map<Message, List<InetAddress>> sent;
    private Map<Socket, String> clients;
    int copias = 5;

    public HandleSocket(Socket s, boolean type, DatagramSocket ds, Map<InetAddress, No> tabela, Map<Message, Integer> toSend, Map<Message, List<InetAddress>> sent, Map<Socket, String> clients) {
        // True - Server
        // False - Client
        if(type == true) {
            this.server = s;
            this.client = null;
        }
        else {
            this.client = s;
            this.server = null;
        }
        this.ds = ds;
        this.tabela = tabela;
        this.toSend = toSend;
        this.sent = sent;
        this.clients = clients;
    }

    /**
     * Esta funcao tem como objetivo a criacao das N copias e a efetuar imediatamente
     * o envio no maximo de duas copias, caso seja possivel
     * @param re Mensagem a efetuar copias
     */
    private void sendCopies(String re) throws Exception{
        DatagramPacket packet = null;
        byte[] b = null;
        InetAddress dip, sip;

        dip = InetAddress.getByName(re.split(" ")[2]);
        sip = InetAddress.getByName(re.split(" ")[1]);

        Message m = new Message(sip, dip, "", System.currentTimeMillis(), true);
        if(re.split(" ")[0].equals("NEWS_FOR")) {
            m.setMess(re.split(" ")[3]);
            m.setType("NEWS_FOR");
        }

        // Escolhe os dois nos ligados a menos tempo
        No menor = null;
        No menor2 = null;
        for(No n : tabela.values()) {
            if(menor != null && n.getNumHellos() < menor.getNumHellos() && n.getSaltos() != 0) {
                menor2 = menor;
                menor = n;
            }
            else if(menor2 != null && n.getNumHellos() < menor2.getNumHellos() && n.getSaltos() != 0) {
                menor2 = n;
            }
            else if(menor == null && n.getSaltos() != 0) {
                menor = n;
            }
            else if(menor2 == null && n.getSaltos() != 0) {
                menor2 = n;
            }
        }

        b = m.toString().getBytes();
        sent.put(m, new ArrayList<InetAddress>());

        // Caso existam nos na tabela envia as mensagens
        if(menor != null) {
            packet = new DatagramPacket(b, b.length, menor.getIp(), 6666);
            ds.send(packet);
            sent.get(m).add(menor.getIp());
            // System.out.println("Sent to: " + menor.getIp().getHostAddress());
            if(menor2 != null) {
                packet = new DatagramPacket(b, b.length, menor2.getIp(), 6666);
                ds.send(packet);
                sent.get(m).add(menor2.getIp());
                // System.out.println("Sent to: " + menor2.getIp().getHostAddress());
                copias--;
            }
            copias--;
        }
        toSend.put(m, copias);
        copias = 5;
    }

    @Override
    public void run() {
        String re;

        while(server != null) {
            try{
                BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
                re = in.readLine();

                if(re != null) {
                    // System.out.println("Envia copias: " + re);
                    // Faz N copias e envia por UDP
                    sendCopies(re);
                }
            }
            catch(Exception e){
                e.printStackTrace();
                server = null;
            }
        }

        while(client != null) {
            try{
                if(!client.isClosed()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    re = in.readLine();
                    if(re != null) {
                        clients.put(client, InetAddress.getByName(re.split(" ")[2]).getHostAddress());
                        // System.out.println("Envia copias: " + re);
                        // Faz N copias e envia por UDP
                        sendCopies(re);
                    }
                }
                else {
                    client = null;
                }
            }
            catch(Exception e){
                e.printStackTrace();
                client = null;
            }
        }
    }

}
