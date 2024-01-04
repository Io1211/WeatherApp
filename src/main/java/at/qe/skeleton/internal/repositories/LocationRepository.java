package at.qe.skeleton.internal.repositories;

import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.model.LocationId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationRepository extends AbstractRepository<Location, LocationId> {
  // Custom query necessary due to composite primary key (see
  // https://www.baeldung.com/jpa-composite-primary-keys)
  @Query("SELECT location FROM Location location WHERE location.locationId = :queryId")
  Location findLocationById(@Param("queryId") LocationId queryId);
}
