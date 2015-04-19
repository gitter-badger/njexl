package com.noga.njexl.testing.dataprovider.excel;

import jxl.Workbook;

import java.io.File;

/**
 * Created by noga on 15/04/15.
 */
public class XlsReader implements ExcelReader {

    private Workbook workBook;

    /**
     * Creates an instance of the ExcelReaderXLS - sheet is pointing to sheetNum
     *
     * @param fileName the excel xsl file to read from
     *
     */
    public XlsReader(String fileName){
        try {
            workBook = Workbook.getWorkbook(new File(fileName));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public String[] sheets() {
        return workBook.getSheetNames();
    }

    @Override
    public String value(String sheet, int row, int column) {
        return workBook.getSheet(sheet).getCell(column, row).getContents();
    }

    @Override
    public int rowCount(String sheet) {
        return workBook.getSheet(sheet).getRows();
    }

    @Override
    public int columnCount(String sheet) {
        return workBook.getSheet(sheet).getColumns();
    }
}
