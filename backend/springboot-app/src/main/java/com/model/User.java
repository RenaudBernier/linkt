@Entity
@Table(name = "users")
public abstract class User 
{
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String password;


    public User() {}

    public User(String email, String firstName, String lastName, String phoneNumber, String password) 
    {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.password = password;

    }   

    public Long getUserId() 
    {
        return userId;
    } 

    public String getEmail() 
    {
        return email;
    }

    public String getFirstName() 
    {
        return firstName;
    }

    public String getLastName() 
    {
        return lastName;
    }

    public String getPhoneNumber() 
    {
        return phoneNumber;
    }

    public void setEmail(String email) 
    {
        this.email = email;
    }
    
    public void setFirstName(String firstName) 
    {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) 
    {
        this.lastName = lastName;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

}
