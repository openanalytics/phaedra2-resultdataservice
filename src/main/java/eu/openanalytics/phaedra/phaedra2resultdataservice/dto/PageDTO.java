package eu.openanalytics.phaedra.phaedra2resultdataservice.dto;

import lombok.Value;
import org.springframework.data.domain.Page;

import java.util.List;

@Value
public class PageDTO<T> {

    List<T> data;

    StatusDTO status;

    @Value
    public static class StatusDTO {
        int totalPages;
        long totalElements;
        boolean first;
        boolean last;
    }

    public static <R> PageDTO<R> map(Page<R> page) {

        return new PageDTO<>(
            page.getContent(),
            new StatusDTO(
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast()
            )
        );

    }

}
