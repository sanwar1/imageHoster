package ImageHoster.service;

import ImageHoster.model.User;
import ImageHoster.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  /*Calls the registerUser() method in the UserRepository class to persist the user record in DB*/
  public void registerUser(User newUser) {
    userRepository.registerUser(newUser);
  }

  /*This method receives the User type object Calls the checkUser() method in the Repository passing
  the username and password which checks the username and password in the database.
  The Repository returns User type object if user with entered username and password exists
  in the database else returns null*/
  public User login(User user) {
    User existingUser = userRepository.checkUser(user.getUsername(), user.getPassword());
    return existingUser;
  }
}
