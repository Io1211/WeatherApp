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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

@Component
@Scope("application")
public class LocationService {

  @Autowired private LocationRepository locationRepository;

  @Autowired private GeocodingApiRequestService geocodingApiRequestService;

  @Autowired private CurrentAndForecastAnswerService currentAndForecastAnswerService;

  @Autowired private CurrentAndForecastAnswerRepository currentAndForecastAnswerRepository;

  /**
   * calls the GeocodingApiRequestService. you can specifiy how many Locations you want to get back
   * at max by the api. the maximum of locations you can get back is 5. The minimum is 1. The method
   * gives back a List of LocationAnswerDTOs. The reason why we give back a List and not a single
   * object, is that it is needed for the autocompletion feature.
   *
   * @param locationName the name of the Location to be searched for
   * @param limit the limit of locations you want to retrieve from api
   * @return List of {@link LocationAnswerDTO}.
   */
  public List<LocationAnswerDTO> callApi(
      @NotNull String locationName, @NotNull @Min(1) @Max(5) int limit) {
    return geocodingApiRequestService.retrieveLocationsLonLat(locationName, limit);
  }

  /**
   * Searches for current Data with the given Location Name. If there is no db entry for the
   * Location Object with the corresponding Lon/Lat Id, a new one is being created. If there is an
   * existing Location in the db and it is not older than one hour, this Location will be returned.
   * If there is an older Location in the db, the Weather Api will be called and the Location in the
   * db will get updated with the new Weather and this Location will be returned. <br>
   * <br>
   *
   * @param locationSearchString the name of the location
   * @return a location with weatherdata not older than the last full hour.
   * @throws FailedToSerializeDTOException if there occur problems with the DTO serialization.
   */
  public Location handleLocationSearch(String locationSearchString)
      throws FailedToSerializeDTOException {
    // This method covers 3 cases:
    // 1. The searched location is already persisted and has up-to-date weather data.
    // 2. The searched location is already persisted but the weather data is out of date.
    // 3. The searched location does not exist in the database yet.
    Location location = new Location();
    LocationAnswerDTO locationAnswerDTO = callApi(locationSearchString, 1).get(0);
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
