package at.qe.skeleton.internal.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.io.Serial;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;

/** Entity to persist a users favorite locations */
@Entity
public class Favorite implements Persistable<String>, Serializable {

  @Id @GeneratedValue private Long id;

  @ManyToOne(optional = false)
  private Location location;

  /** Used for ordering list of favorites */
  private Integer priority;

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
