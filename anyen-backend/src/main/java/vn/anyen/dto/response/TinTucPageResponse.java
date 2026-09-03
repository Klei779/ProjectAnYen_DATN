package vn.anyen.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TinTucPageResponse {

    private List<TinTucResponse> items;

    private long total;

    /**
     * Trang hiện tại, bắt đầu từ 1.
     */
    private int page;

    private int pageSize;

    private int totalPages;
}