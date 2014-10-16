package com.diosoft.calendar.server.adapter;

import com.diosoft.calendar.server.common.Person;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "attender")
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonAdapter implements Comparable<PersonAdapter>, Serializable {

    private String name;
    private String lastName;
    private String email;

    public String getLastName() {
        return lastName;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }


    public PersonAdapter(){};

    public PersonAdapter(Person person) {
        this.name = person.getName();
        this.lastName = person.getLastName();
        this.email = person.getEmail();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Person)) return false;
        if (this == obj) return true;

        PersonAdapter person = (PersonAdapter) obj;

        if (email != null ? !email.equals(person.email) : person.email != null) return false;
        if (lastName != null ? !lastName.equals(person.lastName) : person.lastName != null) return false;
        if (name != null ? !name.equals(person.name) : person.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Person { ");
        sb.append(name).append(", ")
                .append(lastName).append(", ")
                .append(email).append(" } \n");

        return sb.toString();
    }

    @Override
    public int compareTo(PersonAdapter obj) {
        if (obj == null) return 1;
        PersonAdapter person = (PersonAdapter) obj;
        int result = name.compareTo(person.name);
        if (result != 0) return (int) (result / Math.abs(result));
        result = lastName.compareTo(person.lastName);
        if (result != 0) return (int) (result / Math.abs(result));
        result = email.compareTo(person.email);
        return (result != 0) ? (int) (result / Math.abs(result)) : 0;
    }
}