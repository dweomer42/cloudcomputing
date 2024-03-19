package com.openfaas.function;

import com.openfaas.model.IHandler;
import com.openfaas.model.IResponse;
import com.openfaas.model.IRequest;
import com.openfaas.model.Response;
import java.io.IOException;
import java.sql.*;
import java.util.*;

class User
{
    public String name;
    public String email;
    public String interest;
}

class Story
{
    public String title;
    public String author;
    public String category;
    public String details;
}

public class Handler extends com.openfaas.model.AbstractHandler {

        public static Connection getConnection() throws IOException, SQLException
        {  
          // Obtain access parameters and use them to create connection
      
          return DriverManager.getConnection("jdbc:sqlserver://sc20jh-db-server.database.windows.net:1433;database=sc20jh-sql-db;user=sc20jh@sc20jh-db-server;password=Nvq4u299**;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
    
        }
    
        public static void createTable(Connection database) throws SQLException
        {
        // Create a Statement object with which we can execute SQL commands
    
        Statement statement = database.createStatement();
    
        // Try to create a table, it'll error if it already exists
    
            try {
                //System.out.println("creating table if it doesn't exist");
                statement.executeUpdate("CREATE TABLE storySummary("
                + "id INT IDENTITY(1,1) PRIMARY KEY,"
                + "category VARCHAR(32) NOT NULL,"
                + "subscribers INTEGER NOT NULL,"
                + "count INTEGER NOT NULL)");
    
                //statement.executeUpdate("ALTER TABLE [dbo].[stories] ENABLE CHANGE_TRACKING");
            }
            catch (Exception error) 
            {
                // Catch and ignore SQLException, as this merely indicates
                // that the table already exists!
                //error.printStackTrace();
            }
        }
    
        public static int CountCategory(String CategoryName, Connection database)
        {
            int count = 0;
            try
            {
                PreparedStatement statement = database.prepareStatement("SELECT * FROM stories WHERE category = ?");
                statement.setString(1, CategoryName);
                ResultSet results = statement.executeQuery();
                while (results.next()) {
                    count++;
                }
                //System.out.println("Successfully counted categories");
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
    
            return count;
        }
    
        public static int CountSubscribers(String CategoryName, Connection database)
        {
            int count = 0;
            try
            {
                PreparedStatement statement = database.prepareStatement("SELECT * FROM users WHERE interest = ? OR interest = 'any'");
                statement.setString(1, CategoryName);
                ResultSet results = statement.executeQuery();
                while (results.next()) {
                    count++;
                }
                //System.out.println("Succesfully counted subscribers");
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
    
            return count;
        }

    public IResponse Handle(IRequest req) {
        Response res = new Response();
	    res.setBody("Hello, world!");
        Connection database = null;
        String combine = "";
        try {
            database = getConnection();
            createTable(database);
        } catch (Exception e) {
            combine = res.getBody() + "\n" + e.getMessage();
            res.setBody(combine);
        }

            try{
                Statement statement = database.createStatement();
                ResultSet results = statement.executeQuery("SELECT DISTINCT category FROM stories");
                while (results.next()) {
                    String category = results.getString("category");
                    System.out.println(category);
                    int count = CountCategory(category, database);
                    int subscribers = CountSubscribers(category, database);
                    // System.out.println(count);
                    // System.out.println(subscribers);
                    // System.out.println("Trying to update table");

                    // Query this
                    PreparedStatement myStatement = database.prepareStatement("SELECT * FROM storySummary WHERE category = ?");
                    myStatement.setString(1, category);
                    ResultSet foundStory = myStatement.executeQuery();
                    //System.out.println("made it here");
                    if(foundStory.next())
                    {
                        myStatement = database.prepareStatement("UPDATE storySummary SET count = ?, subscribers = ? WHERE category = ?");
                        myStatement.setInt(1, count);
                        myStatement.setInt(2, subscribers);
                        myStatement.setString(3, category);
                        myStatement.executeUpdate();
                    }
                    else
                    {
                        myStatement = database.prepareStatement("INSERT INTO storySummary (category, count, subscribers) VALUES(?,?,?)");
                        myStatement.setInt(2, count);
                        myStatement.setInt(3, subscribers);
                        myStatement.setString(1, category);
                        myStatement.executeUpdate();
                    }
                    myStatement.close();
                }
                statement.close();
            }
            catch(Exception e)
            {
                combine = res.getBody() + "\n" + e.getMessage();
                res.setBody(combine);
            }





        
	    return res;
    }
}
