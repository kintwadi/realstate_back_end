package com.imovel.api.pagination;

import com.imovel.api.model.Property;
import com.imovel.api.repository.PropertyRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public class CustomPagination {

    private final PropertyRepository propertyRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public CustomPagination(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    // Temporarily commented out to test EntityManager issue
    /*
    @Transactional(readOnly = true)
    public PaginationResult<Property> getProperties(Pagination pagination) {
        // Create query
        TypedQuery<Property> query = entityManager.createQuery(
                "SELECT p FROM Property p ORDER BY p.id", Property.class);

        // Get total count
        long totalRecords = propertyRepository.countAllProperties();

        // Calculate pagination metadata
        int lastPageNumber = calculateLastPageNumber(totalRecords, pagination.getPageSize());
        int currentPageNumber = adjustCurrentPageNumber(pagination.getPageNumber(), lastPageNumber);

        // Apply pagination
        int offset = (currentPageNumber - 1) * pagination.getPageSize();
        query.setFirstResult(offset);
        query.setMaxResults(pagination.getPageSize());

        // Execute query
        List<Property> properties = query.getResultList();

        return buildPaginationResult(
                currentPageNumber,
                lastPageNumber,
                pagination.getPageSize(),
                totalRecords,
                properties
        );
    }
    */
    
    public List<Property> getProperties(Pagination pagination, String sortBy, String sortDirection) {
        TypedQuery<Property> query = entityManager.createQuery(
            "SELECT p FROM Property p ORDER BY p." + sortBy + " " + sortDirection, Property.class);
        query.setFirstResult((pagination.getPageNumber() - 1) * pagination.getPageSize());
        query.setMaxResults(pagination.getPageSize());
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public PaginationResult<Property> getPropertiesWithFilter(Pagination pagination, Property filter) {
        // Build dynamic query based on filter
        String queryStr = "SELECT p FROM Property p WHERE " +
                "(:type IS NULL OR p.type = :type) AND " +
                "(:category IS NULL OR p.category = :category) AND " +
                "(:status IS NULL OR p.status = :status) AND " +
                "(:price IS NULL OR p.price = :price) " +
                "ORDER BY p.id";

        // Create query
        TypedQuery<Property> query = entityManager.createQuery(queryStr, Property.class)
                .setParameter("type", filter.getType())
                .setParameter("category", filter.getCategory())
                .setParameter("status", filter.getStatus())
                .setParameter("price", filter.getPrice());

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

        // Apply pagination
        int offset = (currentPageNumber - 1) * pagination.getPageSize();
        query.setFirstResult(offset);
        query.setMaxResults(pagination.getPageSize());

        // Execute query
        List<Property> properties = query.getResultList();

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