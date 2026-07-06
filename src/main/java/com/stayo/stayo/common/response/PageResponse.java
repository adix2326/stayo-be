package com.stayo.stayo.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content; //(Stores the items list, e.g. PropertyResponse)
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private long totalPages;
    private boolean last;
}
