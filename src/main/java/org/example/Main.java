package org.example;

import java.sql.*;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.google.cloud.bigquery.Field;
import java.util.HashMap;
import java.util.Map;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.BigQueryError;
import java.util.List;




public class Main {
    static final String JDBC_DRIVER = "com.mongodb.jdbc.MongoDriver";
    static final String URL = "jdbc:mongodb://federateddatabaseinstance0-2wqno.a.query.mongodb.net/?ssl=true&authSource=admin";

    public static void main(String[] args) throws SQLException {

        /** BigQuery settings **/
        String projectId = "gsidemo-246315";
        String datasetName = "mflix";
        String tableName = "Users";

        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
        TableId tableId = TableId.of(datasetName, tableName);

        try {
            // Initialize client that will be used to send requests. This client only needs to be created
            // once, and can be reused for multiple requests.
            Schema schema =
                    Schema.of(
                            Field.of("name", StandardSQLTypeName.STRING)
                            );


            TableDefinition tableDefinition = StandardTableDefinition.of(schema);
            TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();

            bigquery.create(tableInfo);
            System.out.println("Table created successfully");
        } catch (BigQueryException e) {
            System.out.println("Table was not created. \n" + e.toString());
        }



        /** MongoDB settings **/
        java.util.Properties p = new java.util.Properties();
        // These properties will be added to the URI.
        // Uncomment if you wish to specify user and password.
        p.setProperty("user", "venkatesh");
        p.setProperty("password", "ashwin123");
        p.setProperty("database", "Database0");
        System.out.println("Connecting to database test...");
        Connection conn = DriverManager.getConnection(URL, p);

        DatabaseMetaData dbmd = conn.getMetaData();
        System.out.println("Database >>>>>>"+ dbmd);

        System.out.println("Creating statement...");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from Users");
        System.out.println("++++++ Showing contents for mydb.Users ++++++++");
        System.out.println("rs >>>>>>>>>>>>>"+rs);

        while(rs.next()){
            //Retrieve by column name
            String name = rs.getString("name");
            Map<String, Object> rowContent = new HashMap<>();
            rowContent.put("name", name);
            tableInsertRows(bigquery, tableId, rowContent);
            System.out.println("metadata>>>>>>> "+name);


        }

    }

    public static void tableInsertRows(
          BigQuery bigquery, TableId tableId,Map<String, Object> rowContent
    ){
        InsertAllResponse response =
                bigquery.insertAll(
                        InsertAllRequest.newBuilder(tableId)
                                // More rows can be added in the same RPC by invoking .addRow() on the builder.
                                // You can also supply optional unique row keys to support de-duplication
                                // scenarios.
                                .addRow(rowContent)
                                .build());
        if (response.hasErrors()) {
            // If any of the insertions failed, this lets you inspect the errors
            for (Map.Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
                System.out.println("Response error: \n" + entry.getValue());
            }
        }
    }
}