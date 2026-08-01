package vn.tayjava.controller.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class PageResponseAbstract {
    public long pageNumber; // so trang
    public long pageSize; // số phần tử 1
    public long totalPages;
    public long totalElements; // tổng số phaafn tử
}
