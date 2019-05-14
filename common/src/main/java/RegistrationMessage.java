public class RegistrationMessage extends AbstractMessage {

    private String login;
    private String password;
    private boolean isRegistrationPassed;

    public boolean isRegistrationPassed() {
        return isRegistrationPassed;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setRegistrationPassed(boolean registrationPassed) {
        isRegistrationPassed = registrationPassed;
    }

    public RegistrationMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
