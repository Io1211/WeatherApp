package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;

public class LocationSearchBuilder {
  private String locationName;
  private LocationAnswerDTO locationAnswerDTO = null;
  private CurrentAndForecastAnswerDTO currentAndForecastAnswerDTO = null;

  public String getLocationName() {
    return locationName;
  }

  public LocationSearchBuilder setLocationName(String locationName) {
    this.locationName = locationName;
    return this;
  }

  public LocationAnswerDTO getLocationAnswerDTO() {
    return locationAnswerDTO;
  }

  public LocationSearchBuilder setLocationAnswerDTO(LocationAnswerDTO locationAnswerDTO) {
    this.locationAnswerDTO = locationAnswerDTO;
    return this;
  }

  public CurrentAndForecastAnswerDTO getCurrentAndForecastAnswerDTO() {
    return currentAndForecastAnswerDTO;
  }

  public LocationSearchBuilder setCurrentAndForecastAnswerDTO(
      CurrentAndForecastAnswerDTO currentAndForecastAnswerDTO) {
    this.currentAndForecastAnswerDTO = currentAndForecastAnswerDTO;
    return this;
  }

  public LocationSearch build() {
    return new LocationSearch(locationName, locationAnswerDTO, currentAndForecastAnswerDTO);
  }
}
