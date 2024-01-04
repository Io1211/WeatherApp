package at.qe.skeleton.internal.services;

import at.qe.skeleton.internal.model.Userx;
import at.qe.skeleton.internal.repositories.UserxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Service for accessing and manipulating user data.
 * <p>
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 */
@Component
@Scope("application")
public class UserxService {

    @Autowired
    private UserxRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Returns a collection of all users.
     *
     * @return
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Collection<Userx> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Loads a single user identified by its username.
     *
     * @param username the username to search for
     * @return the user with the given username
     */
    public Userx loadUser(String username) {

        return userRepository.findFirstByUsername(username);
    }

    public Userx loadUserByEmail(String email) {
        return userRepository.findFirstByEmail(email);
    }
    /**
     * Saves the user. This method will also set {@link Userx#createDate} for new
     * entities or {@link Userx#updateDate} for updated entities. The user
     * requesting this operation will also be stored as {@link Userx#createDate}
     * or {@link Userx#updateUser} respectively.
     *
     * @param user the user to save
     * @return the updated user
     */
    public Userx saveUser(Userx user) throws JpaSystemException {
        if (user.isNew()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && !auth.getName().equals("anonymousUser")) {
                Userx authenticatedUser = userRepository.findFirstByUsername(auth.getName());
                user.setCreateUser(authenticatedUser);
            } else {
                // Handle the scenario when there's no authenticated user (e.g., default admin or null)
                // user.setCreateUser(defaultAdminUser); // Example: Set a default admin user
                user.setCreateUser(null); // Or allow null if the schema permits
            }

            String password = user.getPassword();
            if (password == null) {
                throw new JpaSystemException(new RuntimeException("Password can't be empty"));
            }
            user.setPassword(passwordEncoder.encode(password));
        } else {
            user.setUpdateUser(getAuthenticatedUser());
        }
        return userRepository.save(user);
    }


    /**
     * Deletes the user.
     *
     * @param user the user to delete
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteUser(Userx user) {
        userRepository.delete(user);
        // :TODO: write some audit log stating who and when this user was permanently deleted.
    }

    private Userx getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findFirstByUsername(auth.getName());
    }
}
