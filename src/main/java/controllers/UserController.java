package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"),
                    rs.getLong("created_at"));

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }


// Selv tilføjet
  public static User getUserByEmail(String email) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where email='" + email +"'";

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user = new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getLong("created_at"));
        //Selv tilføjet
        Algorithm algorithm = Algorithm.HMAC256("secret");
        String token = JWT.create().withClaim("userID", user.getId()).sign(algorithm);
        user.setToken(token);

        // return the create object
        return user;
      } else {
        System.out.println("No User found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"),
                    rs.getLong("created_at"));

        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    //Selv tilføjet
    Hashing hashing = new Hashing();

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it. : Fix
    int userID = dbCon.insert(
        "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
            + user.getFirstname()
            + "', '"
            + user.getLastname()
            + "', '"
            + hashing.hashWithSalt(user.getPassword()) //selv tilføjet
            + "', '"
            + user.getEmail()
            + "', "
            + user.getCreatedTime()
            + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else{
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }

  public static void delete(int id) {

    Log.writeLog(UserController.class.getName(), id, "Actually deleting a user in DB", 0);

    if(dbCon == null){
     dbCon = new DatabaseController();
    }
    String sql = "DELETE FROM user WHERE id = " + id;

    dbCon.deleteUpdate(sql);

  }

  public static User updateUser(User userInfo) {
// Selv tilføjet

   // Hashing hashing = new Hashing();

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    DecodedJWT jwt = null;
    try{
      jwt = JWT.decode(userInfo.getToken());
    }catch (JWTDecodeException e){
      e.printStackTrace();
    }

    int id = jwt.getClaim("userID").asInt();

    User userToChange = getUser(id);


    if (userInfo.getFirstname() != null)
      userToChange.setFirstname(userInfo.getFirstname());
    if (userInfo.getLastname() != null)
      userToChange.setLastname(userInfo.getLastname());
    if (userInfo.getPassword() != null)
      userToChange.setPassword(userInfo.getPassword());
    if (userInfo.getEmail() != null)
      userToChange.setEmail(userInfo.getEmail());

    //The password is hased before saving it.
    String sql = "UPDATE user set first_name= '" + userToChange.getFirstname() +
            "', last_name = '" + userToChange.getLastname() +
            "', password = '" + userToChange.getPassword() +
            "', email = '" + userToChange.getEmail() +
            "' WHERE id = " + userToChange.getId();


    if (dbCon.deleteUpdate(sql)) {
      return userToChange;
    }

return null;
  }

}
