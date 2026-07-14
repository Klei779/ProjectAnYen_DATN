package vn.anyen.service;

import vn.anyen.dto.request.TinTucRequest;
import vn.anyen.dto.response.TinTucResponse;

import java.util.List;

public interface TinTucService {

    List<TinTucResponse> getAll();

    TinTucResponse findById(Integer id);

    TinTucResponse create(TinTucRequest request);

    TinTucResponse update(Integer id, TinTucRequest request);

    void delete(Integer id);

}