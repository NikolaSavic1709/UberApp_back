package com.uberTim12.ihor.model.users;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.Set;

import static jakarta.persistence.InheritanceType.JOINED;
import static jakarta.persistence.InheritanceType.TABLE_PER_CLASS;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Inheritance(strategy=JOINED)
@Table(name = "ihor")
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Lob
    @Column(name = "profile_picture")
    private byte[] profilePicture;

    @Column(name = "telephone_number")
    private String telephoneNumber;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "password", nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "authority_id")
    private Authority authority;

    @OneToMany(mappedBy="user", fetch = FetchType.EAGER)
    private Set<Note> notes;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked;

    @Column(name="is_active")
    private boolean isActive;


    protected User(String name, String surname, byte[] profilePicture, String telephoneNumber, String email, String address, String password) {
        super();
        this.setName(name);
        this.setSurname(surname);
        this.setProfilePicture(profilePicture);
        this.setTelephoneNumber(telephoneNumber);
        this.setEmail(email);
        this.setAddress(address);
        this.setPassword(password);
        this.setBlocked(false);
        this.setActive(true);
    }

    public User(String name, String surname, byte[] profilePicture, String telephoneNumber, String email, String address, String password, Authority authority, Set<Note> notes, boolean isBlocked, boolean isActive) {
        this.name = name;
        this.surname = surname;
        this.profilePicture = profilePicture;
        this.telephoneNumber = telephoneNumber;
        this.email = email;
        this.address = address;
        this.password = password;
        this.authority = authority;
        this.notes = notes;
        this.isBlocked = isBlocked;
        this.isActive = isActive;
    }
}
