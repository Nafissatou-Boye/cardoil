package cardoil.backend.service;

import cardoil.backend.dto.request.AssignerStationsRequest;
import cardoil.backend.dto.request.ChangerStatutServiceRequest;
import cardoil.backend.dto.request.ServiceCatalogueRequest;
import cardoil.backend.dto.response.ServiceCatalogueResponse;

import java.util.List;

public interface AdminCompagnieServiceCatalogueService {
    List<ServiceCatalogueResponse> getServices(String login);
    ServiceCatalogueResponse getService(String login, Long serviceId);
    ServiceCatalogueResponse creerService(String login, ServiceCatalogueRequest request);
    ServiceCatalogueResponse modifierService(String login, Long serviceId, ServiceCatalogueRequest request);
    ServiceCatalogueResponse changerStatut(String login, Long serviceId, ChangerStatutServiceRequest request);
    ServiceCatalogueResponse assignerStations(String login, Long serviceId, AssignerStationsRequest request);
}