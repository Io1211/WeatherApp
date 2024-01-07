package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.model.UserxRole;
import at.qe.skeleton.internal.services.EmailService;
import at.qe.skeleton.internal.services.RegistrationService;
import at.qe.skeleton.internal.services.TokenService;
import at.qe.skeleton.internal.services.UserxService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Scope("session")
public class UserRegistrationBean {

    private Userx user = new Userx();

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    TokenService tokenService;

    private String token;

    private String insertedToken;

    public Userx getUser() {
        return user;
    }

    public void setUser(Userx user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.user.setPassword(password);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getInsertedToken() {
        return insertedToken;
    }

    public void setInsertedToken(String insertedToken) {
        this.insertedToken = insertedToken;
    }

    private void addMessage(String summary, FacesMessage.Severity severity) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, summary, null));
    }
    public String register() {
        try {
            String token = tokenService.generateToken();
            setToken(token);
            registrationService.registerUser(user, token);
            return "confirmRegistration";
        }
        catch (RuntimeException e) {
            addMessage(e.getMessage(), FacesMessage.SEVERITY_ERROR);
            return null;
        }
    }

    public String confirmRegistration() {
        registrationService.confirmRegistrationOfUser(user.getUsername(), token, insertedToken);
        return "login";
    }
}
