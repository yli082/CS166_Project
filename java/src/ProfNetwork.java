/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */
import java.util.Calendar;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class ProfNetwork {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of ProfNetwork
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public ProfNetwork (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end ProfNetwork

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
          List<String> record = new ArrayList<String>();
         for (int i=1; i<=numCol; ++i)
            record.add(rs.getString (i));
         result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            ProfNetwork.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      ProfNetwork esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the ProfNetwork object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new ProfNetwork (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Goto Friend List");
                System.out.println("2. Update Profile");
                System.out.println("3. Write a new message");
                System.out.println("4. Send Friend Request");
                System.out.println("5. Search for a person");
                System.out.println("6. View/delete sent messages");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: FriendList(esql,authorisedUser); break;
                   case 2: UpdateProfile(esql,authorisedUser); break;
                   case 3: NewMessage(esql,authorisedUser); break;
                   case 4: SendRequest(esql); break;
                   case 5: Searchperson(esql,authorisedUser); break;
                   case 6: Viewsent(esql,authorisedUser); break;
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         while(login.length()==0)
         {
             System.out.print("User name required: ");
             login = in.readLine();
         }
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         while(password.length()==0)
         {
             System.out.print("Password required: ");
             password = in.readLine();
         }
         System.out.print("\tEnter user email: ");
         String email = in.readLine();
         while(email.length()==0)
         {
             System.out.print("Email required: ");
             email = in.readLine();
         }
         System.out.print("\tEnter name: ");
         String name = in.readLine();


	 //Creating empty contact\block lists for a user
	 String query = String.format("INSERT INTO USR (userId, password, email, name) VALUES ('%s','%s','%s','%s')", login, password, email,name);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USR WHERE userId = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end
public static boolean isDateValid(String date)
{
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy");
    sdf.setLenient(false);
    return sdf.parse(date,new ParsePosition(0)) != null;
}
   public static int FriendList(ProfNetwork esql,String user)
   {
       return -1;
   }

   public static int UpdateProfile(ProfNetwork esql, String user)
   {
       try{
           String query = String.format("SELECT * FROM USR WHERE userid ='%s'",user);

       List<List<String>> yoyo = esql.executeQueryAndReturnResult(query);


        System.out.println("Current profile");
        System.out.println("---------");
        System.out.println("Username: " + yoyo.get(0).get(0));
        System.out.println("Email: " + yoyo.get(0).get(2));
        System.out.println("Name: " + yoyo.get(0).get(3));
        System.out.println("Birthday: " + yoyo.get(0).get(4));



      System.out.println("---------");


  System.out.println("Menu");
                System.out.println("---------");
                System.out.println("1. Change password");
                System.out.println("2. Change Email");
                System.out.println("3. Change name");
                System.out.println("4. Change birthday");
                System.out.println("5. View/Update work experience");
                System.out.println("6. View/Update Educational details");
                System.out.println("9. Go back");


System.out.println(".........................");
//System.out.println("Press anything else to return to the main menu\n");
               // System.out.print("Please make your choice: ");
                //String yes = in.readLine();
            //int foo = Integer.parseInt(yes);
            switch(readChoice()){

       case 1:
           System.out.print("Please enter your current password: ");
           String  checker = in.readLine();
           if((checker).equals(yoyo.get(0).get(1)))
           {
               System.out.print("Password verified, please enter your new password: ");
               String newpw = in.readLine();
               String qq = String.format("UPDATE USR SET password = '%s' WHERE userid = '%s',password = '%s'",newpw,user,checker);
               esql.executeUpdate(qq);
               //update pw
           }
           else
           {
                System.out.println("Incorrect password");
                //kill them
           }

       break;
       case 2:
            System.out.print("Enter a new email: ");
            String ems = in.readLine();
            String oreos = String.format("UPDATE USR SET email = '%s', WHERE userid = '%s'",ems, user);
            esql.executeUpdate(oreos);
       break;
       case 3:
            System.out.print("Enter a new name: ");
            String emss = in.readLine();
            String oreoss = String.format("UPDATE USR SET name = '%s', WHERE userid = '%s'",emss, user);
            esql.executeUpdate(oreoss);
       break;
       case 4:
            System.out.print("Enter a new birthday (dd/mm/yyyy): ");
            String emsc = in.readLine();
            while(!isDateValid(emsc))
                 {
                     System.out.print("Please enter a valid date (dd/mm/yyyy): ");
                     emsc = in.readLine();
                 }
            String oreosc = String.format("UPDATE USR SET dateofbirth = '%s', WHERE userid = '%s'",emsc, user);
            esql.executeUpdate(oreosc);
       break;

       case 5:

System.out.println("Work Experience");
                System.out.println("---------");
                System.out.println("1. View work experience");
                System.out.println("2. Add work experience");
                System.out.println("3. Update previous work experience");
System.out.println(".........................");
//System.out.println("Press anything else to return to the main menu\n");
            String query2 = String.format("SELECT * FROM WORK_EXPR WHERE userid = '%s'",user);
            int testtest = esql.executeQuery(query2);
            List<List<String>> yoyo2 = esql.executeQueryAndReturnResult(query2);
            switch(readChoice()){
                case 1:
                    for(int j = 0; j < yoyo2.size();++j)
                    {
System.out.println("---------");
                System.out.println("Company: " + yoyo2.get(j).get(1));

                System.out.println("Role: " + yoyo2.get(j).get(2));

                System.out.println("Location: " + yoyo2.get(j).get(3));

                System.out.println("Startdate: " + yoyo2.get(j).get(4));

                System.out.println("Enddate: " + yoyo2.get(j).get(5));
System.out.println("---------");

                    }
                break;
            case 2:
                 System.out.print("Please enter your company name: ");
                 String company = in.readLine();
                 company.trim();
                  System.out.print("Please enter your role: ");
                 String role = in.readLine();
                 role.trim();
                 System.out.print("Please enter your companies location: ");
                 String location = in.readLine();
                 location.trim();
                 System.out.print("Please enter your start date: ");
                 String startdate = in.readLine();
                 while(!isDateValid(startdate))
                 {
                     System.out.print("Please enter a valid date: ");
                     startdate = in.readLine();
                 }
                 System.out.print("Please enter your end date: ");
                 String enddate = in.readLine();
                 while(!isDateValid(enddate))
                 {
                     System.out.print("Please enter a valid date: ");
                     enddate= in.readLine();
                 }
                 String query3 = String.format("INSERT INTO WORK_EXPR(userId,company,role,location,startdate,enddate) VALUES ('%s','%s','%s','%s','%s','%s')",user,company,role,location,startdate,enddate);
                 esql.executeUpdate(query3);

                //add work exp
            break;
            case 3:

                int i = 0;
                while(i < yoyo2.size())
                {
System.out.println("---------");
                System.out.println("Current company: " + yoyo2.get(i).get(1));

                System.out.println("Current role: " + yoyo2.get(i).get(2));

                System.out.println("Current location: " + yoyo2.get(i).get(3));

                System.out.println("Current startdate: " + yoyo2.get(i).get(4));

                System.out.println("Current enddate: " + yoyo2.get(i).get(5));
System.out.println("---------");
                System.out.print("Update? y/n: ");
                String z = in.readLine();
                while(true)
                {
                    if((z).equals("y") || (z).equals("n"))
                        break;
                    System.out.print("Enter y or n: ");
                    z = in.readLine();
                }
                if((z).equals("y"))
                {
                 System.out.print("Please enter your company name: ");
                 String company1 = in.readLine();
                  System.out.print("Please enter your role: ");
                 String role1 = in.readLine();
                 System.out.print("Please enter your companies location: ");
                 String location1 = in.readLine();
                 System.out.print("Please enter your start date (dd/mm/yyyy): ");
                 String startdate1 = in.readLine();
                 while(!isDateValid(startdate1))
                 {
                     System.out.print("Please enter a valid date (dd/mm/yyyy): ");
                     startdate1 = in.readLine();
                 }
                 System.out.print("Please enter your end date (dd/mm/yyyy): ");
                 String enddate1 = in.readLine();
                 while(!isDateValid(enddate1))
                 {
                     System.out.print("Please enter a valid date (dd/mm/yyyy): ");
                     enddate1= in.readLine();
                 }

                //String qq = String.format("UPDATE USR SET password = '%s' WHERE userid = '%',password = '%s'",newpw,user,checker);

                    String bb = String.format("UPDATE WORK_EXPR SET company = '%s', role ='%s', location = '%s', startdate = '%s', enddate = '%s' WHERE userid = '%s' AND company = '%s' AND role = '%s' AND location = '%s'AND startdate = '%s'AND enddate = '%s'",company1,role1,location1,startdate1,enddate1,yoyo2.get(i).get(0),yoyo2.get(i).get(1),yoyo2.get(i).get(2),yoyo2.get(i).get(3),yoyo2.get(i).get(4),yoyo2.get(i).get(5));

                esql.executeUpdate(bb);
                }
                ++i;
                }
                //update work exp
                break;
                default : System.out.println ("Unrecognized choice!"); break;
            }
       break;
       case 6:
System.out.println("Update profile");
                System.out.println("---------");
                System.out.println("1. View educational details");
                System.out.println("2. Add educational details");
                System.out.println("3. Update educational details");
System.out.println(".........................");
//System.out.println("Press anything else to return to the main menu\n");
            String query12 = String.format("SELECT * FROM EDUCATIONAL_DETAILS WHERE userid = '%s'",user);
            List<List<String>> yoyo12 = esql.executeQueryAndReturnResult(query12);
            switch(readChoice()){
                case 1:
                for(int i = 0; i < yoyo12.size();++i)
                {
                System.out.println("---------");
                System.out.println("Institution name: " + yoyo12.get(i).get(1));

                System.out.println("Major: " + yoyo12.get(i).get(2));

                System.out.println("Degree: " + yoyo12.get(i).get(3));

                System.out.println("Startdate: " + yoyo12.get(i).get(4));

                System.out.println("Enddate: " + yoyo12.get(i).get(5));
System.out.println("---------");

                }
                break;
            case 2:
                 System.out.print("Please enter your institution name: ");
                 String company21 = in.readLine();
                 company21.trim();
                  System.out.print("Please enter your major: ");
                 String role21 = in.readLine();
                 role21.trim();
                 System.out.print("Please enter your degree: ");
                 String location21 = in.readLine();
                 location21.trim();
                 System.out.print("Please enter your start date (dd/mm/yyyy): ");
                 String startdate21 = in.readLine();
                 while(!isDateValid(startdate21))
                 {
                     System.out.print("Please enter a valid date (dd/mm/yyyy): ");
                     startdate21 = in.readLine();
                 }
                 System.out.print("Please enter your end date (dd/mm/yyyy): ");
                 String enddate21 = in.readLine();
                 while(!isDateValid(enddate21))
                 {
                     System.out.print("Please enter a valid date (dd/mm/yyyy): ");
                     enddate21= in.readLine();
                 }
                 String query13 = String.format("INSERT INTO EDUCATIONAL_DETAILS(userId,instituitionName,major,degree,startdate,enddate) VALUES ('%s','%s','%s','%s','%s','%s')",user,company21,role21,location21,startdate21,enddate21);
                 esql.executeUpdate(query13);

                //add work exp
            break;
            case 3:

                int i = 0;
                while(i < yoyo12.size())
                {
System.out.println("---------");
                System.out.println("Current institution name: " + yoyo12.get(i).get(1));

                System.out.println("Current major: " + yoyo12.get(i).get(2));

                System.out.println("Current degree: " + yoyo12.get(i).get(3));

                System.out.println("Current startdate: " + yoyo12.get(i).get(4));

                System.out.println("Current enddate: " + yoyo12.get(i).get(5));
System.out.println("---------");
                System.out.print("Update? y/n: ");
                String z = in.readLine();
                while(true)
                {
                    if((z).equals("y") || (z).equals("n"))
                        break;
                    System.out.print("Enter y or n: ");
                    z = in.readLine();
                }
                if((z).equals("y"))
                {
                 System.out.print("Please enter your institution name: ");
                 String company13 = in.readLine();
                  System.out.print("Please enter your major: ");
                 String role13 = in.readLine();
                 System.out.print("Please enter your degree: ");
                 String location13 = in.readLine();
                 System.out.print("Please enter your start date (dd/mm/yyyy): ");
                 String startdate13 = in.readLine();
                 while(!isDateValid(startdate13))
                 {
                     System.out.print("Please enter a valid date (dd/mm/yyyy): ");
                     startdate13 = in.readLine();
                 }
                 System.out.print("Please enter your end date (dd/mm/yyyy): ");
                 String enddate13 = in.readLine();
                 while(!isDateValid(enddate13))
                 {
                     System.out.print("Please enter a valid date (dd/mm/yyyy): ");
                     enddate13= in.readLine();
                 }

                //String qq = String.format("UPDATE USR SET password = '%s' WHERE userid = '%',password = '%s'",newpw,user,checker);

                    String bb1 = String.format("UPDATE EDUCATIONAL_DETAILS SET instituitionName = '%s', major ='%s', degree = '%s', startdate = '%s', enddate = '%s' WHERE userid = '%s' AND instituitionName = '%s' AND major = '%s' AND degree = '%s'AND startdate = '%s'AND enddate = '%s'",company13,role13,location13,startdate13,enddate13,yoyo12.get(i).get(0),yoyo12.get(i).get(1),yoyo12.get(i).get(2),yoyo12.get(i).get(3),yoyo12.get(i).get(4),yoyo12.get(i).get(5));

                esql.executeUpdate(bb1);
                }
                ++i;
                }
                //update work exp
                break;
                case 9: break;
                default : System.out.println ("Unrecognized choice!"); break;
            }
       break;


       //break;
       default : System.out.println("Unrecognized choice!"); break;
       }

        System.out.println(".........................");
       }


        catch(Exception e){
            System.err.println(e.getMessage());
        }
       return -1;
   }

   public static int NewMessage(ProfNetwork esql, String user)
   {
       try{
       System.out.print("Enter name: ");
       String recipient = in.readLine();
       String chh = String.format("SELECT * FROM USR WHERE userid='%s'",recipient);
       int empty = esql.executeQuery(chh);
       if(empty == 0)
       {
           System.out.println("User does not exist");
           return -1;
       }

       List<List<String>> coont = esql.executeQueryAndReturnResult("SELECT * FROM MESSAGE");

       String query = String.format("SELECT * FROM MESSAGE WHERE senderid = '%s' AND receiverID = '%s'",user,recipient);
       int messagenumber = esql.executeQuery(query);
       System.out.print("Enter message: ");
       Calendar calendar = Calendar.getInstance();
       java.sql.Timestamp ourJavaTimeStampObject = new java.sql.Timestamp(calendar.getTime().getTime());
       String message = in.readLine();
        String meow = String.format("INSERT INTO MESSAGE(msgId,senderId,receiverId,contents,sendTime,deletestatus,status) VALUES ('%s','%s','%s','%s','%s','%s','%s')",coont.size(),user,recipient,message,ourJavaTimeStampObject,0,"Delivered");
//if deletestatus is 1, sender deletes it, if 2, receiver deletes, if 3 both delete.
esql.executeUpdate(meow);

}
catch(Exception e){
        //   System.err.println (e.getMessage ());
       }

       return -1;
   }

   public static int Viewsent(ProfNetwork esql, String user)
   {
       try{
           String query = String.format("SELECT * FROM MESSAGE WHERE senderid= '%s'",user);
           List<List<String>> sentmessages = esql.executeQueryAndReturnResult(query);
           for(int i = 0; i < sentmessages.size();++i)
           {

System.out.println("---------");
               System.out.println("Message: " + sentmessages.get(i).get(3));

               System.out.println("Recipient: " + sentmessages.get(i).get(2));
               System.out.println("Time: " + sentmessages.get(i).get(4));
System.out.println("---------");
               System.out.print("Delete message?");
                String z = in.readLine();
                while(true)
                {
                    if((z).equals("y") || (z).equals("n"))
                        break;
                    System.out.print("Enter y or n: ");
                    z = in.readLine();
                }
                if((z).equals("y"))
                {
                    String deletething = String.format("UPDATE MESSAGE SET senderid = '%s', deletestatus = '%s' WHERE msgId = '%s'", -1, sentmessages.get(i).get(5)+1,sentmessages.get(i).get(0));    esql.executeUpdate(deletething);


                }
           }
       }
       catch(Exception e){
       }
       return -1;

   }

   public static int SendRequest(ProfNetwork esql)
   {

       return -1;
   }
   public static int Searchperson(ProfNetwork esql, String user)
   {
    try{
System.out.println("---------");
       System.out.print("\tEnter name: ");
       String name1 = in.readLine();
       String query = String.format("SELECT userid, email, name FROM USR WHERE userid='%s'",name1);
       //int yoyo = esql.executeQueryAndPrintResult(query);
       int empty = esql.executeQuery(query);
       if(empty == 0)
       {
           System.out.println("There are no users with this id");
       }
       else{

       List<List<String>> yoyo = esql.executeQueryAndReturnResult(query);
       //String poop = yoyo.get(0).get(0);

       System.out.println("Userid: " + yoyo.get(0).get(0));
       System.out.println("Email: " + yoyo.get(0).get(1));
       System.out.println("Name: " + yoyo.get(0).get(2));
System.out.println("---------");
       //print other info like work/education i guess
        System.out.println("MENU");
                System.out.println("---------");
                System.out.println("1. Add as friend");
                System.out.println("2. Send a message");
System.out.println(".........................");
                System.out.println("Press anything else to go back\n");
                System.out.print("Please make your choice: ");
       String yes = in.readLine();
       int foo = Integer.parseInt(yes);
       if(foo == 1)
       {
           //addfriend
           System.out.println("Friend request send");
           //or System.out.print("friend already in list)";
       }
       else if(foo == 2)
       {
           //sendmessage
           //
           //
           //
           //
           //
           //IF WITHIN 3 CONNECTIONS OR 5 FOR NEW. idk how to do it
           //
           //
        List<List<String>> coont = esql.executeQueryAndReturnResult("SELECT * FROM MESSAGE");

       String queryy = String.format("SELECT * FROM MESSAGE WHERE senderid = '%s' AND receiverID = '%s'",user,name1);
       int messagenumber = esql.executeQuery(queryy);
       System.out.print("Enter message: ");
       Calendar calendar = Calendar.getInstance();
       java.sql.Timestamp ourJavaTimeStampObject = new java.sql.Timestamp(calendar.getTime().getTime());
       String message = in.readLine();
        String meow = String.format("INSERT INTO MESSAGE(msgId,senderId,receiverId,contents,sendTime,deletestatus,status) VALUES ('%s','%s','%s','%s','%s','%s','%s')",coont.size(),user,name1,message,ourJavaTimeStampObject,0,"Delivered");
//if deletestatus is 1, sender deletes it, if 2, receiver deletes, if 3 both delete.
esql.executeUpdate(meow);

           System.out.println("Message sent");
           }
        System.out.println(".........................");
       }

}
catch(Exception e){
        //   System.err.println (e.getMessage ());
       }
       return -1;
   }

// Rest of the functions definition go in here

}//end ProfNetwork
