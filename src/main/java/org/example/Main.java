package org.example;

import java.sql.*;

import com.google.cloud.bigquery.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main {
    static final String JDBC_DRIVER = "com.mongodb.jdbc.MongoDriver";
    static final String URL = "jdbc:mongodb://federateddatabaseinstance1-2wqno.a.query.mongodb.net/?ssl=true&authSource=admin";

    public static void main(String[] args) throws SQLException {

        /** BigQuery settings **/
        String datasetName = "mflix";
        String tableName = "Users-02";

        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
        TableId tableId = TableId.of(datasetName, tableName);



        /** MongoDB settings **/
        java.util.Properties p = setProperties();
        System.out.println("Connecting to database test...");
        Connection conn = DriverManager.getConnection(URL, p);

        /** Execute SQL query on Mongodb **/
        System.out.println("Creating statement...");
        Statement stmt = conn.createStatement();
        ResultSet resultSet = stmt.executeQuery("SELECT * from movies");

        ResultSetMetaData rsmd = resultSet.getMetaData();
        System.out.println("++++++ Showing contents for mydb.Users ++++++++");
        System.out.println("rs >>>>>>>>>>>>>"+resultSet);
        int columnsNumber = rsmd.getColumnCount();
        System.out.println(">>>>>>>>>>>>>"+columnsNumber);


        /** Bigquery Create Schema **/

        final List<Field> fields = createSchema(rsmd, columnsNumber);
        Schema schema = Schema.of(fields);
        TableDefinition tableDefinition = StandardTableDefinition.of(schema);
        TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();

        try{
            bigquery.create(tableInfo);
        }catch (BigQueryException e) {
            System.out.println("Table was not created. \n" + e.toString());
        }


        System.out.println("Table created successfully");

        while(resultSet.next()){
            //Retrieve by column name
            Map<String, Object> rowContent = createRowContent(rsmd, columnsNumber, resultSet);
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

//                String name = resultSet.getString("name");
//                System.out.println("metadata>>>>>>> "+name);
        }
    }
    public static List<Field> createSchema(ResultSetMetaData rsmd, int columnsNumber ) throws SQLException {
        final List<Field> fields = new ArrayList<>();
        for(int i=1; i<= columnsNumber; i++) {
            String dataType = rsmd.getColumnTypeName(i);
            if (dataType == "string") {
                fields.add(Field.of(rsmd.getColumnName(i), StandardSQLTypeName.STRING));
            } else if (dataType == "int") {
                fields.add(Field.of(rsmd.getColumnName(i), StandardSQLTypeName.INT64));
            } else if (dataType == "double" || dataType == "float") {
                fields.add(Field.of(rsmd.getColumnName(i), StandardSQLTypeName.FLOAT64));
            } else if (dataType == "object") {
                fields.add(Field.of(rsmd.getColumnName(i), StandardSQLTypeName.STRING));
            } else {
                fields.add(Field.of(rsmd.getColumnName(i), StandardSQLTypeName.STRING));
            }
        }
        return fields;
    }

    public static Map<String, Object> createRowContent(ResultSetMetaData rsmd, int columnsNumber, ResultSet resultSet) throws SQLException {
        Map<String, Object> rowContent = new HashMap<>();

        for (int i = 1; i <= columnsNumber; i++) {
            String dataType = rsmd.getColumnTypeName(i);

            if(dataType == "string") {
                String columnValue = resultSet.getString(i);
                rowContent.put(rsmd.getColumnName(i), columnValue);
            }
            else if(dataType == "int") {
                Integer columnValue = resultSet.getInt(i);
                rowContent.put(rsmd.getColumnName(i), columnValue);
            }
            else if(dataType == "objectId") {
                String columnValue = resultSet.getObject(i).toString();
                rowContent.put(rsmd.getColumnName(i), columnValue);
            }
            else if(dataType == "object") {
                String columnValue = resultSet.getString(i);
                rowContent.put(rsmd.getColumnName(i), columnValue);
            }
            else if(dataType == "double") {
                Double columnValue = resultSet.getDouble(i);
                rowContent.put(rsmd.getColumnName(i), columnValue);
            }
            else {
                String columnValue = resultSet.getString(i);
                rowContent.put(rsmd.getColumnName(i), columnValue);
            }
            System.out.println(">>>>>>>>>>>>>>>>>>"+dataType);
        }
        return rowContent;
    }

    public static java.util.Properties setProperties(){
        java.util.Properties property = new java.util.Properties();
        // These properties will be added to the URI.
        // Uncomment if you wish to specify user and password.
        property.setProperty("user", "venkatesh");
        property.setProperty("password", "ashwin123");
        property.setProperty("database", "mflix");
        return property;
    }

}