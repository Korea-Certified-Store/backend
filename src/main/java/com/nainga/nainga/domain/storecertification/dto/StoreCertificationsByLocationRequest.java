package com.nainga.nainga.domain.storecertification.dto;

import com.nainga.nainga.domain.store.domain.Location;
import lombok.Data;

@Data
public class StoreCertificationsByLocationRequest {
    Location northWestLocation;
    Location southEastLocation;
}
