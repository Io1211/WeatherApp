package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.model.UserxRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Scope("application")
public class RegistrationService {

    @Autowired
    private UserxService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TokenService tokenService;

    public void sendRegistrationEmail(String email, String token) {
        emailService.sendEmail(email, "Confirm your registration", "Please click on the following link to confirm your registration: http://localhost:8080/confirmRegistration.xhtml" + "\nYour token: " + token + "\n\nIf you did not register, please ignore this email.");
    }

    public void registerUser(Userx user, String token) {
        if (userService.loadUser(user.getUsername()) != null) {
            throw new RuntimeException("Username already exists.");
        }
        user.setRoles(Set.of(UserxRole.REGISTERED_USER));
        user.setEnabled(false);
        sendRegistrationEmail(user.getEmail(), token);
        userService.saveUser(user);
    }

    public void confirmRegistrationOfUser(String username, String token, String insertedToken) {
        Userx user = userService.loadUser(username);
        if (tokenService.validateToken(insertedToken, token)) {
            user.setEnabled(true);
            userService.saveUser(user);
        }
    }


}
