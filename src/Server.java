
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    private static final int PORT = 9090;
    //private static int PEER_PORT = 9900;

    private static final HashMap<String, Boolean> onlineUsers = new HashMap<>();
    //private static final HashMap<String, Integer> usersPorts = new HashMap<>();

    private static Connection connection;
    private static Statement statement;
    private static ResultSet resultSet;

    private static void ConnectToSQL() {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            try {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "");
                statement = connection.createStatement();
            } catch (SQLException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ClassNotFoundException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static void DeleteUser(String username) {
        try {
            CallableStatement cs = connection.prepareCall("{call deleteUser(?)}");
            cs.setString(1, username);
            cs.execute();
        } catch (SQLException e) {
        }
    }

    private static void CreateGroup(String groupName, String owner) {
        try {
            CallableStatement cs = connection.prepareCall("{call createGroup(?, ?)}");
            cs.setString(1, groupName);
            cs.setString(2, owner);
            cs.execute();

        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static void DeleteGroup(String groupName, String owner) {
        try {
            CallableStatement cs = connection.prepareCall("{call deleteGroup(?, ?)}");
            cs.setString(1, groupName);
            cs.setString(2, owner);
            cs.execute();

        } catch (SQLException e) {
        }
    }

    private static void AddUsersToGroup(String owner, String groupName, ArrayList<String> users) {

        try {

            for (String user : users) {
                if (CheckDatabase(user)) {
                    CallableStatement cs = connection.prepareCall("{call addUserToGroup(?, ?, ?)}");
                    cs.setString(1, groupName);
                    cs.setString(2, owner);
                    cs.setString(3, user);
                    cs.execute();
                }
            }

        } catch (SQLException e) {
        }
    }

    private static void DeleteUsersFromGroup(String owner, String groupName, ArrayList<String> users) {

        try {

            for (String user : users) {

                CallableStatement cs = connection.prepareCall("{call deleteUserFromGroup(?, ?, ?)}");
                cs.setString(1, groupName);
                cs.setString(2, owner);
                cs.setString(3, user);
                cs.execute();

            }

        } catch (SQLException e) {
        }
    }

    private static void AddMetaData(String filename, Integer port, String ip, String sender, String group) {
        try {
            CallableStatement cs = connection.prepareCall("{call addMetaData(?, ?, ?, ?, ?)}");
            cs.setString(1, filename);
            cs.setString(2, ip);
            cs.setString(3, port.toString());
            cs.setString(4, sender);
            cs.setString(5, group);
            cs.execute();

        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static UserFile FindFileAtUsers(String clientName, String filename) {
        try {
            String name = null, ip = null, user = null, group = null;
            Integer port = null;
            CallableStatement cs = connection.prepareCall("{call findFileAtUsers(?)}");
            cs.setString(1, filename);
            cs.execute();
            ResultSet rs = cs.getResultSet();
            while (rs.next()) {
                name = rs.getString("name");
                ip = rs.getString("ip");
                port = Integer.parseInt(rs.getString("port"));
                user = rs.getString("userName");
                group = rs.getString("group_owned");

                if (CheckIfUserInGroup(clientName, group, user) == true) {
                    return new UserFile(name, ip, port, user, group, onlineUsers.get(user));
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }

        return null;
    }

    private static String GetUserGroup(String user) {
        try {
            CallableStatement cs = connection.prepareCall("{call getUserGroup(?)}");
            cs.setString(1, user);
            cs.execute();
            ResultSet rs = cs.getResultSet();

            if (rs.next()) {

                return rs.getString("groupName");
            }
        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }

        return null;
    }

    private static boolean CheckIfUserInGroup(String user, String group, String group_owner) {

        if (group == null) {
            return true;
        }

        try {
            CallableStatement cs = connection.prepareCall("{call checkIfUserInGroup(?, ?, ?)}");
            cs.setString(1, user);
            cs.setString(2, group);
            cs.setString(3, group_owner);
            cs.execute();
            ResultSet rs = cs.getResultSet();

            if (rs.next()) {

                return true;
            }
        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }

        return false;
    }

    private static ArrayList<String> GetAllRegisteredUsers() {

        ArrayList<String> allUsers = new ArrayList<>();

        try {
            CallableStatement cs = connection.prepareCall("{call getAllRegisteredUsers()}");
            cs.execute();
            ResultSet rs = cs.getResultSet();
            while (rs.next()) {
                String name = rs.getString("name");
                allUsers.add(name);

            }

        } catch (SQLException e) {
        }

        return allUsers;
    }

    private static boolean CheckDatabase(String username) {

        try {
            CallableStatement cs = connection.prepareCall("{call checkDatabase(?)}");
            cs.setString(1, username);
            cs.execute();
            ResultSet rs = cs.getResultSet();
            if (rs.next()) {

                return true;

            }
        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }

        return false;
    }

    private static void UpdatePortAtUserInDB(String user, Integer port) {
        try {
            CallableStatement cs = connection.prepareCall("{call updatePortAtUser(?, ?)}");
            cs.setString(1, user);
            cs.setInt(2, port);
            cs.execute();
        } catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void Register(String username, String password) {
        try {
            CallableStatement cs = connection.prepareCall("{call register(?, ?)}");
            cs.setString(1, username);
            cs.setString(2, password);
            cs.execute();

        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static Boolean LogIn(String username, String password) {

        try {
            CallableStatement cs = connection.prepareCall("{call login(?, ?)}");
            cs.setString(1, username);
            cs.setString(2, password);
            cs.execute();
            ResultSet rs = cs.getResultSet();

            if (rs.next()) {

                return true;
            }

        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }

        return false;
    }

    private static String getSQLCommands(String filename) {
        StringBuilder triggerQuery = new StringBuilder();
        try {
            FileInputStream inputStream = new FileInputStream(filename);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                triggerQuery.append(line);
            }
        } catch (IOException e) {
            System.out.println("Exception while Reading MySQL_Trigger.txt File");
        }
        return triggerQuery.toString();
    }

    private static void executeStatements(String scriptName) {
        String queries = getSQLCommands(scriptName);
        String[] querryArray = queries.split(";");

        for (String query : querryArray) {
            try {
                statement.execute(query);
            } catch (SQLException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void executeStatement(String scriptName) {
        String query = getSQLCommands(scriptName);

        try {
            statement.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String args[]) throws SQLException {

        try {
            Socket socket;

            ServerSocket listener = new ServerSocket(PORT);

            // connect to Database
            ConnectToSQL();
            executeStatements("create_tables.sql");
            executeStatement("register.sql");
            executeStatement("checkDatabase.sql");
            executeStatement("login.sql");
            executeStatement("updatePortAtUser.sql");
            executeStatement("getAllRegisteredUsers.sql");
            executeStatement("createGroup.sql");
            executeStatement("deleteGroup.sql");
            executeStatement("addUserToGroup.sql");
            executeStatement("deleteUserFromGroup.sql");
            executeStatement("addMetaData.sql");
            executeStatement("getUserGroup.sql");
            executeStatement("checkIfUserInGroup.sql");
            executeStatement("findFileAtUsers.sql");
            executeStatement("deleteUser.sql");

            for (String user : GetAllRegisteredUsers()) {
                onlineUsers.put(user, Boolean.FALSE);
            }

            System.out.println((char) 27 + "[38;42m Server is listening...");

            while (true) {
                try {
                    socket = listener.accept();

                    System.out.println("Connection established...");
                    new ServerThread(socket).start();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            executeStatements("drop_tables.sql");
        }

    }

    private static class ServerThread extends Thread {

        String line = null;
        BufferedReader is = null;
        PrintWriter os = null;
        Socket s = null;
        String clientName = null;
        Boolean signedIn = Boolean.FALSE, signedUp = Boolean.FALSE;

        public ServerThread(Socket s) {
            this.s = s;
        }

        @Override
        public void run() {
            try {
                is = new BufferedReader(new InputStreamReader(s.getInputStream()));
                os = new PrintWriter(s.getOutputStream(), true);

            } catch (IOException e) {
                System.out.println("IO error in server thread");
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
            }

            try {

                while (true) {

                    while (!signedIn) {
                        os.println("Sign Up or Sign In");
                        line = is.readLine();

                        System.out.println("Logged out now. I got: " + line);

                        if (line.toLowerCase().equals("sign up")) {
                            os.println("signup:name");
                            // got name
                            line = is.readLine();
                            String cName = line;

                            os.println("signup:password");
                            // got psswd
                            line = is.readLine();
                            String cPsswd = line;

                            // daca nu exista, adauga-l!
                            if (!CheckDatabase(cName)) {
                                Register(cName, cPsswd);
                                os.println("Registered Client");
                            } else {
                                // start again
                                os.println("Sign Up Failed");
                            }
                        } else if (line.toLowerCase().equals("sign in")) {
                            os.println("signin:name");
                            // got name
                            line = is.readLine();

                            // the client has this name when is signed in!
                            // even if he is a hecker:)
                            clientName = line;

                            os.println("signin:password");
                            // got psswd
                            line = is.readLine();
                            String clientPsswd = line;

                            if (LogIn(clientName, clientPsswd)) {
                                os.println("Signed In");

                                // he is signed in, he is surely online
                                synchronized (onlineUsers) {
                                    onlineUsers.put(clientName, Boolean.TRUE);
                                }

                                String userss = "";
                                for (String s : GetAllRegisteredUsers()) {
                                    userss += s + " ";
                                }

                                // send users
                                os.println(userss);

                                // update and send the port
                                int clientPort = this.s.getPort() + 1000;

                                UpdatePortAtUserInDB(clientName, clientPort);
                                os.println(clientPort);

                                signedIn = true;
                            } else {
                                os.println("Sign In Failed");
                            }
                        }
                    }

                    // get a command from the client
                    line = is.readLine();

                    if (line.toLowerCase().contains("upload ")) {
                        System.out.println("Got from client " + clientName + ", the command: " + line);

                        // create user's dir within Server dir, if it doesn't exist
                        File dirs = new File("Server\\" + clientName);
                        if (!dirs.exists()) {
                            dirs.mkdirs();
                        }

                        String[] comm = line.split(" ");

                        for (int i = 1; i < comm.length; i++) {
                            String filename = comm[i];

                            // get the file now
                            line = is.readLine();

                            // file doesn't exist
                            if (line.equals("failedUpload")) {
                                continue;
                            }

                            try (FileOutputStream out = new FileOutputStream("Server\\" + clientName + "\\" + filename)) {
                                out.write(line.getBytes());
                            }

                            //port
                            line = is.readLine();
                            Integer port = Integer.parseInt(line);

                            //ip
                            line = is.readLine();

                            AddMetaData(filename, port, line, clientName, GetUserGroup(clientName));
                        }
                    } else if (line.toLowerCase().contains("find ")) {
                        System.out.println("Got from client " + clientName + ", the command: " + line);

                        // filename
                        line = is.readLine();
                        String filename = line;
                        UserFile userfile = null;
                        userfile = FindFileAtUsers(clientName, filename);

                        if (userfile != null) {

                            os.println("foundFile");

                            os.println(userfile.name);
                            os.println(userfile.ip);
                            os.println(userfile.port);
                            os.println(userfile.file_owner);
                            os.println(userfile.group_owned);
                            os.println(userfile.isOnline);
                        } else {
                            os.println("notFoundFile");
                        }
                    } else if (line.toLowerCase().contains("create group")) {
                        String[] comm = line.split(" ");
                        CreateGroup(comm[2], clientName);
                    } else if (line.toLowerCase().contains("delete group")) {
                        String[] comm = line.split(" ");

                        DeleteGroup(comm[2], clientName);
                    } else if (line.toLowerCase().contains("add ") && line.toLowerCase().contains(" to group ")) {
                        String[] comm = line.split(" ");
                        ArrayList<String> usersList = new ArrayList<>();
                        String groupName = comm[comm.length - 1];

                        for (int i = 1; i < comm.length - 3; i++) {
                            usersList.add(comm[i]);
                        }

                        AddUsersToGroup(clientName, groupName, usersList);

                    } else if (line.toLowerCase().contains("delete ") && line.toLowerCase().contains(" from group ")) {
                        String[] comm = line.split(" ");
                        ArrayList<String> usersList = new ArrayList<>();
                        String groupName = comm[comm.length - 1];

                        for (int i = 1; i < comm.length - 3; i++) {
                            usersList.add(comm[i]);
                        }

                        DeleteUsersFromGroup(clientName, groupName, usersList);

                    } else if (line.toLowerCase().contains("log out")) {
                        System.out.println("Got from client " + clientName + ", the command: " + line);
                        signedIn = false;
                        synchronized (onlineUsers) {
                            // he closed the client, he is offline
                            onlineUsers.put(clientName, Boolean.FALSE);
                        }
                    } else if (line.toLowerCase().contains("delete") && !line.toLowerCase().contains(" from group ")) {
                        String[] comm = line.split(" ");

                        String user = comm[2];

                        System.out.println(user);

                        DeleteUser(user);

                    } else if (line.toLowerCase().equals("quit")) {
                        synchronized (onlineUsers) {
                            // he signed out, he is definetly offline, 
                            onlineUsers.put(clientName, Boolean.FALSE);
                        }
                        System.out.println("Got from client " + clientName + ", the command: " + line);
                        break;
                    }
                }
            } catch (IOException e) {

                line = this.getName();
                System.out.println("IO Error / Client " + line + " terminated abruptly.");
            } catch (NullPointerException e) {
                line = this.getName();
                System.out.println("Client " + line + " Closed.");
            } finally {
                try {
                    System.out.println("Connection Closing...");
                    if (is != null) {
                        is.close();
                        System.out.println("Socket Input Stream Closed.");
                    }

                    if (os != null) {
                        os.close();
                        System.out.println("Socket Output Stream Closed.");
                    }
                    if (s != null) {
                        s.close();
                        System.out.println("Socket Closed.");
                    }

                } catch (IOException ie) {
                    System.out.println("Socket Close Error.");
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ie);
                }
            }
        }
    }
}
