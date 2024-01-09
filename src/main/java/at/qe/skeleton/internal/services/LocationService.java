package at.qe.skeleton.internal.services;

import at.qe.skeleton.external.model.currentandforecast.CurrentAndForecastAnswerDTO;
import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import at.qe.skeleton.internal.model.CurrentAndForecastAnswer;
import at.qe.skeleton.internal.model.Location;
import at.qe.skeleton.internal.model.LocationId;
import at.qe.skeleton.internal.repositories.CurrentAndForecastAnswerRepository;
import at.qe.skeleton.internal.repositories.LocationRepository;
import at.qe.skeleton.internal.services.exceptions.FailedApiRequest;
import at.qe.skeleton.internal.services.exceptions.FailedToSerializeDTOException;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(LocationService.class);

  public LocationAnswerDTO callApi(@NotNull String locationName) throws FailedApiRequest {
    try {
      return geocodingApiRequestService.retrieveLocationLonLat(locationName);
    } catch (final Exception e) {
      LOGGER.error("error in request", e);
      throw new FailedApiRequest(
          "An error occurred in the Geocoding api call with %s as the searched location"
              .formatted(locationName));
    }
  }

  /**
   * Searches for the location to the provided location-name in the database. If there is no
   * corresponding entry, meaning a location with the longitude and latitude (id) of the searched
   * location-name, a new one is created with up-to-date weather data. If one exists, its weather
   * data is checked. If it is up-to-date i.e., saved more recently than the last full hour, the
   * previously persisted location (containing the weather data) is returned. Else, new weather data
   * is fetched from the weather api and saved to the persisted location, which is then returned.
   * <br>
   *
   * @param locationSearchString the name of the location
   * @return a location with weatherdata not older than the last full hour.
   * @throws FailedToSerializeDTOException if there occur problems with the DTO serialization.
   */
  public Location handleLocationSearch(String locationSearchString)
      throws FailedToSerializeDTOException, FailedApiRequest {
    // This method covers 3 cases:
    // 1. The searched location is already persisted and has up-to-date weather data.
    // 2. The searched location is already persisted but the weather data is out of date.
    // 3. The searched location does not exist in the database yet.
    Location location = new Location();
    LocationAnswerDTO locationAnswerDTO = callApi(locationSearchString);
    CurrentAndForecastAnswerDTO weatherDTO;
    // Case 1:
    if (locationAlreadyPersisted(locationAnswerDTO)) {
      location = getLocation(locationAnswerDTO);
      if (locationHasUpToDateWeatherData(location)) {
        return location;
      }
    }
    // Cases 2 & 3:
    // they can be handled the same, as overwriting the id, city, state etc. with the very
    // same data doesn't make a difference in the data per se and simplifies this method slightly by
    // combining the logic of the two cases.
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
