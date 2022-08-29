package org.example;

import com.google.cloud.bigquery.*;
import java.sql.*;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class Main {
  static final String URL =
      "jdbc:mongodb://federateddatabaseinstance0-2wqno.a.query.mongodb.net/?ssl=true&authSource=admin";

  public static void main(String[] args) throws SQLException {

//    /** BigQuery parameters. * */
//    String datasetName = args[0];
//    String tableName = args[1];
//
//    /** MongoDB parameters. * */
//    String mdbUser = args[2];
//    String mdbPass = args[3];
//    String datalakeDBName = args[4];
//    String collection = args[5];
//    String deapth = args[6];

    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
    TableId tableId = TableId.of("mflix", "atlassql01");

    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>"+ tableId);
    /** MongoDB settings. * */
    java.util.Properties p = Utils.setProperties("venkatesh", "ashwin123", "MyFirstDatabase");
    System.out.println("Connecting to database test...");
    Connection conn = DriverManager.getConnection(URL, p);

    /** Execute SQL query on Mongodb. * */
    System.out.println("Creating statement...");
    Statement stmt = conn.createStatement();
    String query =
        "SELECT * from FLATTEN("
            + "MyFirstDatabase"
            + "."
            + "customers"
            + " WITH DEPTH =>"
            + "1"
            + ")";
    ResultSet resultSet = stmt.executeQuery(query);
    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+resultSet);

    ResultSetMetaData rsmd = resultSet.getMetaData();
    int columnsNumber = rsmd.getColumnCount();

    /** Bigquery Create Schema. * */
    final List<Field> fields = Utils.createSchema(rsmd, columnsNumber);
    Schema schema = Schema.of(fields);
    TableDefinition tableDefinition = StandardTableDefinition.of(schema);
    TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();

    try {
      bigquery.create(tableInfo);
      System.out.println("Table created successfully");
    } catch (BigQueryException e) {
      System.out.println("Table was not created. \n" + e.toString());
    }
    //
    System.out.println("?>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> here");

    try{
      while (resultSet.next()) {
        // Retrieve by column name
        Map<String, Object> rowContent = Utils.createRowContent(rsmd, columnsNumber, resultSet);
        InsertAllResponse response =
                bigquery.insertAll(InsertAllRequest.newBuilder(tableId).addRow(rowContent).build());
        if (response.hasErrors()) {
          // If any of the insertions failed, this lets you inspect the errors
          for (Map.Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
            System.out.println("Response error: \n" + entry.getValue());
          }
        }
      }
    }
    catch (Exception e){
      System.out.println("Exception: " + e.toString());
    }

    conn.close();
  }
}
