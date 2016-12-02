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

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   //login info for later use
   private static String authorisedUser = null;

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe (String dbname, String dbport) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://127.0.0.1:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Cafe

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
		//System.out.printf("%-24s", rsmd.getColumnName(i)); 
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString(i));
            // System.out.printf("%24s", rs.getString(i));
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
      if (args.length != 2) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Cafe.class.getName () +
            " <dbname> <port>");
         return;
      }//end if

      Greeting();
      Cafe esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         esql = new Cafe (dbname, dbport);

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              String user_type = find_type(esql);
              //System.out.println(user_type); // to test user type
	      switch (user_type){
		case "Customer": 
		  while(usermenu) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Order History");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: UpdateOrder(esql); break;
                       case 5: ViewOrderHistory(esql); break;
                       case 6: ViewOrderStatus(esql); break;
                       case 7: UpdateUserInfo(esql); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
		case "Employee": 
		  while(usermenu) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Current Orders");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: EmployeeUpdateOrder(esql); break;
                       case 5: ViewCurrentOrder(esql); break;
                       case 6: ViewOrderStatus(esql); break;
                       case 7: UpdateUserInfo(esql); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
		case "Manager ":		// full string has space at end or it won't detect 
		  while(usermenu) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Current Orders");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println("8. Update Menu");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: EmployeeUpdateOrder(esql); break;
                       case 5: ViewCurrentOrder(esql); break;
                       case 6: ViewOrderStatus(esql); break;
                       case 7: ManagerUpdateUserInfo(esql); break;
                       case 8: UpdateMenu(esql); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
	      }//end switch
            }//end if
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
         "              User Interface                         \n" +
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
    **/
   public static void CreateUser(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();
         
	 String type="Customer";
	 String favItems="";

	 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end
   
   /*queryandreturnresult
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Users WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
	 System.out.println("Incorrect PW or user does not exist. (case-sensitive)");
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage());
         return null;
      }
   }//end
   
   /*find_type
    * Returns the type of user that has just gone through the LogIn check
    * @return User type or null if the type cannot be retrieved
    **/

   public static String find_type(Cafe esql){ // completed by Ronson
      String type = "There is an error";
      try{
	 //String login = authorisedUser; 
	 String query = String.format("SELECT type FROM Users WHERE login = '%s'", authorisedUser);
	 List<List<String>> result = esql.executeQueryAndReturnResult(query); 
	 if (result.size() > 0) { // If no data, return error --> all users should have a type
	    type = result.get(0).get(0);
	 }
	 else {
	    System.err.println("There was an error in retrieving the user type.");
	    return null;
	 }
         
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
      
      return type;
   }//end

   public static void BrowseMenuName(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void BrowseMenuType(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static Integer AddOrder(Cafe esql){
      // Your code goes here.
      // ...
      // ...
      Integer orderid=0;
      return orderid;
   }//end 

   public static void UpdateOrder(Cafe esql){ // this function is for customer only || completed by RL
      // Only update, add is separate
      // Allowed to update any non-paid order created by THEMSELVES. 
      // In order to modify an order, the Customer should type in the OrderID for that order.
      // This means orderhistory or orderstatus should contain the orderIDs, for the user to see.
      
      // Menu for customer on updating orders .... order is not paid AND item status = hasnt started
      // 1. Modify order ---> input orderid ---> display all items in order --> add/remove --> pick item from display
      //    a. add item  
      //    b. remove item (only if item status hasnt started).. when adding or removing item, also update order price total
      //    c. edit comment on item
      //    d. Cancel
      // 2. Go back
      boolean customermenu = true;
      try {
         while(customermenu) {
            System.out.println("UPDATE MENU");
            System.out.println("-----------");
            System.out.println("1. Modify Order");
            System.out.println("...............");
            System.out.println("2. Go back");
            switch (readChoice()) {
               case 1:
                  int input;
                  System.out.println("Please enter the orderID:");
                  try {
                     input = Integer.parseInt( in.readLine());
                  }catch (Exception e) {
                      System.out.println("Your input is invalid!");
                      break;
                  } 
                  // check if orderID is accessible for current user
                  // if not, break;
		  String query = String.format("SELECT * FROM Orders WHERE login = '%s' AND orderid = '%s' AND paid='false'", authorisedUser, input);
                  int check = esql.executeQuery(query);
		  if (check <=  0) {
		     System.out.println("You did not place this order or the order has already been paid.");
		     break;
                  }
                  // else, print list of items for order  || IMPORTANT: PRIMARY KEY -> orderID + itemname --> itemname is unique in an order
                  else {
		     String test = "%Hasn''t%";
	             query = String.format("Select itemName,comments FROM ItemStatus WHERE orderid = '%s' AND status LIKE '%s'", input, test);
		     System.out.println("YOUR ORDER || ONLY ITEMS THAT CAN BE MODIFIED ARE SHOWN");
		     System.out.println("-------------------------------------------------------");
		     int check_item = esql.executeQueryAndPrintResult(query);
                     if (!(check_item > 0)) {
                        System.out.println("There are no items that can be modified for this order.");
                        break;
                     }
		     System.out.println("-------------------------------------------------------");
		  }  
                  boolean mod = true;
                  while(mod) {
                     //here, give options to add or remove item from order IF item is 'hasnt started' OPTIONAL
                     System.out.println("UPDATE MENU");
		     System.out.println("-----------");
                     System.out.println("1. Edit comment");
                     System.out.println("...............");
                     System.out.println("2. Go back");
                     switch (readChoice()) {
                        case 1:
                           System.out.println("Please enter the name of the item you wish to modify.");
                           String item = "%" + in.readLine() + "%"; 
                           System.out.println("Please enter your comment(130 chars max)");
                           String userInput = in.readLine();
			   if (userInput.length() > 130) {
                              System.out.println("Exceeded max character limit. Update failed.");
                              break;
                           }
                           else {
                              query = String.format("UPDATE ItemStatus SET comments='%s' WHERE orderid = '%s' AND itemName LIKE '%s'", userInput, input, item );
                              esql.executeUpdate(query);
			      break;
                           }
                        case 2:
                           mod = false;
                           break;
                     }
                  } 
                  break;
               case 2:
                  customermenu = false;
                  break;
            }
         }
      }catch(Exception e) {
          System.err.println (e.getMessage ());
      }
   }//end

   public static void EmployeeUpdateOrder(Cafe esql){ // this function is for employee/manager
      boolean employeemenu = true;
      int oid;
      String query;
      try {
         while (employeemenu) {
            System.out.println("UPDATE MENU");
            System.out.println("-----------");
            System.out.println("1. Update item status");
            System.out.println("2. Update order status");
            System.out.println("......................");
            System.out.println("3. Go back");
            switch (readChoice()) {
               case 1:
                  System.out.println("Enter the order id to modify: ");
                  try {
                     oid = Integer.parseInt(in.readLine());
                  }catch(Exception e) {
                     System.out.println("Your input is invalid!");
                     break;
                  }
                  query = String.format("SELECT * FROM Orders WHERE orderid = '%s'", oid);
                  int oid_check = esql.executeQuery(query);
                  if (oid_check > 0) {
                     query = String.format("SELECT itemName,status FROM ItemStatus WHERE orderid = '%s'", oid);
                     System.out.println("ITEMS FOR THIS ORDER");
                     System.out.println("---------------------------------");
                     int check_item = esql.executeQueryAndPrintResult(query);
                     if (!(check_item > 0)) {
                        System.out.println("For some reason, there are no items in this order...");
                        break;
                     }
                     System.out.println("---------------------------------");
                     boolean item_menu = true;
                     while (item_menu) {
                        System.out.println("ITEM STATUS UPDATE");
                        System.out.println("------------------");
                        System.out.println("1. Hasn't started");
                        System.out.println("2. Started");
                        System.out.println("3. Finished");
                        System.out.println(".................");
                        System.out.println("4. Go back");
                        switch (readChoice()) {
                           case 1:
                              System.out.println("Enter the item you want to modify: ");
                              String item = in.readLine();
                              query = String.format("UPDATE ItemStatus SET status='Hasn''t Started' WHERE orderid='%s' AND itemName='%s'", oid, item);
                              esql.executeUpdate(query);
                              System.out.println("Status for item successfully changed to 'Hasn't Started'");
                              break;
                           case 2: 
                              System.out.println("Enter the item you want to modify: ");
                              item = in.readLine();
                              query = String.format("UPDATE ItemStatus SET status='Started' WHERE orderid='%s' AND itemName='%s'", oid, item);
                              esql.executeUpdate(query);
                              System.out.println("Status for item successfully changed to 'Started'");
                              break;
                           case 3:
                              System.out.println("Enter the item you want to modify: ");
                              item = in.readLine();
                              query = String.format("UPDATE ItemStatus SET status='Finished' WHERE orderid='%s' AND itemName='%s'", oid, item);
                              esql.executeUpdate(query);
                              System.out.println("Status for item successfully changed to 'Finished'");
                              break;
                           case 4:
                              item_menu = false;
                              break;
                        }   
                     }
                  }
                  else {
                     System.out.println("This order does not exist.");
                     break;
                  }
                  break;
               case 2:
                  System.out.println("Enter the order id to modify: ");
                  try {
                     oid = Integer.parseInt(in.readLine());
                  }catch(Exception e) {
                     System.out.println("Your input is invalid!");
                     break;
                  }
                  query = String.format("SELECT paid FROM Orders WHERE orderid='%s'", oid);
                  oid_check = esql.executeQuery(query);
                  if (oid_check > 0) {
                     System.out.println("-----------------------------------------------");
                     esql.executeQueryAndPrintResult(query);
                     System.out.println("-----------------------------------------------");
                     System.out.println("ORDER STATUS UPDATE");
                     System.out.println("-------------------");
                     System.out.println("1. Paid");
                     System.out.println("2. Not paid");
                     System.out.println("...........");
                     System.out.println("3. Go back");
                     boolean order_menu = true;
                     while (order_menu) {
                        switch (readChoice()) {    // don't want to continuously loop for this, b/c it is either/or for a single order
                           case 1:
                              query = String.format("UPDATE Orders SET paid='true' WHERE orderid='%s'", oid);
                              esql.executeUpdate(query);
                              System.out.println("Status for order successfully changed.");
                              order_menu = false;
                              break;
                           case 2:
                              query = String.format("UPDATE Orders SET paid='false' WHERE orderid='%s'", oid);
                              esql.executeUpdate(query);
                              System.out.println("Status for order successfully changed.");
                              order_menu = false;
                              break;
                           case 3:
                              order_menu = false;
                              break;
                        }
                     }
                  }
                  else {
                     System.out.println("This order does not exist.");
                     break;
                  }
               case 3:
                  employeemenu = false;
                  break;

            }
         }
      }catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }//end

   public static void ViewOrderHistory(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void UpdateUserInfo(Cafe esql){ // customer/employee share this function
      // Your code goes here.
      // ...
      // check user type before completing algorithm
   }//end

   public static void ManagerUpdateUserInfo(Cafe esql){ // manager uses this function
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void UpdateMenu(Cafe esql){ // for manager only
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void ViewOrderStatus(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void ViewCurrentOrder(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void Query6(Cafe esql){ // useless function?
      // Your code goes here.
      // ...
      // ...
   }//end Query6

}//end Cafe
