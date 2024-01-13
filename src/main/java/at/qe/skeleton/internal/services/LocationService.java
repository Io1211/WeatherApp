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
import at.qe.skeleton.internal.services.exceptions.GeocodingApiReturnedEmptyListException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Scope("application")
public class LocationService {

  @Autowired private LocationRepository locationRepository;

  @Autowired private GeocodingApiRequestService geocodingApiRequestService;

  @Autowired private CurrentAndForecastAnswerService currentAndForecastAnswerService;

  @Autowired private CurrentAndForecastAnswerRepository currentAndForecastAnswerRepository;

  private static final Logger LOGGER = LoggerFactory.getLogger(LocationService.class);

  /**
   * Calls the GeocodingApiRequestService. you can specifiy how many Locations you want to get back
   * at max by the api. the maximum of locations you can get back is 5. The minimum is 1. The method
   * gives back a List of LocationAnswerDTOs. The reason why we give back a List and not a single
   * object, is that it is needed for the autocompletion feature.
   *
   * @param locationName the name of the Location to be searched for
   * @param limit the limit of locations you want to retrieve from api
   * @return List of {@link LocationAnswerDTO}.
   */
  public List<LocationAnswerDTO> callApi(@NotNull String locationName, @Min(1) @Max(5) int limit)
      throws FailedApiRequest, GeocodingApiReturnedEmptyListException {
    try {
      return geocodingApiRequestService.retrieveLocationsLonLat(locationName, limit);
    } catch (GeocodingApiReturnedEmptyListException e) {
      LOGGER.info(e.getMessage());
      throw new GeocodingApiReturnedEmptyListException(e.getMessage());
    } catch (final Exception e) {
      String errorMessage =
          "An error occurred in the Geocoding api call with %s as the searched location"
              .formatted(locationName);
      LOGGER.error(errorMessage, e);
      throw new FailedApiRequest(errorMessage);
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
   * @return a location with weather-data not older than the last full hour.
   */
  public Location handleLocationSearch(String locationSearchString) throws FailedApiRequest {
    // This method covers 3 cases:
    // 1. The searched location is already persisted and has up-to-date weather data.
    // 2. The searched location is already persisted but the weather data is out of date.
    // 3. The searched location does not exist in the database yet.
    LocationAnswerDTO locationAnswerDTO = callApi(locationSearchString, 1).get(0);
    // Case 1:
    if (locationAlreadyPersisted(locationAnswerDTO)) {
      Location location = getLocation(locationAnswerDTO);
      if (locationHasUpToDateWeatherData(location)) {
        return location;
      }
      // Case 2:
      else {
        // get the old weather data from the location
        CurrentAndForecastAnswer oldWeather =
            currentAndForecastAnswerRepository.findById(location.getWeather().getId()).orElse(null);
        Assert.notNull(
            oldWeather,
            "The location was already persisted, yet has no weather data associated to it");

        // get the new weather data, save it to the database and connect it to the respective
        // location.
        CurrentAndForecastAnswerDTO newWeatherDTO =
            currentAndForecastAnswerService.callApi(
                locationAnswerDTO.longitude(), locationAnswerDTO.latitude());
        CurrentAndForecastAnswer newWeather =
            currentAndForecastAnswerService.saveWeather(newWeatherDTO);
        location.setWeather(newWeather);

        // now that the new weather data has replaced the old one, the old can be safely deleted.
        // Deleting it before would throw an error, because the old weather data would still be a
        // field of the location and thus can't be deleted for data integrity reasons
        currentAndForecastAnswerRepository.delete(oldWeather);

        // save the updated location and return it
        return locationRepository.save(location);
      }
    }
    // Case 3:
    Location location = new Location();
    location.setId(locationAnswerDTO.latitude(), locationAnswerDTO.longitude());
    location.setCity(locationAnswerDTO.name());
    location.setState(locationAnswerDTO.state());
    location.setCountry(locationAnswerDTO.country());
    CurrentAndForecastAnswerDTO newWeatherDTO =
        currentAndForecastAnswerService.callApi(
            locationAnswerDTO.longitude(), locationAnswerDTO.latitude());
    CurrentAndForecastAnswer newWeather =
        currentAndForecastAnswerService.saveWeather(newWeatherDTO);
    location.setWeather(newWeather);

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
