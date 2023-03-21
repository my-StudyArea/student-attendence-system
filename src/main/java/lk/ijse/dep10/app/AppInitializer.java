package lk.ijse.dep10.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lk.ijse.dep10.app.db.DBConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AppInitializer extends Application {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                System.out.println("Database connection is about to close");
                if (DBConnection.getInstance().connection != null && !DBConnection.getInstance().connection.isClosed()) {
                    DBConnection.getInstance().connection.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        generateTablesIfNotExist();

        primaryStage.setScene(new Scene(new FXMLLoader(getClass().getResource("/view/MainView.fxml")).load()));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    private void generateTablesIfNotExist() {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SHOW TABLES ");

            HashSet<String> tableNameSet = new HashSet<>();
            while (rst.next()) {
                tableNameSet.add(rst.getString(1));
            }

            boolean tableExists = tableNameSet.containsAll(Set.of("Attendance", "Picture", "Student", "User"));

            if (!tableExists) {
                System.out.println("schema is about to auto generate");
                getClass().getResource("/schema.sql");

                stm.execute(readDBScript());
            }

//            InputStream is = getClass().getResourceAsStream("/schema.sql");
//            BufferedReader br = new BufferedReader(new InputStreamReader(is));
//            String line;
//            StringBuilder dbScript = new StringBuilder();
//
//            while ((line = br.readLine()) != null) {
//                dbScript.append(line).append("\n");
//            }
//            br.close();
//            stm.execute(dbScript.toString());

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to connect").showAndWait();
            throw new RuntimeException(e);
        }
    }

    private String readDBScript() {
        InputStream is = getClass().getResourceAsStream("/schema.sql");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))){
            String line;
            StringBuilder dbScripBuilder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                dbScripBuilder.append(line);
            }
            return dbScripBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
