package at.qe.skeleton.internal.model;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import org.springframework.data.domain.Persistable;

/** Entity to persist a users favorite locations */
@Entity
public class Favorite implements Persistable<Long>, Serializable {

  @Id @GeneratedValue private Long id;

  /** The {@link Location} for this favorite. */
  @ManyToOne(optional = false)
  private Location location;

  /** Used for ordering list of favorites */
  private Integer priority; // Integer is used, because we need to be able to sort the list

  /** User who owns this favorite (bidirectional for setting the priority) */
  @ManyToOne private Userx user;

  public void setLocation(Location location) {
    this.location = location;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public Integer getPriority() {
    return priority;
  }

  public Location getLocation() {
    return location;
  }

  public Userx getUser() {
    return user;
  }

  public void setUser(Userx user) {
    this.user = user;
  }

  @PrePersist
  public void onCreate() {
    if (this.user.getFavorites().size() == 1) {
      this.priority = 0;
    } else {

      // set the newly created priority to the highest priority + 1
      this.priority =
          this.user.getFavorites().stream()
                  .filter(x -> Objects.nonNull(x.getPriority())) // filter out new priority
                  .mapToInt(Favorite::getPriority)
                  .max()
                  .orElse(0)
              + 1;
    }
  }

  @Override
  public Long getId() {
    return this.id;
  }

  @Override
  public boolean isNew() {
    return (null == id);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + Objects.hashCode(this.id);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Favorite other)) {
      return false;
    }
    return Objects.equals(this.id, other.id);
  }

  @Override
  public String toString() {
    return "at.qe.skeleton.model.Favorite[ id=" + id + " ]";
  }

  @Serial private static final long serialVersionUID = 1;
}
