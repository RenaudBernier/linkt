package com.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("organizer")
public class Organizer extends User
{
    private boolean isApproved;
    private String organizationName;

    public Organizer() { super(); }

    public Organizer(String email, String firstName, String lastName, String phoneNumber, String password)
    {
        super(email, firstName, lastName, phoneNumber, password);
        this.isApproved = false;
    }

    public boolean isApproved()
    {
        return isApproved;
    }

    public void setApproved(boolean approved)
    {
        isApproved = approved;
    }

    public String getOrganizationName()
    {
        return organizationName;
    }

    public void setOrganizationName(String organizationName)
    {
        this.organizationName = organizationName;
    }
}
