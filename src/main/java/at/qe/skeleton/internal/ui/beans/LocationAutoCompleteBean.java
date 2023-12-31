package at.qe.skeleton.internal.ui.beans;

import at.qe.skeleton.external.model.location.LocationAnswerDTO;
import at.qe.skeleton.external.services.GeocodingApiRequestService;
import at.qe.skeleton.external.services.WeatherApiRequestService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/*
* Handles the Location Auto Completion with a call to Geocoding Api.
* here is an exemplary by Primefaces docs: http://www.primefaces.org:8080/showcase/ui/input/autoComplete.xhtml?jfwid=9a08e
*
*
 */
@Component
@Scope("view")
public class LocationAutoCompleteBean {
    GeocodingApiRequestService geocodingApiRequestService;
    WeatherApiRequestService weatherApiRequestService;
    public LocationAutoCompleteBean(GeocodingApiRequestService geocodingApiRequestService,
                                    WeatherApiRequestService weatherApiRequestService){
        this.geocodingApiRequestService = geocodingApiRequestService;
        this.weatherApiRequestService = weatherApiRequestService;
    };
        public List<String> autoCompleteLocation(String query){
        String queryLowerCase = query.toLowerCase();
        List<String> locationList = new ArrayList<>();
        List<LocationAnswerDTO> locationAnswerDTOList= geocodingApiRequestService.retrieveLocationsLonLat(query);
        //todo: fertig schreiben
        return null;
        }
}
