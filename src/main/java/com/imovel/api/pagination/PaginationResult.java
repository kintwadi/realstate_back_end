
package com.imovel.api.pagination;

import java.util.ArrayList;
import java.util.List;

public class PaginationResult< T >
{
    private int       currentPageNumber;
    private int       lastPageNumber;
    private int       pageSize;
    private long      totalRecords;
    private List< T > records;

    public PaginationResult() {
        records = new ArrayList< T >();
    }

    public int getCurrentPageNumber()
    {
        return currentPageNumber;
    }
    public void setCurrentPageNumber( int currentPageNumber )
    {
        this.currentPageNumber = currentPageNumber;
    }
    public int getLastPageNumber()
    {
        return lastPageNumber;
    }

    public void setLastPageNumber( int lastPageNumber )
    {
        this.lastPageNumber = lastPageNumber;
    }

    public int getPageSize()
    {
        return pageSize;
    }

    public void setPageSize( int pageSize )
    {
        this.pageSize = pageSize;
    }

    public long getTotalRecords()
    {
        return totalRecords;
    }

    public void setTotalRecords( long totalRecords )
    {
        this.totalRecords = totalRecords;
    }

    public List< T > getRecords()
    {
        return records;
    }

    public void setRecords( List< T > records )
    {
        this.records = records;
    }


}