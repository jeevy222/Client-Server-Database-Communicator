package finalproject.server;

import finalproject.entities.Person;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Date;

public class Server extends JFrame implements Runnable {

    public static final int DEFAULT_PORT = 8001;
    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 800;
    final int AREA_ROWS = 10;
    final int AREA_COLUMNS = 40;

    private int clientNo = 0;

    private JTextArea ta;
    private JPanel controlPanel;
    private JButton Query;
    private ObjectOutputStream outputToFile;
    private ObjectInputStream inputFromClient;

    private PreparedStatement queryStmtName;
    private Connection conn;




    public Server(String dbFile) throws IOException, SQLException {
        this(DEFAULT_PORT, dbFile);
        conn = DriverManager.getConnection("jdbc:sqlite:server.db");
        createControlPanel();
        ta = new JTextArea(10,10);
        JScrollPane sp = new JScrollPane(ta);
        this.add(sp,BorderLayout.CENTER);
        this.setTitle("Server");
        this.setSize(400,200);
        this.add(controlPanel, BorderLayout.NORTH);
        Thread t = new Thread(this);
        t.start();

        Query.addActionListener(new Server.QueryButtonListener());

    }

    public Server(int port, String dbFile) throws IOException, SQLException {

        this.setSize(Server.FRAME_WIDTH, Server.FRAME_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    private JPanel createControlPanel() {

        controlPanel = new JPanel();

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        menuBar.add(createFileMenu());

        JLabel label1 = new JLabel("DB:Server.db");

        Query = new JButton("Query DB");
        controlPanel.add(label1);
        controlPanel.add(Query);

        return controlPanel;

    }
    public JMenu createFileMenu()
    {
        JMenu menu = new JMenu("File");
        menu.add(createFileExitItem());
        return menu;
    }

    public JMenuItem createFileExitItem()
    {
        JMenuItem item = new JMenuItem("Exit");
        class MenuItemListener implements ActionListener
        {
            public void actionPerformed(ActionEvent event)
            {
                System.exit(0);
            }
        }
        ActionListener listener = new MenuItemListener();
        item.addActionListener((e) -> System.exit(0));
        return item;
    }


    class QueryButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event)
        {

            try {
                PreparedStatement stmt = getQueryStatement();
                ResultSet rset = stmt.executeQuery();
                ResultSetMetaData rsmd = rset.getMetaData();
                int numColumns = rsmd.getColumnCount();

                String rowString = "";
                for (int i=1;i<=numColumns;i++) {
                    Object o = rsmd.getColumnName(i);
                    rowString += o.toString() + "\t";
                }
                rowString += "\n";

                while (rset.next()) {
                    for (int i=1;i<=numColumns;i++) {
                        Object o = rset.getObject(i);
                        rowString += o.toString() + "\t";
                    }
                    rowString += "\n";
                }
                System.out.print("->" + rowString);
                ta.append(rowString);
            } catch (SQLException e) {
                System.out.println(e);
            }

        }

        private PreparedStatement getQueryStatement() throws SQLException {
            queryStmtName = conn.prepareStatement("Select first ,last, age, city, sent , id from People");
            return queryStmtName;
        }
    }


    public static void main(String[] args) {



        try {
            JFrame sv = new Server("Server.db");

            sv.setVisible(true);
            sv.setSize(FRAME_WIDTH, FRAME_HEIGHT);
            sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            sv.setVisible(true);


        } catch (IOException | SQLException e) {
            System.out.println(e);
        }
    }

    @Override
    public void run() {
        try {

            ServerSocket serverSocket = new ServerSocket(8001);
            ta.append("Listening at Port 8001" +'\n');

            while (true) {
                Socket socket = serverSocket.accept();
                clientNo++;

                ta.append('\n' + "Starting thread for client " + clientNo +
                        " at " + new Date() + '\n');


                InetAddress inetAddress = socket.getInetAddress();
                ta.append("Client " + clientNo + "'s host name is "
                        + inetAddress.getHostName() + "\n");
                ta.append("Client " + clientNo + "'s IP Address is "
                        + inetAddress.getHostAddress() + "\n");
                ta.append("Listening for input from Client " + clientNo + "\n");

                new Thread(new HandleAClient(socket,clientNo)).start();
            }

        }
        catch(IOException ex) {

            if (ex.getClass() == IOException.class)
                System.out.println("IO Exception");
            else
                System.out.println(ex.getMessage());
        }

    }
    class HandleAClient implements Runnable {
        private Socket socket;
        private int clientNum;


        public HandleAClient(Socket socket,int clientNum) {
            this.socket = socket;
            this.clientNum = clientNum;
        }

        public void run() {
            try {

                ObjectOutputStream outputToClient = new ObjectOutputStream(
                        socket.getOutputStream());
                
                while (true) {
                    ObjectInputStream inputFromClient = new ObjectInputStream(
                            socket.getInputStream());


                    Object object = inputFromClient.readObject();

                    Person person = (Person) object;
                    ta.append("got Person " + person.toString() + '\n' + " Inserting into DB " + "\n");
                    ta.append("\n");
                    PreparedStatement updateSentInClient = conn.prepareStatement(" INSERT INTO People (first, last, age, city, sent, id) VALUES (?,?,?,?,?,?)");
                    updateSentInClient.setString(1, person.getFirst_name());
                    updateSentInClient.setString(2, person.getLast_name());
                    updateSentInClient.setString(3, person.getAge());
                    updateSentInClient.setString(4, person.getCity());
                    updateSentInClient.setString(5, "1");
                    updateSentInClient.setString(6, person.getID());
                    updateSentInClient.executeUpdate();
                    System.out.println("A new student object is stored");

                    ObjectOutputStream os=new ObjectOutputStream(socket.getOutputStream());
                    os.writeUTF("Success\n");
                    os.flush();

                }
            }
            catch(IOException | ClassNotFoundException | SQLException ex) {
                if (ex.getClass() == EOFException.class)
                    System.out.println("IO Exception");
                else
                    System.out.println(ex.getMessage());
            } finally {
                try {
                    ta.append("Connection Closed" + '\n');
                    inputFromClient.close();
                    outputToFile.close();
                }
                catch (Exception ex) {
                    if(ex.getClass() == NullPointerException.class)
                        System.out.println("IO Exception");
                    else
                        System.out.println(ex.getMessage());
                }
            }
        }
    }
}
