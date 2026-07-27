// NotificationPersonnelleService.java
package cardoil.backend.service;

import cardoil.backend.dto.response.NotificationPersonnelleResponse;

import java.util.List;

public interface NotificationPersonnelleService {
    List<NotificationPersonnelleResponse> mesNotifications(String login);
    long compteNonLues(String login);
    void marquerLue(String login, Long id);
    void marquerToutLu(String login);
    void supprimer(String login, Long id);
}