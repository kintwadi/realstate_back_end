
package com.imovel.api.util;

public class Pagination
{
    // ---------------------------------------------------------------------
    // Properties
    // ---------------------------------------------------------------------
    private int pageSize;
    private int pageNumber;
    // ---------------------------------------------------------------------
    // Construction
    // ---------------------------------------------------------------------
    public int getPageSize()
    {
        return pageSize;
    }

    public void setPageSize( int pageSize )
    {
        this.pageSize = pageSize;
    }

    public int getPageNumber()
    {
        return pageNumber;
    }

    public void setPageNumber( int pageNumber )
    {
        this.pageNumber = pageNumber;
    }


}

