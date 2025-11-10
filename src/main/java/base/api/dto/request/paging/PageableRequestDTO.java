package base.api.dto.request.paging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageableRequestDTO {

    private int pageNumber = 1;    // dễ hiểu hơn (1-based cho người dùng)
    private int pageSize = 10;
    private String keyword;
    private String sortBy = "createdAt";
    private Sort.Direction sortDirection = Sort.Direction.DESC;

    public Pageable toPageable() {
        validate();
        // Spring Data PageRequest mặc định 0-based, nên trừ 1
        return PageRequest.of(pageNumber - 1, pageSize, Sort.by(sortDirection, sortBy));
    }

    public void validate() {
        if (pageNumber < 1) this.pageNumber = 1;
        if (pageSize <= 0 || pageSize > 100) this.pageSize = 10;
        if (sortBy == null || sortBy.isBlank()) this.sortBy = "createdAt";
        if (sortDirection == null) this.sortDirection = Sort.Direction.DESC;
    }
}
