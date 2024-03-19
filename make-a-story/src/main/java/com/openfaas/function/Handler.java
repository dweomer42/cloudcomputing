package com.openfaas.function;

import com.openfaas.model.IHandler;
import com.openfaas.model.IResponse;
import com.openfaas.model.IRequest;
import com.openfaas.model.Response;

import java.sql.*;
import java.time.LocalDateTime;
import java.io.IOException;
import com.google.gson.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import java.util.*;


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
      // Load properties
  
      // FileInputStream in = new FileInputStream(propsFile);
      // Properties props = new Properties();
      // props.load(in);
  
      // Define JDBC driver


        System.setProperty("jdbc.drivers", "com.mysql.jdbc.Driver");
        // Setting standard system property jdbc.drivers
        // is an alternative to loading the driver manually
        // by calling Class.forName()
  
      // Obtain access parameters and use them to create connection
  
      return DriverManager.getConnection("jdbc:sqlserver://sc20jh-db-server.database.windows.net:1433;database=sc20jh-sql-db;user=sc20jh@sc20jh-db-server;password=Nvq4u299**;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");

    }

    public static void createTable(Connection database) throws SQLException
  {
    // Create a Statement object with which we can execute SQL commands

    Statement statement = database.createStatement();

    // Try to create a table, it'll error if it already exists

    try {
      statement.executeUpdate("CREATE TABLE stories("
      + "id INT PRIMARY KEY AUTO_INCREMENT,"
      + "title VARCHAR(64) NOT NULL,"
      + "author VARCHAR(32) NOT NULL,"
      + "category VARCHAR(32) NOT NULL,"
      + "details VARCHAR(128) NOT NULL,"
      + "checked CHAR(1) NOT NULL,"
      + "timestamp DATETIME NOT NULL)");

      
  }
    catch (SQLException error) {
      // Catch and ignore SQLException, as this merely indicates
      // that the table already exists!
      //error.printStackTrace();
    }

    statement.close();
  }

  public static void addData(Story newStory, Connection database)
   throws IOException, SQLException
  {
    // Prepare statement used to insert data
    //int id = newStory.storyId;
    String title = newStory.title;
    String author = newStory.author;
    String category = newStory.category;
    String details = newStory.details;
    LocalDateTime timestamp = LocalDateTime.now();

    PreparedStatement statement =
     database.prepareStatement("INSERT INTO stories(title,author,category,details,checked,timestamp) VALUES(?,?,?,?,?,?)");
     //statement.setInt(1, id);
     statement.setString(1, title);
     statement.setString(2, author);
     statement.setString(3, category);
     statement.setString(4, details);
     statement.setString(5, "False");
     statement.setObject(6, timestamp);
    // Loop over input data, inserting it into table...
 
      statement.executeUpdate();

    statement.close();
  }

    public IResponse Handle(IRequest req){
        Response res = new Response();
	      res.setBody("Hello, world!");
        String body = req.getBody();
        String combine = res.getBody() + req.getBody();
        res.setBody(combine);
        String author = "";
        String title = "";
        String category = "";
        String details = "";
        Story newStory = new Story();
        Map<String, String> query = req.getQuery();
        author = query.get("author");
        title = query.get("title");
        category = query.get("category");
        details = query.get("details");
        combine = res.getBody() + author + title + category + details;
        res.setBody(combine);
        // try
        // {
        //     System.err.println("got in");
        //     combine = res.getBody() + "\ngot in";
        //     res.setBody(combine);
        //     Gson gson = new Gson();
        //     newStory = gson.fromJson(req.getBody(), Story.class);
        //     // JsonObject jobj = gson.fromJson(req.getBody(), JsonObject.class);
        //     System.err.println("read jobj");
        //     combine = res.getBody() + "\nread jobj";
        //     res.setBody(combine);
        //     // author = jobj.get("author").getAsString();
        //     // title = jobj.get("title").getAsString();
        //     // category = jobj.get("category").getAsString();
        //     // details = jobj.get("details").getAsString();
        // }
        // catch (Exception e)
        // {
        //     //e.printStackTrace();
        //     String errorStr = ExceptionUtils.getStackTrace(e);
        //     //System.err.println("Failed to read json");
        //     combine = res.getBody() + "\nFailed to read json\n" + errorStr;
        //     res.setBody(combine);
        // }


        // Connection database = null;
        // try {
        //     database = getConnection();
        //     createTable(database);
        // } catch (Exception e) {
        //     e.printStackTrace();

        // }

        newStory.title = title;
        newStory.author = author;
        newStory.category = category;
        newStory.details = details;
        // //res.setBody(newStory.title);
        // try{
        //     addData(newStory, database);
        // }
        // catch (Exception e)
        // {
        //     e.printStackTrace();
        // }
	    return res;
    }
}
