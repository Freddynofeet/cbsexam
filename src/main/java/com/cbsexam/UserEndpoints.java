package com.cbsexam;

import cache.ProductCache;
import cache.UserCache;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import javassist.bytecode.stackmap.BasicBlock;
import model.User;
import utils.Encryption;
import utils.Hashing;
import utils.Log;

@Path("user")
public class UserEndpoints {
//Selv tilføjet
  private static UserCache userCache = new UserCache();

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {
//Selv tilføjet
    try {

      // Use the ID to get the user from the controller.
      User user = UserController.getUser(idUser);

      // TODO: Add Encryption to JSON : fix
      // Convert the user object to json in order to return the object
      String json = new Gson().toJson(user);

      // Added encryption
      json = Encryption.encryptDecryptXOR(json);

      // Return the user with the status code 200
      // TODO: What should happen if something breaks down? : fix

      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
      // Selv tilføjet
    } catch (Exception e){
      return Response.status(400).type(MediaType.APPLICATION_JSON_TYPE).entity("Could not get user").build();
    }
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    // Selv tilføjet
    ArrayList<User> users = userCache.getUsers(false);

    // TODO: Add Encryption to JSON : fix
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);

    // Added encryption
    json = Encryption.encryptDecryptXOR(json);

   // json = Encryption.encryptDecryptXOR(json);

    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system.
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String x) {

    User loginUser = new Gson().fromJson(x, User.class);

    User dbUser = UserController.getUserByEmail(loginUser.getEmail());

    String json =new Gson().toJson(dbUser);

    if (loginUser.getEmail().equals(dbUser.getEmail()) && loginUser.getPassword().equals(dbUser.getPassword())){
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      // Return a response with status 200 and JSON as type
      return Response.status(400).entity("Endpoint not implemented yet").build();

    }

  }

  // TODO: Make the system able to delete users : fix
  @DELETE
  @Path("/delete/")
  public Response deleteUser(String token) {
//selv tilføjet
    try {

      DecodedJWT jwt = null;

      try {
        jwt = JWT.decode(token);

      } catch (JWTDecodeException exception) {
        System.out.println(exception.getMessage());
      }

      User user = UserController.getUser(jwt.getClaim("userId").asInt());

        UserController.delete(user.getId());
        userCache.getUsers(true);

        return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User with id is now deleted").build();


// Selv tilføjet
    } catch (Exception e) {
      return Response.status(400).type(MediaType.APPLICATION_JSON_TYPE).entity("Could not delete user or does not exist").build();
    }
  }


  // TODO: Make the system able to update users
  public Response updateUser(String x) {

    // Return a response with status 200 and JSON as type
    return Response.status(400).entity("Endpoint not implemented yet").build();
  }
}
