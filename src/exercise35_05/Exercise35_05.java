/**
 * Class: CSCI5520U Rapid Java Development
 * Instructor: Y. Daniel Liang
 * Description: Connects to a database and displays a
 * table from the database onto a GUI.
 * Due: 01/23/2017
 *
 * @author Shaun C. Dobbs
 * @version 1.0
 *
 * I pledge by honor that I have completed the programming assignment
 * independently. I have not copied the code from a student or any source. I
 * have not given my code to any student. -+
 *
 * Sign here: Shaun C. Dobbs
 */
package exercise35_05;

import java.sql.*;
import static javafx.application.Application.launch;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import java.sql.Connection;
import java.sql.ResultSet;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Exercise35_05 extends Application {

    // Connection to the database
    private TableView tableView = new TableView();
    private ObservableList<ObservableList> data = tableView.getItems();
    BorderPane borderPaneExecutionResult = new BorderPane();

    private Connection connection;

    // Statement to execute SQL commands
    private Statement statement;

    // Text area to enter SQL commands
    private TextArea tasqlCommand = new TextArea();

    // Text area to display results from SQL commands
    private TextArea taSQLResult = new TextArea();

    // DBC info for a database connection
    private TextField tfUsername = new TextField();
    private PasswordField pfPassword = new PasswordField();
    private ComboBox<String> cboURL = new ComboBox<>();
    private ComboBox<String> cboDriver = new ComboBox<>();

    private Button btExecuteSQL = new Button("Execute SQL Command");
    private Button btClearSQLCommand = new Button("Clear");
    private Button btConnectDB = new Button("Connect to Database");
    private Button btClearSQLResult = new Button("Clear Result");
    private Label lblConnectionStatus = new Label("Not currently connected to a database");

    @Override // Override the start method in the Application clas
    public void start(Stage primaryStage) {
        cboURL.getItems().addAll(FXCollections.observableArrayList(
                "jdbc:mysql://localhost:3306/javabook"
        ));
        cboURL.getSelectionModel().selectFirst();

        cboDriver.getItems().addAll(FXCollections.observableArrayList(
                "com.mysql.jdbc.Driver", "sun.jdbc.odbc.dbcOdbcDriver",
                "oracle.jdbc.driver.OracleDriver"));
        cboDriver.getSelectionModel().selectFirst();

        // Create UI for connecting to the database
        GridPane gridPane = new GridPane();
        gridPane.add(cboURL, 1, 0);
        gridPane.add(cboDriver, 1, 1);
        gridPane.add(tfUsername, 1, 2);
        gridPane.add(pfPassword, 1, 3);
        gridPane.add(new Label("JDBC Driver"), 0, 0);
        gridPane.add(new Label("Database URL"), 0, 1);
        gridPane.add(new Label("Username"), 0, 2);
        gridPane.add(new Label("Password"), 0, 3);

        HBox hBoxConnection = new HBox();
        hBoxConnection.getChildren().addAll(
                lblConnectionStatus, btConnectDB);
        hBoxConnection.setAlignment(Pos.CENTER_RIGHT);

        VBox vBoxConnection = new VBox(5);
        vBoxConnection.getChildren().addAll(
                new Label("Enter Database Information"),
                gridPane, hBoxConnection);

        gridPane.setStyle("-fx-border-color: black;");

        HBox hBoxSQLCommand = new HBox(5);
        hBoxSQLCommand.getChildren().addAll(
                btClearSQLCommand, btExecuteSQL);
        hBoxSQLCommand.setAlignment(Pos.CENTER_RIGHT);

        BorderPane borderPaneSqlCommand = new BorderPane();
        borderPaneSqlCommand.setTop(
                new Label("Enter an SQL Command"));
        borderPaneSqlCommand.setCenter(
                new ScrollPane(tasqlCommand));
        borderPaneSqlCommand.setBottom(
                hBoxSQLCommand);

        HBox hBoxConnectionCommand = new HBox(10);
        hBoxConnectionCommand.getChildren().addAll(
                vBoxConnection, borderPaneSqlCommand);

        borderPaneExecutionResult.setTop(
                new Label("SQL Execution Result"));
        borderPaneExecutionResult.setCenter(tableView);
        borderPaneExecutionResult.setTop(taSQLResult);
        borderPaneExecutionResult.setBottom(btClearSQLResult);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(hBoxConnectionCommand);
        borderPane.setCenter(borderPaneExecutionResult);

        // Create a scene and place it in the stage
        Scene scene = new Scene(borderPane, 825, 600);
        primaryStage.setTitle("Exercise35_05"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        btConnectDB.setOnAction(e -> connectToDB());
        btExecuteSQL.setOnAction(e -> executeSQL());
        btClearSQLCommand.setOnAction(e -> tasqlCommand.setText(null));
        btClearSQLResult.setOnAction(e -> {
            taSQLResult.setText(null);
            tableView.getColumns().clear();
        });
    }

    /**
     * Connect to DB
     */
    private void connectToDB() {
        // Get database information from the user input
        String driver = cboDriver
                .getSelectionModel().getSelectedItem();
        String url = cboURL.getSelectionModel().getSelectedItem();
        String username = tfUsername.getText().trim();
        String password = pfPassword.getText().trim();

        // Connection to the database
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(
                    url, username, password);
            lblConnectionStatus.setText("Connected to " + url);
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Execute SQL commands
     */
    private void executeSQL() {
        if (connection == null) {
            taSQLResult.setText("Please connect to a database first");
            return;
        } else {
            String sqlCommands = tasqlCommand.getText().trim();
            String[] commands = sqlCommands.replace('\n', ' ').split(";");

            for (String aCommand : commands) {
                if (aCommand.trim().toUpperCase().startsWith("SELECT")) {
                    processSQLSelect(aCommand);
                } else {
                    processSQLNonSelect(aCommand);
                }
            }
        }
    }

    /**
     * Execute SQL SELECT commands
     */
    private void processSQLSelect(String sqlCommand) {
        try {
            // Get a new statement for the current connection
            statement = connection.createStatement();

            // Execute a SELECT SQL command
            ResultSet resultSet = statement.executeQuery(sqlCommand);

            //Add columns
            for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(resultSet.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });
                tableView.getColumns().addAll(col);
            }
            while (resultSet.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();

                //Iterate Row               
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {

                    //Iterate Column
                    row.add(resultSet.getString(i));
                }
                data.add(row);
            }
            //Close the connection
            connection.close();

            //add to TableView
            tableView.setItems(data);
            tableView.rowFactoryProperty();
        } catch (SQLException ex) {
            taSQLResult.setText(ex.toString());
        }
    }

    /**
     * Execute SQL DDL, and modification commands
     */
    private void processSQLNonSelect(String sqlCommand) {
        try {
// Get a new statement for the current connection
            statement = connection.createStatement();
            // Execute a non-SELECT SQL command
            statement.executeUpdate(sqlCommand);
            taSQLResult.setText("SQL command executed");
        } catch (SQLException ex) {
            taSQLResult.setText(ex.toString());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
