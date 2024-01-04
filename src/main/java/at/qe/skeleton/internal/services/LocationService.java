package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.model.LocationId;
import at.qe.skeleton.internal.repositories.LocationRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("application")
public class LocationService {

  @Autowired private LocationRepository locationRepository;

  @Autowired private GeocodingApiRequestService geocodingApiRequestService;

  @Autowired private CurrentAndForecastAnswerService currentAndForecastAnswerService;

  public LocationAnswerDTO callApi(@NotNull String locationName) {
    return geocodingApiRequestService.retrieveLocationLonLat(locationName);
  }

  // When the location is saved it is assumed that there is no corresponding weather call for said
  // location yet.
  public Location saveLocation(@NotNull LocationAnswerDTO locationAnswerDTO)
      throws FailedToSerializeDTOException {
    Location location = new Location();
    location.setId(locationAnswerDTO.latitude(), locationAnswerDTO.longitude());
    location.setCity(locationAnswerDTO.name());
    location.setCountry(locationAnswerDTO.country());
    location.setState(locationAnswerDTO.state());
    location.setWeather(
        currentAndForecastAnswerService.saveWeather(
            currentAndForecastAnswerService.callApi(
                locationAnswerDTO.longitude(), locationAnswerDTO.latitude())));
    return locationRepository.save(location);
  }

  public Location getLocation(@NotNull String locationName)
      throws FailedJsonToDtoMappingException, FailedToSerializeDTOException {
    LocationAnswerDTO searchedLocationDTO = callApi(locationName);
    double searchedLatitude = searchedLocationDTO.latitude();
    double searchedLongitude = searchedLocationDTO.longitude();
    Location searchedLocation =
        locationRepository.findLocationById(new LocationId(searchedLatitude, searchedLongitude));
    if (searchedLocation == null) {
      return saveLocation(searchedLocationDTO);
    } else if (!locationHasUpToDateWeatherData(searchedLocation)) {
      searchedLocation.setWeather(
          currentAndForecastAnswerService.saveWeather(
              currentAndForecastAnswerService.callApi(searchedLongitude, searchedLatitude)));
      return searchedLocation;
    } else {
      return searchedLocation;
    }
  }

  public boolean locationHasUpToDateWeatherData(@NotNull Location location)
      throws FailedJsonToDtoMappingException {
    return currentAndForecastAnswerService
        .getLastHourCurrentAndForecastWeather()
        .contains(
            currentAndForecastAnswerService.deserializeDTO(location.getWeather().getWeatherData()));
  }
}
