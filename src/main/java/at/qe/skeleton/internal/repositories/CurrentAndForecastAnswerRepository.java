package at.qe.skeleton.internal.repositories;

import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository for managing {@link CurrentAndForecastAnswer} entities. */
public interface CurrentAndForecastAnswerRepository
    extends AbstractRepository<CurrentAndForecastAnswer, String> {
  CurrentAndForecastAnswer findById(Long id);

  @Query("SELECT w FROM CurrentAndForecastAnswer w WHERE w.timestampLastCall >= :queryDateTime")
  List<CurrentAndForecastAnswer> findByTimestampLastCallIsAfter(
      @Param("queryDateTime") ZonedDateTime queryDateTime);
}
