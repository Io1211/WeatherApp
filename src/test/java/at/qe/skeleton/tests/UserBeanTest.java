package at.qe.skeleton.tests;

import at.qe.skeleton.internal.services.UserxService;
import at.qe.skeleton.internal.ui.beans.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Some very basic tests for UserBean.
 */
@SpringBootTest
public class UserBeanTest {

    @Mock
    private UserxService userService;

    @InjectMocks
    private UserBean userBean;

    //method creates mock instances for all fields annotated with @Mock and
    //injects mocks into the fields annotated with @InjectMocks
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetPassword() {
        String testPassword = "testPassword";
        userBean.setPassword(testPassword);
        assertEquals(testPassword, userBean.getUser().getPassword(), "Password should be set correctly");
    }

    @Test
    public void testSetUsername() {
        String testUsername = "testUsername";
        userBean.getUser().setUsername(testUsername);
        assertEquals(testUsername, userBean.getUser().getUsername(), "Username should be set correctly");
    }

    @Test
    public void testSetFirstName() {
        String testFirstName = "testFirstName";
        userBean.getUser().setFirstName(testFirstName);
        assertEquals(testFirstName, userBean.getUser().getFirstName(), "FirstName should be set correctly");
    }

    @Test
    public void testSetLastName() {
        String testLastName = "testLastName";
        userBean.getUser().setLastName(testLastName);
        assertEquals(testLastName, userBean.getUser().getLastName(), "LastName should be set correctly");
    }

    @Test
    public void testSetEmail() {
        String testEmail = "testEmail";
        userBean.getUser().setEmail(testEmail);
        assertEquals(testEmail, userBean.getUser().getEmail(), "Email should be set correctly");
    }

    @Test
    public void testSetPhone() {
        String testPhone = "testPhone";
        userBean.getUser().setPhone(testPhone);
        assertEquals(testPhone, userBean.getUser().getPhone(), "Phone should be set correctly");
    }

    @Test
    public void testRegister() {
        String firstName = "John";
        String lastName = "Doe";
        userBean.getUser().setFirstName(firstName);
        userBean.getUser().setLastName(lastName);

        String result = userBean.register();
        assertEquals("successPage", result, "Register should return 'successPage'");

        //TODO: check if userService.saveUser() was called
        //verify(userService, times(1)).saveUser()

    }
}
