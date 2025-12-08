package com.linkt.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import com.linkt.model.Student;
import com.linkt.model.Organizer;

@Entity
@Table(name = "user")
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
    @JsonIgnore
    private String password;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_code_expiry")
    private java.time.LocalDateTime verificationCodeExpiry;

    @Column(name = "two_factor_code")
    private String twoFactorCode;

    @Column(name = "two_factor_code_expiry")
    private java.time.LocalDateTime twoFactorCodeExpiry;


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

    public void setUserId(Long userId)
    {
        this.userId = userId;
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
        } else if (this instanceof Administrator) {
            return "administrator";
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

    public boolean isEmailVerified() {
        return emailVerified != null && emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public java.time.LocalDateTime getVerificationCodeExpiry() {
        return verificationCodeExpiry;
    }

    public void setVerificationCodeExpiry(java.time.LocalDateTime verificationCodeExpiry) {
        this.verificationCodeExpiry = verificationCodeExpiry;
    }

    public String getTwoFactorCode() {
        return twoFactorCode;
    }

    public void setTwoFactorCode(String twoFactorCode) {
        this.twoFactorCode = twoFactorCode;
    }

    public java.time.LocalDateTime getTwoFactorCodeExpiry() {
        return twoFactorCodeExpiry;
    }

    public void setTwoFactorCodeExpiry(java.time.LocalDateTime twoFactorCodeExpiry) {
        this.twoFactorCodeExpiry = twoFactorCodeExpiry;
    }

}
