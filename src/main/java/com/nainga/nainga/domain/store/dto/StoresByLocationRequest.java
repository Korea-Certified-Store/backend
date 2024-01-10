package com.nainga.nainga.domain.store.dto;

import com.nainga.nainga.domain.store.domain.Location;
import lombok.Data;

@Data
public class StoresByLocationRequest {
    Location northWestLocation;
    Location southEastLocation;
}
