package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;

public class LocationSearch {
  private String locationName;
  private LocationAnswerDTO locationAnswerDTO;
  private CurrentAndForecastAnswerDTO currentAndForecastAnswerDTO;

  public LocationSearch(
      String locationName,
      LocationAnswerDTO locationAnswerDTO,
      CurrentAndForecastAnswerDTO currentAndForecastAnswerDTO) {
    this.locationName = locationName;
    this.locationAnswerDTO = locationAnswerDTO;
    this.currentAndForecastAnswerDTO = currentAndForecastAnswerDTO;
  }

  public String getLocationName() {
    return locationName;
  }

  public void setLocationName(String locationName) {
    this.locationName = locationName;
  }

  public LocationAnswerDTO getLocationAnswerDTO() {
    return locationAnswerDTO;
  }

  public void setLocationAnswerDTO(LocationAnswerDTO locationAnswerDTO) {
    this.locationAnswerDTO = locationAnswerDTO;
  }

  public CurrentAndForecastAnswerDTO getCurrentAndForecastAnswerDTO() {
    return currentAndForecastAnswerDTO;
  }

  public void setCurrentAndForecastAnswerDTO(
      CurrentAndForecastAnswerDTO currentAndForecastAnswerDTO) {
    this.currentAndForecastAnswerDTO = currentAndForecastAnswerDTO;
  }
}
