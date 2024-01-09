package com.nainga.nainga.domain.store.dto;

import com.nainga.nainga.domain.store.domain.Location;
import lombok.Data;

@Data
public class StoreByLocationRequest {
    Location northWestLocation;
    Location southEastLocation;
}
