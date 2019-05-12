public class AuthMessage extends AbstractMessage{

    String login;
    String password;
    Boolean isAuthPassed;

    public void setAuthPassed(Boolean authPassed) {
        isAuthPassed = authPassed;
    }

    public AuthMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
