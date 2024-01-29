package at.qe.skeleton.internal.model;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

/**
 * Entity representing users.
 *
 * <p>This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
@Entity
public class Userx implements Persistable<String>, Serializable, Comparable<Userx> {

  @Serial private static final long serialVersionUID = 1L;

  /** The username is used as the primary id for this entity. */
  @Id
  @Column(length = 100)
  private String username;

  /** The {@link Userx} who created this user. */
  @ManyToOne(optional = true)
  private Userx createUser;

  /** The date and time when this user was created. */
  @Column(nullable = false)
  @CreationTimestamp
  private LocalDateTime createDate;

  /** The {@link Userx} who last updated this user. */
  @ManyToOne(optional = true)
  private Userx updateUser;

  /** The date and time when this user was last updated. */
  @UpdateTimestamp private LocalDateTime updateDate;

  /**
   * The list of {@link Favorite} entities for this user. FetchType.EAGER is used because Lazy
   * loading complicates testing makes testing more error-prone
   */
  @OneToMany(
      cascade = CascadeType.ALL,
      mappedBy = "user",
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<Favorite> favorites;

  /**
   * The {@link FavoriteDataConfig} entity for this user. It's the configuration of the view of the
   * favorites.
   */
  @OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
  private FavoriteDataConfig favoriteDataConfig;

  private String password;

  private String firstName;
  private String lastName;
  private String email;
  private String phone;

  /**
   * The {@link Subscription} entity for this user. If the user is a premium user, this entity is
   * not null.
   */
  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private Subscription subscription;

  /** The enabled status of this user. If the user is disabled, he can't log in. */
  boolean enabled;

  /**
   * The {@link CreditCard} entity for this user. Is not allowed to be null, if the user is a
   * premium user.
   */
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "credit_card_id", referencedColumnName = "id")
  private CreditCard creditCard;

  /**
   * The {@link UserxRole} entities for this user. FetchType.EAGER is used because Lazy loading
   * complicates testing makes testing more error-prone.
   */
  @ElementCollection(targetClass = UserxRole.class, fetch = FetchType.EAGER)
  @CollectionTable(name = "Userx_UserxRole")
  @Enumerated(EnumType.STRING)
  private Set<UserxRole> roles;

  /** The default constructor for the entity. The favorite data config is initialized. */
  @PrePersist
  public void onCreate() {
    this.setFavoriteDataConfig(new FavoriteDataConfig());
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Set<UserxRole> getRoles() {
    return roles;
  }

  public void setRoles(Set<UserxRole> roles) {
    this.roles = roles;
  }

  public void addRole(UserxRole role) {
    this.roles.add(role);
  }

  public void removeRole(UserxRole role) {
    this.roles.remove(role);
  }

  public List<Favorite> getFavorites() {
    return favorites;
  }

  public void setFavorites(List<Favorite> favorites) {
    this.favorites = favorites;
  }

  public FavoriteDataConfig getFavoriteDataConfig() {
    return favoriteDataConfig;
  }

  public void setFavoriteDataConfig(FavoriteDataConfig favoriteDataConfig) {
    this.favoriteDataConfig = favoriteDataConfig;
  }

  public Userx getCreateUser() {
    return createUser;
  }

  public void setCreateUser(Userx createUser) {
    this.createUser = createUser;
  }

  public LocalDateTime getCreateDate() {
    return createDate;
  }

  public void setCreateDate(LocalDateTime createDate) {
    this.createDate = createDate;
  }

  public Userx getUpdateUser() {
    return updateUser;
  }

  public void setUpdateUser(Userx updateUser) {
    this.updateUser = updateUser;
  }

  public LocalDateTime getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(LocalDateTime updateDate) {
    this.updateDate = updateDate;
  }

  public CreditCard getCreditCard() {
    return creditCard;
  }

  public void setCreditCard(CreditCard creditCard) {
    this.creditCard = creditCard;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + Objects.hashCode(this.username);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Userx)) {
      return false;
    }
    final Userx other = (Userx) obj;
    return Objects.equals(this.username, other.username);
  }

  @Override
  public String toString() {
    return "at.qe.skeleton.model.User[ id=" + username + " ]";
  }

  @Override
  public String getId() {
    return getUsername();
  }

  public void setId(String id) {
    setUsername(id);
  }

  @Override
  public boolean isNew() {
    return (null == createDate);
  }

  @Override
  public int compareTo(Userx o) {
    return this.username.compareTo(o.getUsername());
  }

  public boolean isPremium() {
    return getRoles().contains(UserxRole.PREMIUM_USER);
  }

  public Subscription getSubscription() {
    return subscription;
  }

  public void setSubscription(Subscription subscription) {
    this.subscription = subscription;
  }
}
