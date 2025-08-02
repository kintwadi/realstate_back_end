package com.imovel.api.pagination;

import com.imovel.api.model.Property;
import com.imovel.api.model.enums.PropertyCategory;
import com.imovel.api.model.enums.PropertyType;

public class Usage {

    public static void main(String[] args) {

       // 1. Basic Pagination:

        {
            Pagination pagination = new Pagination();
            pagination.setPageNumber(2);
            pagination.setPageSize(10);

            //PaginationResult<Property> result = propertyPaginationService.getProperties(pagination);
        }


        //2.Filtered Pagination:
        Pagination pagination = new Pagination();
        pagination.setPageNumber(1);
        pagination.setPageSize(5);

        Property filter = new Property();
        filter.setType(PropertyType.RENT);
        filter.setCategory(PropertyCategory.APARTMENT);

        //PaginationResult<Property> result = propertyPaginationService.getPropertiesWithFilter(pagination, filter);
    }
}
