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
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: FriendList(esql,authorisedUser); break;
                   case 2: UpdateProfile(esql,authorisedUser); break;
                   case 3: NewMessage(esql); break;
                   case 4: SendRequest(esql); break;
                   case 5: Searchperson(esql,authorisedUser); break;
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
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user email: ");
         String email = in.readLine();
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
       //executeQueryAndPrintResult
       try{

System.out.println("---------");
       String query = String.format("SELECT userid FROM USR WHERE userId = '%s'",user);
       int yoyo = esql.executeQueryAndPrintResult(query);
       System.out.println("yoyo activated");

System.out.println(".........................");
       }
       catch(Exception e){
           System.err.println (e.getMessage ());
       }
       return -1;
   }

   public static int UpdateProfile(ProfNetwork esql, String user)
   {
       try{
           String query = String.format("SELECT * FROM USR WHERE userid ='%s'",user);

       List<List<String>> yoyo = esql.executeQueryAndReturnResult(query);

  System.out.println("Update profile");
                System.out.println("---------");
                System.out.println("1. Change password");
                System.out.println("2. Update work experience");
                System.out.println("3. Update Educational details");
System.out.println(".........................");
System.out.println("Press anything else to return to the main menu\n");
                System.out.print("Please make your choice: ");
                String yes = in.readLine();
            int foo = Integer.parseInt(yes);
       if(foo == 1)
       {
           System.out.print("Please enter your current password: ");
           String  checker = in.readLine();
           if((checker).equals(yoyo.get(0).get(1)))
           {
               System.out.print("Password verified, please enter your new password: ");
               String newpw = in.readLine();
               String qq = String.format("UPDATE USR SET password = '%s' WHERE userid = '%',password = '%s'",newpw,user,checker);
               esql.executeUpdate(qq);
               //update pw
           }
           else
           {
                System.out.println("Incorrect password");
                //kill them
           }

       }
       else if(foo == 2)
       {
System.out.println("Update profile");
                System.out.println("---------");
                System.out.println("1. Add work experience");
                System.out.println("2. Update previous work experience");
System.out.println(".........................");
System.out.println("Press anything else to return to the main menu\n");
                System.out.print("Please make your choice: ");
                String yaa = in.readLine();
                int yaa1 = Integer.parseInt(yaa);
            String query2 = String.format("SELECT * FROM WORK_EXPR WHERE userid = '%s'",user);
            int testtest = esql.executeQuery(query2);
            List<List<String>> yoyo2 = esql.executeQueryAndReturnResult(query2);

            if(yaa1 == 1)
            {
//String query = String.format("INSERT INTO USR (userId, password, email, name) VALUES ('%s','%s','%s','%s')", login, password, email,name);
                 System.out.print("Please enter your company name: ");
                 String company = in.readLine();
                 company.trim();
                  System.out.print("Please enter your roll ");
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
            }
            else
            {
                int i = 0;
                while(i < yoyo2.size())
                {
                System.out.println("Current company: " + yoyo2.get(i).get(1));

                System.out.println("Current role: " + yoyo2.get(i).get(2));

                System.out.println("Current location: " + yoyo2.get(i).get(3));

                System.out.println("Current startdate: " + yoyo2.get(i).get(4));

                System.out.println("Current enddate: " + yoyo2.get(i).get(5));
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
                 String company = in.readLine();
                 company.trim();
                  System.out.print("Please enter your roll ");
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

                //String qq = String.format("UPDATE USR SET password = '%s' WHERE userid = '%',password = '%s'",newpw,user,checker);

                    String bb = String.format("UPDATE WORK_EXPR SET company = '%s', role ='%s', location = '%s', startdate = '%s', enddate = '%s' WHERE userid = '%s' AND company = '%s' AND role = '%s' AND location = '%s'AND startdate = '%s'AND enddate = '%s'",company,role,location,startdate,enddate,yoyo2.get(i).get(0),yoyo2.get(i).get(1),yoyo2.get(i).get(2),yoyo2.get(i).get(3),yoyo2.get(i).get(4),yoyo2.get(i).get(5));

                System.out.println(bb);
                esql.executeUpdate(bb);
                }
                ++i;
                }
                //update work exp
            }
       }
       else if(foo == 3)
       {

       }
        System.out.println(".........................");
       }


        catch(Exception e){
            System.err.println(e.getMessage());
        }
       return -1;
   }

   public static int NewMessage(ProfNetwork esql)
   {
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
