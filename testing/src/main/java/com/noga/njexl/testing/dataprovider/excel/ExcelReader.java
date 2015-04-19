package com.noga.njexl.testing.dataprovider.excel;

/**
 * Created by noga on 15/04/15.
 */
public interface ExcelReader {

    /**
     * gets the names of the sheets of the  excel file
     * @return String[] of the names of the worksheets
     */
    String[] sheets();

    /**
     * Gets the value of the excel cell as string
     * @param sheet
     *@param row    0 based row index
     * @param column  @return value of the cell, casted to string
     */
    String value(String sheet, int row, int column);

    /**
     * Gets the row count in the sheetName
     * @param sheet
     * @return the number of rows in the sheet
     */
    int rowCount(String sheet);

    /**
     * Gets the column  count in the sheet for the row
     * @param sheet
     * @return the column count
     */
    int columnCount(String sheet);

}
