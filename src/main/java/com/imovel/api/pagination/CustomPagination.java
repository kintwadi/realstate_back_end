package com.imovel.api.pagination;

import com.imovel.api.model.Property;
import com.imovel.api.repository.PropertyRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public class CustomPagination {

    private final PropertyRepository propertyRepository;

    public CustomPagination(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Transactional(readOnly = true)
    public PaginationResult<Property> getProperties(Pagination pagination) {
        // Get total count
        long totalRecords = propertyRepository.countAllProperties();

        // Calculate pagination metadata
        int lastPageNumber = calculateLastPageNumber(totalRecords, pagination.getPageSize());
        int currentPageNumber = adjustCurrentPageNumber(pagination.getPageNumber(), lastPageNumber);

        // Get paginated results
        int offset = (currentPageNumber - 1) * pagination.getPageSize();
        List<Property> properties = propertyRepository.findAllPropertiesWithPagination(
                offset, 
                pagination.getPageSize()
        );

        return buildPaginationResult(
                currentPageNumber,
                lastPageNumber,
                pagination.getPageSize(),
                totalRecords,
                properties
        );
    }

    @Transactional(readOnly = true)
    public PaginationResult<Property> getPropertiesWithFilter(Pagination pagination, Property filter) {
        // Get filtered count
        long totalRecords = propertyRepository.countPropertiesWithFilter(
                filter.getType(),
                filter.getCategory(),
                filter.getStatus(),
                filter.getPrice()
        );

        // Calculate pagination metadata
        int lastPageNumber = calculateLastPageNumber(totalRecords, pagination.getPageSize());
        int currentPageNumber = adjustCurrentPageNumber(pagination.getPageNumber(), lastPageNumber);

        // Get paginated results with filter
        int offset = (currentPageNumber - 1) * pagination.getPageSize();
        List<Property> properties = propertyRepository.findPropertiesWithFilter(
                filter.getType(),
                filter.getCategory(),
                filter.getStatus(),
                filter.getPrice(),
                offset,
                pagination.getPageSize()
        );

        return buildPaginationResult(
                currentPageNumber,
                lastPageNumber,
                pagination.getPageSize(),
                totalRecords,
                properties
        );
    }

    private int calculateLastPageNumber(long totalRecords, int pageSize) {
        int lastPage = (int) Math.ceil((double) totalRecords / pageSize);
        return lastPage == 0 ? 1 : lastPage;
    }

    private int adjustCurrentPageNumber(int requestedPage, int lastPage) {
        return Math.min(requestedPage, lastPage);
    }

    private PaginationResult<Property> buildPaginationResult(
            int currentPage,
            int lastPage,
            int pageSize,
            long totalRecords,
            List<Property> properties
    ) {
        PaginationResult<Property> result = new PaginationResult<>();
        result.setCurrentPageNumber(currentPage);
        result.setLastPageNumber(lastPage);
        result.setPageSize(pageSize);
        result.setTotalRecords(totalRecords);
        result.setRecords(properties);
        return result;
    }
}