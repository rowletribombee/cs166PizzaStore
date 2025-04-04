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
import java.sql.Timestamp;
import java.util.Date;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

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
   }//end PizzaStore

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
      stmt.close();
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
       while (rs.next()){
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
            PizzaStore.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      PizzaStore esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore (dbname, dbport, user, "");

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
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Menu");
                System.out.println("4. Place Order"); //make sure user specifies which store
                System.out.println("5. View Full Order ID History");
                System.out.println("6. View Past 5 Order IDs");
                System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                System.out.println("8. View Stores"); 

                //**the following functionalities should only be able to be used by drivers & managers**
                System.out.println("9. Update Order Status");

                //**the following functionalities should ony be able to be used by managers**
                System.out.println("10. Update Menu");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewProfile(esql, authorisedUser); break;
                   case 2: updateProfile(esql, authorisedUser); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql, authorisedUser); break;
                   case 5: viewAllOrders(esql, authorisedUser); break;
                   case 6: viewRecentOrders(esql, authorisedUser); break;
                   case 7: viewOrderInfo(esql, authorisedUser); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(esql, authorisedUser); break;
                   case 10: updateMenu(esql, authorisedUser); break;
                   case 11: updateUser(esql, authorisedUser); break;



                   case 20: usermenu = false; break;
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
    * Creates a new user
    **/
   public static void CreateUser(PizzaStore esql){
      
      String fixedLogin = "";
      try{
         System.out.println("\nEnter your login (up to 50 characters): ");
         String newLogin = in.readLine();
         String newLoginFixed = newLogin.substring(0, Math.min(newLogin.length(), 50));
         System.out.println("\nYour login is: " + newLoginFixed);
         fixedLogin = "SELECT login FROM Users WHERE login = '"+ newLoginFixed+ "'";
         int x = esql.executeQuery(fixedLogin);
         if(x != 0){
            throw new RuntimeException("This login is already in use. \n");
         }
         System.out.println("\nEnter your password (up to 30 characters): ");
         String password = in.readLine();
         password = password.substring(0, Math.min(password.length(), 30));
         System.out.println("\nEnter your phone number: ");
         String phonenum = in.readLine();
         phonenum = phonenum.substring(0, Math.min(phonenum.length(), 20));

      String query = "INSERT INTO Users(login, password, role, phoneNum) VALUES('" + newLoginFixed + "','" + password +"',  'customer','" + phonenum +"')";
      esql.executeUpdate(query);
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(PizzaStore esql){
      boolean getlogin = false;
      String  FixedNewLogin= "";
      try{
      while(!getlogin){ //get login, check if login exists
         System.out.println("\nEnter your login (up to 50 characters): ");
         String newLogin = in.readLine();
         FixedNewLogin = newLogin.substring(0, Math.min(newLogin.length(), 50));
         System.out.println("\nYour login is: " + newLogin);
         String loginquery = "SELECT login FROM Users WHERE login = '"+ FixedNewLogin+ "'";
         int x = esql.executeQuery(loginquery);
         if(x != 0){
            getlogin = true;
         }
         else{
            System.out.println("This login does not exist \n");
            return null;
         }
      }
         System.out.println("\nEnter your password: ");
         String pwd = in.readLine();
         pwd = pwd.substring(0, Math.min(pwd.length(), 30));
         pwd = "SELECT login FROM Users WHERE login = '"+ FixedNewLogin+ "'AND password = '" + pwd + "'";
         if(esql.executeQuery(pwd) != 1){
            System.out.println("This login and password is invalid\n");
            return null;
         }
         return FixedNewLogin;
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
      return null;
   }

// Rest of the functions definition go in here

   public static void viewProfile(PizzaStore esql, String user) {
      try{
         String query = "SELECT* FROM Users WHERE login = '" + user + "' AND role = 'customer'";
         if((esql.executeQuery(query) == 1)){
            query = "SELECT favoriteItems, phoneNum FROM Users WHERE login = '"+ user + "'";
            esql.executeQueryAndPrintResult(query);
         }
         else{
            query = "SELECT* FROM Users";
            esql.executeQueryAndPrintResult(query);
         }
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void updateProfile(PizzaStore esql, String user) {
      try{
            String query = null;
            String newParam = null;
            System.out.println("Select choice to update: ");
            System.out.println("1. Favorite Item");
            System.out.println("2. Phone Number");
            switch (Integer.parseInt(in.readLine())){
                   case 1: System.out.println("Give the name of the new Favorite Item: ");
                           newParam = in.readLine();
                           query = "UPDATE Users SET favoriteItems = '"+ newParam + "' WHERE login = '" + user + "'";
                           esql.executeUpdate(query);
                           break;
                   case 2: System.out.println("Give the new Phone Number: ");
                           newParam = in.readLine();
                           query = "UPDATE Users SET phoneNum = '"+ newParam + "' WHERE login = '" + user + "'";
                           esql.executeUpdate(query);
                           break;
            }
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   
   public static void viewMenu(PizzaStore esql) {
      try{
         
            String conditions = "";
            String sorting = "";
            System.out.println("Filter by item type?"); //choose whether or not to filter by item
            System.out.println("1. Yes");
            System.out.println("2. No");
            switch(readChoice()){
               case 1: System.out.println("Which type?"); //adds to sql conditions
                       System.out.println("1. Entree"); 
                       System.out.println("2. Drink");
                       System.out.println("3. Side");
                       switch(readChoice()){
                           case 1: conditions = "WHERE typeOfItem LIKE '%entree%'"; break;
                           case 2: conditions = "WHERE typeOfItem LIKE '%drinks%'"; break;
                           case 3: conditions = "WHERE typeOfItem LIKE '%sides%'"; break;

                           default : System.out.println("Unrecognized choice!"); break;
                        }
                        break;
               case 2: 

               default : System.out.println("Unrecognized choice!"); break;
            }
            System.out.println("Filter by max price?"); //choose whether or not to add more conditions
            System.out.println("1. Yes");
            System.out.println("2. No");
            switch(readChoice()){
               case 1: System.out.println("What price?");
                       float priceCap = Float.parseFloat(in.readLine());
                       if(conditions == null){
                           conditions = "WHERE price < " + priceCap;
                       }
                       else{
                           conditions = conditions + "AND price < " + priceCap;
                       }
                       break;
               case 2: //conditions same as before, null if no conditions

               default : System.out.println("Unrecognized choice!"); break;
            }
            System.out.println("Sort Data?");
            System.out.println("1. Ascending Price");
            System.out.println("2. Descending Price");
            System.out.println("3. No");
            switch(readChoice()){
               case 1: sorting = "ORDER BY Price ASC"; break;
               case 2: sorting = "ORDER BY Price DESC"; break;
               case 3: sorting = ""; break;
            }
            String query = "SELECT* FROM Items " + conditions + sorting;
            esql.executeQueryAndPrintResult(query); 
            
         
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void placeOrder(PizzaStore esql, String user) {
      try{
         System.out.println("Enter the storeID of the store you wish to order from: ");
         int storeOrder = Integer.parseInt(in.readLine());
         String query = "SELECT Max(orderID) FROM FoodOrder";
         int orderID = Integer.parseInt(((esql.executeQueryAndReturnResult(query)).get(0).get(0))) + 1; //orderID is the max existing orderID + 1
         boolean finishedOrdering = false;
         ArrayList<Integer> quantities = new ArrayList<Integer>();
         ArrayList<String> items = new ArrayList<String>();
         while(!finishedOrdering){ //loop to add items to order
            System.out.println("Give the item name you wish to add: ");
            String itemName = in.readLine();
            items.add(itemName);
            System.out.println("Give the number of the item you wish to add");
            int itemQuantity = Integer.parseInt(in.readLine());
            quantities.add(itemQuantity);
            if(itemQuantity <= 0){
               throw new RuntimeException("No negative item quantities");
            }
            System.out.println("Add more items?");
            System.out.println("1. Order more");
            System.out.println("2. Finish Ordering");
            int choice = Integer.parseInt(in.readLine());
            if(choice == 2){
               finishedOrdering = true;
            }
         }
         float totalCost = 0;
         float itemPrice = 0;
         for(int i = 0; i < items.size(); i++){ //goes through each item in the order to get its price
            query = "SELECT price FROM Items WHERE itemName LIKE '" + items.get(i) + "%'";
            itemPrice = Float.parseFloat(((esql.executeQueryAndReturnResult(query)).get(0)).get(0)); //gets price of item
            totalCost = itemPrice*quantities.get(i);
         }
         System.out.println("Total Price: " + totalCost);
         System.out.println("Confirm order?");
         System.out.println("1. Yes");
         System.out.println("2. No");
         if(Integer.parseInt(in.readLine()) == 2){
            return;
         }
         query = "INSERT INTO FoodOrder VALUES(" + orderID + ", '" + user + "', " + storeOrder + ", " + totalCost + ", 'now' , 'incomplete')";
         esql.executeUpdate(query);
         for(int i = 0; i < items.size(); i++){
            query = "INSERT INTO ItemsInOrder VALUES(" + orderID + ", '"+ items.get(i) +"', " + quantities.get(i) + ")"; //add new items to FoodOrder
            esql.executeUpdate(query);
         }
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void viewAllOrders(PizzaStore esql, String user) {
      try{
         String query = "SELECT* FROM Users WHERE login = '" + user + "'";
         String role = (esql.executeQueryAndReturnResult(query).get(0)).get(2);
         if(role != "manager" && role != "driver"){
            query = "SELECT OrderID FROM foodOrder WHERE login = '" + user + "' ORDER BY orderTimestamp DESC";
         }
         else{
            query = "SELECT* FROM foodOrder ORDER BY orderTimestamp DESC";
         }
         esql.executeQueryAndPrintResult(query);
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void viewRecentOrders(PizzaStore esql, String user) {
      try{
         String query = "SELECT* FROM Users WHERE login = '" + user + "'";
         String role = (esql.executeQueryAndReturnResult(query).get(0)).get(2);
         if(role != "manager" && role != "driver"){
            query = "SELECT OrderID FROM foodOrder WHERE login = '" + user + "' ORDER BY orderTimestamp DESC LIMIT 5";
         }
         else{
            query = "SELECT* FROM foodOrder ORDER BY orderTimestamp DESC LIMIT 5";
         }
         esql.executeQueryAndPrintResult(query);
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void viewOrderInfo(PizzaStore esql, String user) {
      try{
         String query = "SELECT* FROM Users WHERE login = '" + user + "'";
         String role = (esql.executeQueryAndReturnResult(query).get(0)).get(2);
         System.out.println("Specify the orderID of the order you want to view: ");
         int orderID = Integer.parseInt(in.readLine());
         if(role == "manager" || role == "driver"){
            query = "SELECT* FROM FoodOrder WHERE orderID = " + orderID;
            esql.executeQueryAndPrintResult(query);
         }
         else{
            query = "SELECT* FROM FoodOrder WHERE orderID = " + orderID + "AND login = '" + user + "'";
            if(esql.executeQuery(query) == 0){
               throw new RuntimeException("This order does not exist");
            }
            esql.executeQueryAndPrintResult(query);
         }
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void viewStores(PizzaStore esql) {
      try{
         String query = "Select* FROM Store";
         esql.executeQueryAndPrintResult(query);
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void updateOrderStatus(PizzaStore esql, String user) {
      try{
         String query = "SELECT* FROM Users WHERE login = '" + user + "'";
         String role = (esql.executeQueryAndReturnResult(query).get(0)).get(2);
         if(role != "customer"){
            System.out.println("Enter the OrderID: ");
            String itemName = in.readLine();
            System.out.println("Enter the new order status");
            String status = in.readLine();
            query = "SELECT* FROM Items WHERE itemName LIKE '"+ itemName + "'";
            if(esql.executeQuery(query) != 0){ //if the item exists
               query = "UPDATE Items Set orderStatus = '" + status + "' WHERE itemName LIKE '"+ itemName + "'";
               esql.executeUpdate(query);
            }
            else{
               throw new RuntimeException("No such order exists");
            }
            System.out.println("Finished update.");
         }
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void updateMenu(PizzaStore esql, String user) {
      try{
         String query = "SELECT* FROM Users WHERE login = '" + user + "'";
         String managerStatus = (esql.executeQueryAndReturnResult(query).get(0)).get(2); //get role from first entry (should be the only entry)
         if(managerStatus == "manager"){
            System.out.println("Enter the name of the item you want to update/add: ");
            String itemName = in.readLine();
            System.out.println("Enter the list of its ingredients: ");
            String ingredients = in.readLine();
            System.out.println("Enter the item type: ");
            String type = in.readLine();
            System.out.println("Enter the price: ");
            float price = Float.parseFloat(in.readLine());
            System.out.println("Enter the description (optional): ");
            String description = in.readLine();
            query = "SELECT* FROM Items WHERE itemName LIKE '"+ itemName + "'";
            if(esql.executeQuery(query) != 0){ //if the item exists
               query = "UPDATE Items Set \"ingredients\" = '\"" + ingredients + "\"', typeOfItem = '" + type + "', price = "+ price + ", \"description\" = '\"" + description + "\"' WHERE itemName LIKE '"+ itemName + "'"; 
            }
            else{
               query = "INSERT INTO Items VALUES('" + itemName + "', '\"" + ingredients + "\"', '" + type + "', "+ price + ", '\"" + description + "\"')";
            }
            esql.executeUpdate(query);
            System.out.println("Finished update.");
            
         }
         else{
            System.out.println("You lack the privileges to do this");
         }
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void updateUser(PizzaStore esql, String user) {
      try{
         String query = "SELECT* FROM Users WHERE login = '" + user + "'";
         String managerStatus = (esql.executeQueryAndReturnResult(query).get(0)).get(2); //get role from first entry (should be the only entry)
         if(managerStatus == "manager"){ //check if user is a manager
            String targetUser = "";
            String newParam = null;
            System.out.println("Select choice to update: ");
            System.out.println("1. Favorite Item");
            System.out.println("2. Phone Number");
            System.out.println("3. Login");
            System.out.println("4. Role");
            switch (Integer.parseInt(in.readLine())){
                   case 1: System.out.println("Give the login of the user you want to update: ");
                           targetUser = in.readLine();
                           System.out.println("Give the name of the new Favorite Item: ");
                           newParam = in.readLine();
                           query = "UPDATE Users SET favoriteItems = '"+ newParam + "' WHERE login = '" + targetUser + "'";
                           esql.executeUpdate(query);
                           break;
                   case 2: System.out.println("Give the login of the user you want to update: ");
                           targetUser = in.readLine();
                           System.out.println("Give the new Phone Number: ");
                           newParam = in.readLine();
                           query = "UPDATE Users SET phoneNum = '"+ newParam + "' WHERE login = '" + targetUser + "'";
                           esql.executeUpdate(query);
                           break;
                   case 3: System.out.println("Give the login of the user you want to update: ");
                           targetUser = in.readLine();
                           System.out.println("Give the new login: ");
                           newParam = in.readLine();
                           String loginquery = "SELECT login FROM Users WHERE login = '"+ newParam+ "'";
                           int x = esql.executeQuery(loginquery);
                           if(x != 0){
                              throw new RuntimeException("This login is already in use");
                           }
                           query = "UPDATE Users SET login = '"+ newParam + "' WHERE login = '" + targetUser + "'";
                           esql.executeUpdate(query);
                           break;
                   case 4: System.out.println("Give the login of the user you want to update: ");
                           targetUser = in.readLine();
                           String rolecheck = "SELECT* FROM Users WHERE login = '" + user + "'";
                           if((esql.executeQueryAndReturnResult(query).get(0)).get(2) == "manager"){
                              throw new RuntimeException("This user is a manager, you can't update another manager.");
                           }
                           System.out.println("Give the new role: ");
                           newParam = in.readLine();
                           query = "UPDATE Users SET role = '"+ newParam + "' WHERE login = '" + targetUser + "'";
                           esql.executeUpdate(query);
                           break;

                   default : System.out.println("Unrecognized choice!"); 
            }
         }
         else{
            System.out.println("You are not a manager, you can't do this.");
         }
      }
      catch(Exception e){
         System.err.println("Update error: " + e.getMessage());
      }
   }


}//end PizzaStore

