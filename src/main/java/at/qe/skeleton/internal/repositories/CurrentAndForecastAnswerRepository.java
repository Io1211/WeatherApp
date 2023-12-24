package at.qe.skeleton.internal.repositories;

import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;

/** Repository for managing {@link CurrentAndForecastAnswer} entities. */
public interface CurrentAndForecastAnswerRepository
    extends AbstractRepository<CurrentAndForecastAnswer, String> {
  CurrentAndForecastAnswer findById(Long id);
}
