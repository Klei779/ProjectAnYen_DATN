package vn.anyen.service;

import vn.anyen.dto.response.DiaChiRespone;

public interface GeocodingService {

    DiaChiRespone getLocation(String address);
}