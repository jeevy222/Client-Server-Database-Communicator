package finalproject.client;

import finalproject.entities.Person;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientInterface extends JFrame {

    private JPanel controlPanel;
    private JButton Open_Connection;
    private JButton Close_Connection;
    private JButton Send_Data;
    private JButton Query_DB_Data;
    private ActionListener listener;
    private Connection conn;
    private JTextArea textQueryArea;
    JLabel none = new JLabel("<None>");
    JLabel none2 = new JLabel("<None>");

    private PreparedStatement queryStmtName;



    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_PORT = 8001;

    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 400;
    final int AREA_ROWS = 10;
    final int AREA_COLUMNS = 50;

    JComboBox peopleSelect;
    JMenuItem item;
    JFileChooser jFileChooser;
    Socket socket;
    int port;

    public ClientInterface() {
        this(DEFAULT_PORT);

        createControlPanel();
        Open_Connection.addActionListener(new OpenConnectionListener());
        Close_Connection.addActionListener(new CloseConnectionListener() );
        Query_DB_Data.addActionListener(new ClientInterface.QueryButtonListener());
        Send_Data.addActionListener(new ClientInterface.SendButtonListener());
        this.setTitle("Client");

        class ChoiceListener implements ActionListener
        {
            public void actionPerformed(ActionEvent event)
            {
                item.addActionListener(new OpenDBListener());

            }
        }

        listener = new ChoiceListener();
        textQueryArea = new JTextArea(
                AREA_ROWS, AREA_COLUMNS);
        textQueryArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textQueryArea);
        JPanel textPanel = new JPanel();
        textPanel.add(scrollPane);
        this.add(textPanel, BorderLayout.SOUTH);
        this.add(controlPanel, BorderLayout.NORTH);

    }

    public ClientInterface(int port) {
        this.port = port;

    }

    private JPanel createControlPanel() {

        controlPanel = new JPanel();

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        menuBar.add(createFileMenu());

        JPanel inputPanel = new JPanel();
        JLabel label1 = new JLabel("Active DB");
        JLabel label2 = new JLabel("Active Connection");
        JPanel inputPanel2 = createComboBox();
        JPanel buttonPanel = new JPanel();
        JPanel buttonPanel2 = new JPanel();
        Open_Connection = new JButton("Open Connection");
        Close_Connection = new JButton("Close Connection");
        Send_Data = new JButton("Send Data");
        Query_DB_Data = new JButton("Query DB Data");

        inputPanel.add(label1);
        inputPanel.add(none);
        inputPanel.add(label2);
        inputPanel.add(none2);
        inputPanel.setLayout(new GridLayout(2,2));

        buttonPanel.add(Open_Connection);
        buttonPanel.add(Close_Connection);
        buttonPanel2.add(Send_Data);
        buttonPanel2.add(Query_DB_Data);


        controlPanel.add(inputPanel);
        controlPanel.add(inputPanel2);
        controlPanel.add(buttonPanel);
        controlPanel.add(buttonPanel2);

        controlPanel.setLayout(new GridLayout(4,1));
        return controlPanel;

    }

    public JPanel createComboBox()
    {
        peopleSelect = new JComboBox();
        peopleSelect.addItem("Empty");

        peopleSelect.setEditable(true);
        peopleSelect.addActionListener(listener);

        JPanel panel = new JPanel();
        panel.add(peopleSelect);
        return panel;
    }

    public JMenu createFileMenu()
    {
        JMenu menu = new JMenu("File");
        menu.add(createFileOpenItem());
        menu.add(createFileExitItem());
        return menu;
    }

    private void fillComboBox() throws SQLException {

        List<ComboBoxItem> l = getNames();
        peopleSelect.setModel(new DefaultComboBoxModel(l.toArray()));

    }

    private JMenuItem createFileOpenItem() {
        JMenuItem item = new JMenuItem("Open DB");
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setCurrentDirectory(new java.io.File("."));
        jFileChooser.setDialogTitle("Open");
        jFileChooser.setFileSelectionMode(JFileChooser.APPROVE_OPTION);

        class OpenDBListener implements ActionListener
        {

            public void actionPerformed(ActionEvent event)
            {
                int returnVal = jFileChooser.showOpenDialog(getParent());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    System.out.println("You chose to open this file: " + jFileChooser.getSelectedFile().getAbsolutePath());
                    String dbFileName = jFileChooser.getSelectedFile().getAbsolutePath();

                    try {

                        conn = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
                        fillComboBox();
                        none.setText("Client.DB");

                    } catch (Exception e ) {
                        System.err.println("error connection to db: "+ e.getMessage());
                        if (e.getClass() == IOException.class)
                            System.out.println("IO Exception");
                        else
                            System.out.println(e.getMessage());

                    }

                }
            }
        }

        item.addActionListener(new OpenDBListener());
        return item;
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

    class OpenConnectionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            try {
                socket = new Socket("localhost", 8001);
                System.out.println("Connected");
                none2.setText("Connected" + '\n');
            } catch (IOException e1) {

                if (e1.getClass() == IOException.class)
                    System.out.println("IO Exception");
                else
                    System.out.println(e1.getMessage());
            }
        }

    }

    class CloseConnectionListener implements ActionListener {


        public void actionPerformed(ActionEvent e) {
            try {
                none2.setText("Connection Closed" + '\n');
                socket.close();
                System.out.println("Connection Closed");
            } catch (Exception e1) {
                if (e1.getClass() == IOException.class)
                    System.out.println("IO Exception");
                else
                    System.out.println(e1.getMessage());
            }
        }

    }

    class QueryButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event)
        {

            try {
                textQueryArea.setText("");
                PreparedStatement stmt = getQueryStmtName();

                ResultSet rset = stmt.executeQuery();
                ResultSetMetaData rsmd = rset.getMetaData();
                int numColumns = rsmd.getColumnCount();


                String rowString = "";
                for (int i=1;i<=numColumns;i++) {
                    Object o = rsmd.getColumnName(i);
                    rowString += o.toString() + "\t";
                }
                rowString += "\n";
                textQueryArea.setText(rowString);

                while (rset.next()) {
                    for (int i=1;i<=numColumns;i++) {
                        Object o = rset.getObject(i);
                        rowString += o.toString() + "\t";
                    }
                    rowString += "\n";
                }
                System.out.print("->" + rowString);
                textQueryArea.setText(rowString);
            } catch (Exception e) {
                if (e.getClass() == SQLException.class)
                    System.out.println("SQL Exception");
                if(e.getClass() == NullPointerException.class)
                    System.out.println("IO Exception");
                else
                    System.out.println(e.getMessage());
            }

        }

        private PreparedStatement getQueryStmtName() throws SQLException {
            queryStmtName = conn.prepareStatement("Select first ,last, age, city, sent , id from People");
            return queryStmtName;
        }
    }

    class SendButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            try {

                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));

                String query = String.valueOf(peopleSelect.getSelectedItem());
                if(query.length() == 0 || query.equals("null")) {
                    System.out.println(" No person selected");

                    return;
                }
                PreparedStatement queryStmtFromList = conn.prepareStatement("Select first ,last, age, city , id from People where first = ? and last =?");
                queryStmtFromList.setString(1, query.split(" ")[0]);
                queryStmtFromList.setString(2, query.split(" ")[1]);
                ResultSet rset = queryStmtFromList.executeQuery();
                int numColumns = rset.getMetaData().getColumnCount();
                Person person = new Person();
                while (rset.next()) {
                    for (int i=1;i<=numColumns;i++) {
                        switch (i){
                            case 1 :
                                person.setFirst_name(String.valueOf(rset.getObject(i)));
                                break;
                            case 2:
                                person.setLast_name(String.valueOf(rset.getObject(i)));
                                break;
                            case 3:
                                person.setAge(String.valueOf(rset.getObject(i)));
                                break;
                            case 4:
                                person.setCity(String.valueOf(rset.getObject(i)));
                                break;
                            case 5:
                                person.setID(String.valueOf(rset.getObject(i)));
                                break;
                        }
                    }
                }


                ObjectOutputStream os=new ObjectOutputStream(socket.getOutputStream());
                os.writeObject(person);
                os.flush();


                String response = br.readLine();
                if (response != null) {
                    System.out.println("Success");
                    PreparedStatement updateSentInClient = conn.prepareStatement(" UPDATE People SET sent = 1 where first = ? and last =?");
                    updateSentInClient.setString(1, query.split(" ")[0]);
                    updateSentInClient.setString(2, query.split(" ")[1]);
                    updateSentInClient.executeUpdate();
                    fillComboBox();
                } else {
                    System.out.println("Failed");
                }
            } catch (IOException | SQLException e1) {
                if (e1.getClass() == IOException.class)
                    System.out.println("IO Exception");
                else
                    System.out.println(e1.getMessage());
            }

        }

    }

    private List<ComboBoxItem> getNames() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("Select first ,last , id from People where sent = 0");
        ResultSet rset = stmt.executeQuery();
        int numColumns = rset.getMetaData().getColumnCount();
        List<ComboBoxItem> comboBoxItemList = new ArrayList<>();
        while (rset.next()) {
            ComboBoxItem comboBoxItem = new ComboBoxItem();
            StringBuilder sb = new StringBuilder();
            for (int i=1;i<=numColumns;i++) {
                if (i == 1)
                    sb.append(rset.getObject(i));
                else if (i == 2) {
                    sb.append(" ");
                    sb.append(rset.getObject(i));
                    comboBoxItem.setName(sb.toString());
                }
                if(i == 3) {
                    comboBoxItem.setId((Integer) rset.getObject(i));
                    sb = new StringBuilder();
                }
            }
            comboBoxItemList.add(comboBoxItem);
        }
        return comboBoxItemList;
    }

    class ComboBoxItem {
        private int id;
        private String name;

        public void setId(int id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ComboBoxItem() {
        }

        public ComboBoxItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.name;
        }
    }


    class OpenDBListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            int returnVal = jFileChooser.showOpenDialog(getParent());
            if (returnVal == JFileChooser.APPROVE_OPTION) {



                System.out.println("You chose to open this file: " + jFileChooser.getSelectedFile().getAbsolutePath());
                String dbFileName = jFileChooser.getSelectedFile().getAbsolutePath();
                try {
                    none.setText("Client.DB");

                } catch (Exception e ) {
                    System.err.println("error connection to db: "+ e.getMessage());
                    e.printStackTrace();

                }

            }
        }
    }




    public static void main(String[] args) {
        JFrame frame = new ClientInterface();
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setCurrentDirectory(new java.io.File("."));
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);


    }
}
