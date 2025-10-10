package com.model;

import jakarta.persistence.*;
import com.model.Student;
import com.model.Organizer;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public abstract class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String phoneNumber;

    @Column(nullable = false)
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
    
    public String getPassword()
    {
        return password;
    }

    public String getUserType()
    {
        if (this instanceof Student) {
            return "student";
        } else if (this instanceof Organizer) {
            return "organizer";
        }
        return null;
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
    public void setPassword(String password)
    {
        this.password = password;
    }

}
