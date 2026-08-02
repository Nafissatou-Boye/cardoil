package cardoil.backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AssignerStationsRequest {
    private List<Long> stationIds;
}