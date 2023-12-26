package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.services.UserxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("session")
public class UserBean {

  private Userx user = new Userx();

  @Autowired private UserxService userService;

  public Userx getUser() {
    return user;
  }

  public void setPassword(String password) {
    this.user.setPassword(password);
  }

  // To do: add validation of data and error messages and save user in database->update database
  public String register() {
    System.out.println("Registration from " + user.getFirstName() + " " + user.getLastName());
    // user.setRoles(Set.of(new UserxRole[] {UserxRole.EMPLOYEE}));
    // user.setEnabled(true);
    // userService.saveUser(user);
    return "successPage";
  }
}
