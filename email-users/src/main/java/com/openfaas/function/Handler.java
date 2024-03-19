package com.openfaas.function;

import com.openfaas.model.IHandler;
import com.openfaas.model.IResponse;
import com.openfaas.model.IRequest;
import com.openfaas.model.Response;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import jakarta.mail.*;  
import jakarta.mail.internet.*;  
import jakarta.activation.*;  

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

public class Handler extends com.openfaas.model.AbstractHandler 
{

    public static Connection getConnection() throws IOException, SQLException
    {  
        // Obtain access parameters and use them to create connection
      
        return DriverManager.getConnection("jdbc:sqlserver://sc20jh-db-server.database.windows.net:1433;database=sc20jh-sql-db;user=sc20jh@sc20jh-db-server;password=Nvq4u299**;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=60;");
    
    }
    
    public static void createTable(Connection database) throws SQLException
    {
        // Create a Statement object with which we can execute SQL commands
    
        Statement statement = database.createStatement();
    
        // Try to create a table, it'll error if it already exists
    
        try 
        {
            //System.out.println("creating table if it doesn't exist");
            statement.executeUpdate("CREATE TABLE users("
                + "id INT IDENTITY(1,1) PRIMARY KEY,"
                + "name VARCHAR(64) NOT NULL,"
                + "email VARCHAR(64) NOT NULL,"
                + "interest VARCHAR(32) NOT NULL)");
    
            //statement.executeUpdate("ALTER TABLE [dbo].[stories] ENABLE CHANGE_TRACKING");
        }
        catch (Exception error) 
        {
            // Catch and ignore SQLException, as this merely indicates
            // that the table already exists!
            //error.printStackTrace();
        }
    }
    

    public IResponse Handle(IRequest req) 
    {
        Response res = new Response();
	    res.setBody("Hello, world!");
        String combine = "";
        Connection database = null;
        try {
            database = getConnection();
            createTable(database);
        } catch (Exception e) {
            combine = res.getBody() + "\n" + e.getMessage();
            res.setBody(combine);
            return res;
        }

        String from = "jharros73@gmail.com";
        String host = "smtp.gmail.com";//or IP address  
  
        //Get the session object  
        //Properties properties = System.getProperties();  
        //String host = "send.smtp.mailtrap.io";
        //configure Mailtrap's SMTP server details
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        //properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        //properties.setProperty("mail.smtp.host", host);  
        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("jharros73@gmail.com", "vbey oobk yvro ymza");
            }
            };
            Session session = Session.getInstance(properties, authenticator);
            
        //Session session = Session.getDefaultInstance(properties);  
      
        List<Story> stories = new ArrayList<Story>();
        List<User> users = new ArrayList<User>();
        List<Integer> idsChecked = new ArrayList<Integer>();
        try{
                //addData(sensorSet[i], database);
                Statement statement = database.createStatement();
                ResultSet results = statement.executeQuery(
                   "SELECT * FROM stories WHERE checked = \'F\'");
                while (results.next()) 
                {
                    Story readStory = new Story();
                    readStory.author = results.getString("author");
                    readStory.category = results.getString("category");
                    readStory.details = results.getString("details");
                    readStory.title = results.getString("title");
                    stories.add(readStory);
                    idsChecked.add(results.getInt("id"));
                    //statement.executeQuery("UPDATE stories SET checked = \'T\' WHERE id = " + results.getInt("id"));
                }

                results = statement.executeQuery("SELECT * FROM users");
                while (results.next())
                {
                    User readUser = new User();
                    readUser.name = results.getString("name");
                    readUser.email = results.getString("email");
                    readUser.interest = results.getString("interest");
                    users.add(readUser);
                }

                statement.close();

                for(int i = 0; i < users.size(); i++)
                {
                    String targetEmail = users.get(i).email;
                    try{  
                        MimeMessage message = new MimeMessage(session);  
                        //System.out.println("Generated MimeMessage");
                        message.setFrom(new InternetAddress(from));  
                        //System.out.println("Set the message email address");
                        message.addRecipient(Message.RecipientType.TO,InternetAddress.parse(targetEmail)[0]);  
                        //System.out.println("Set the message destination address");
                        message.setSubject("Interesting topics");  
                        String myMessage = "Hello " + users.get(i).name + ",\nWe thought you might like the following story due to your interest in " + users.get(i).interest + ":\n";  
                        Boolean shouldSend = false;
                        for(int j = 0; j < stories.size(); j++)
                        {
                            //System.out.println("user: " +  users.get(i).name);
                            String interest = users.get(i).interest;
                            String category = stories.get(j).category;

                            // System.out.print("interest:" +  interest + ": text after\n");
                            // System.out.print("category:" +  category + ": text after\n");
                          
                            if(interest.equals(category) || interest.equals("any"))
                            {
                                //System.out.println("interest matched");
                                shouldSend = true;
                                myMessage = myMessage +"Title: " + stories.get(j).title + "\nBy: " + stories.get(j).author +"\nDetails: " + stories.get(j).details + "\n\n";
                            }
                        }

                        // Send message  
                        if(shouldSend == true)
                        {
                            //System.out.println("Trying to send email");
                            message.setText(myMessage);
                            Transport.send(message);  
                            //System.out.println("message sent successfully....");  
                        }
                 
                        }
                        catch (Exception e) 
                        {
                            combine = res.getBody() + "\n" + e.getMessage();
                            res.setBody(combine);
                            return res;
                        }  
                }
                Statement secondStatement = database.createStatement();
                for (Integer id : idsChecked) {
                    secondStatement.executeQuery("UPDATE stories SET checked = \'T\' WHERE id = " + id);
                }
                secondStatement.close();
            }
            catch(SQLException e){

            }
            catch( Exception error){
                combine = res.getBody() + "\n" + error.getMessage();
                res.setBody(combine);
                return res;
            }
	    return res;
    }
}
