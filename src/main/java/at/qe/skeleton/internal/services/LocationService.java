package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.model.LocationId;
import at.qe.skeleton.internal.repositories.CurrentAndForecastAnswerRepository;
import at.qe.skeleton.internal.repositories.LocationRepository;
import at.qe.skeleton.internal.services.utils.FailedToSerializeDTOException;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("application")
public class LocationService {

  @Autowired private LocationRepository locationRepository;

  @Autowired private GeocodingApiRequestService geocodingApiRequestService;

  @Autowired private CurrentAndForecastAnswerService currentAndForecastAnswerService;

  @Autowired private CurrentAndForecastAnswerRepository currentAndForecastAnswerRepository;

  public LocationAnswerDTO callApi(@NotNull String locationName) {
    return geocodingApiRequestService.retrieveLocationLonLat(locationName);
  }

  public Location handleLocationSearch(String locationSearchString)
      throws FailedToSerializeDTOException {
    Location location = new Location();
    LocationAnswerDTO locationAnswerDTO = callApi(locationSearchString);
    CurrentAndForecastAnswerDTO weatherDTO;
    if (locationAlreadyPersisted(locationAnswerDTO)) {
      location = getLocation(locationAnswerDTO);
      if (locationHasUpToDateWeatherData(location)) {
        return location;
      }
    }
    // problematic if we store same id twice? will it just be updated?
    location.setId(locationAnswerDTO.latitude(), locationAnswerDTO.longitude());
    location.setCity(locationAnswerDTO.name());
    location.setState(locationAnswerDTO.state());
    location.setCountry(locationAnswerDTO.country());
    weatherDTO =
        currentAndForecastAnswerService.callApi(
            locationAnswerDTO.longitude(), locationAnswerDTO.latitude());
    location.setWeather(currentAndForecastAnswerService.saveWeather(weatherDTO));

    return locationRepository.save(location);
  }

  public Location updateLocationWeather(
      Location locationToUpdate, CurrentAndForecastAnswer updatedWeather) {
    locationToUpdate.setWeather(currentAndForecastAnswerRepository.save(updatedWeather));
    return locationRepository.save(locationToUpdate);
  }

  public boolean locationAlreadyPersisted(@NotNull LocationAnswerDTO searchedLocationDTO) {
    return locationRepository.findLocationById(
            new LocationId(searchedLocationDTO.latitude(), searchedLocationDTO.longitude()))
        != null;
  }

  public boolean locationHasUpToDateWeatherData(@NotNull Location location) {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime lastFullHour =
        now.minusMinutes(now.getMinute()).minusSeconds(now.getSecond()).minusNanos(now.getNano());
    return location.getWeather().getTimestampLastCall().isAfter(lastFullHour);
  }

  public Location getLocation(@NotNull LocationAnswerDTO searchedLocation) {
    return locationRepository.findLocationById(
        new LocationId(searchedLocation.latitude(), searchedLocation.longitude()));
  }
}
