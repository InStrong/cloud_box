import java.sql.*;

public class SQLHandler {
    private static final String url = "jdbc:mysql://localhost:3306/cloud_box?ftimeCode=false&serverTimezone=UTC&useSSL=false";
    private static final String user = "root";
    private static final String password = "fhwheyrfhnjy94";

    private Connection con;
    private Statement stmt;
    private ResultSet rs;

    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, user, password);
            stmt = con.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            con.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isAuthPassed(String login,String password){
        int temp=-1;
        String query = "SELECT EXISTS(SELECT * FROM cloud_box.Users WHERE login='"+login+"' AND password='"+password+"')";

        try {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()){
                temp=rs.getInt(1);
            }
            if (temp!=0) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return false;
    }

    public void registerUser(String login,String password){
        String query = "REPLACE INTO Users (login,password) VALUES ('"+login+"','"+password+"')";
        try {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
