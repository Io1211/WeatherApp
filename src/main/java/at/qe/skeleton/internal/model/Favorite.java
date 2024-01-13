package at.qe.skeleton.internal.model;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import org.springframework.data.domain.Persistable;

/** Entity to persist a users favorite locations */
@Entity
public class Favorite implements Persistable<String>, Serializable {

  @Id @GeneratedValue private Long id;

  @ManyToOne(optional = false)
  private Location location;

  /** Used for ordering list of favorites */
  private Integer priority;

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
  public String getId() {
    return String.valueOf(this.id);
  }

  @Override
  public boolean isNew() {
    return (null == id);
  }

  @Serial private static final long serialVersionUID = 1;
}
