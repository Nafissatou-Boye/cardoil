package cardoil.backend.service;

import cardoil.backend.dto.request.PersonnelRequest;
import cardoil.backend.dto.response.PersonnelResponse;

import java.util.List;

public interface AdminPersonnelService {
    List<PersonnelResponse> getAll(String login);
    PersonnelResponse create(String login, PersonnelRequest request);
    PersonnelResponse update(String login, Long id, PersonnelRequest request);
    void delete(String login, Long id);
    PersonnelResponse createAdminEntreprise(String login, Long entrepriseId, PersonnelRequest request);
    PersonnelResponse remplacerAdminEntreprise(String login, Long entrepriseId, PersonnelRequest request);

}