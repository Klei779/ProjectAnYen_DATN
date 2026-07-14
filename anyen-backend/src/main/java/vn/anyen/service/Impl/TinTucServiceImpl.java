package vn.anyen.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import vn.anyen.dto.request.TinTucRequest;
import vn.anyen.dto.response.TinTucResponse;
import vn.anyen.entity.TinTuc;
import vn.anyen.repository.TinTucRepository;
import vn.anyen.service.TinTucService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TinTucServiceImpl implements TinTucService {

    private final TinTucRepository tinTucRepository;

    @Override
    public List<TinTucResponse> getAll() {
        return tinTucRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TinTucResponse findById(Integer id) {

        TinTuc tinTuc = tinTucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức"));

        return convertToResponse(tinTuc);
    }

    @Override
    public TinTucResponse create(TinTucRequest request) {

        TinTuc tinTuc = new TinTuc();

        BeanUtils.copyProperties(request, tinTuc);

        TinTuc saved = tinTucRepository.save(tinTuc);

        return convertToResponse(saved);
    }

    @Override
    public TinTucResponse update(Integer id, TinTucRequest request) {

        TinTuc tinTuc = tinTucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức"));

        BeanUtils.copyProperties(request, tinTuc);

        TinTuc updated = tinTucRepository.save(tinTuc);

        return convertToResponse(updated);
    }

    @Override
    public void delete(Integer id) {

        TinTuc tinTuc = tinTucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tức"));

        tinTucRepository.delete(tinTuc);
    }

    /**
     * Convert Entity -> Response
     */
    private TinTucResponse convertToResponse(TinTuc tinTuc) {

        TinTucResponse response = new TinTucResponse();

        BeanUtils.copyProperties(tinTuc, response);

        return response;
    }

}