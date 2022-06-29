package org.example;

import java.sql.*;

import com.google.cloud.bigquery.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main {
    static final String URL = "jdbc:mongodb://federateddatabaseinstance0-2wqno.a.query.mongodb.net/?ssl=true&authSource=admin";
    public static void main(String[] args) throws SQLException {

        /** BigQuery parameters **/
        String datasetName = args[0];
        String tableName = args[1];

        /** MongoDB parameters **/
        String mdbUser = args[2];
        String mdbPass = args[3];
        String datalakeDBName = args[4];
        String collection = args[5];
        String  deapth = args[6];

        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
        TableId tableId = TableId.of(datasetName, tableName);

        /** MongoDB settings **/
        java.util.Properties p = setProperties(mdbUser, mdbPass, datalakeDBName);
        System.out.println("Connecting to database test...");
        Connection conn = DriverManager.getConnection(URL, p);

        /** Execute SQL query on Mongodb **/
        System.out.println("Creating statement...");
        Statement stmt = conn.createStatement();
        String query = "SELECT * from FLATTEN("+datalakeDBName+"."+collection+" WITH DEPTH =>"+ deapth +")";
        System.out.println(">>>>>>>>>>>>>>"+ query);
        ResultSet resultSet = stmt.executeQuery(query);

        conn.close();

        ResultSetMetaData rsmd = resultSet.getMetaData();
        System.out.println("++++++ Showing contents for mydb.Users ++++++++");
        int columnsNumber = rsmd.getColumnCount();

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
//
//
        System.out.println("Table created successfully");

        while(resultSet.next()){
            //Retrieve by column name
            Map<String, Object> rowContent = createRowContent(rsmd, columnsNumber, resultSet);
            InsertAllResponse response =
                    bigquery.insertAll(
                            InsertAllRequest.newBuilder(tableId)
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
    public static java.util.Properties setProperties(String mdbUser, String mdbPass, String db_name){
        java.util.Properties property = new java.util.Properties();
        // These properties will be added to the URI.
        // Uncomment if you wish to specify user and password.
        property.setProperty("user", mdbUser);
        property.setProperty("password", mdbPass);
        property.setProperty("database", db_name);
        return property;
    }

}