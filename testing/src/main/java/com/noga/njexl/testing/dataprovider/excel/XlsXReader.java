/**
 * Copyright 2015 Nabarun Mondal
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.noga.njexl.testing.dataprovider.excel;

import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Created by noga on 15/04/15.
 */
public class XlsXReader implements ExcelReader {

    XSSFWorkbook workbook;

    String[] sheetNames;

    final FormulaEvaluator evaluator ;

    public  String readCellValueAsString(XSSFCell cell) {
        CellValue cellValue = evaluator.evaluate(cell);
        switch (cellValue.getCellType()) {
            case XSSFCell.CELL_TYPE_BLANK:
                return "";
            case XSSFCell.CELL_TYPE_BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case XSSFCell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case XSSFCell.CELL_TYPE_NUMERIC:
                Double d = new Double(cell.getNumericCellValue());
                long l = d.longValue();
                Double compD = (double) l;
                if (compD.equals(d)) {
                    return Long.toString(l);
                } else {
                    return Double.toString(d);
                }
        }
        return null;
    }

    private void init(){
        ArrayList<String> names = new ArrayList<>();
        int numSheets = workbook.getNumberOfSheets();
        for ( int i = 0 ; i < numSheets; i++ ){
            names.add(  workbook.getSheetName(i) );
        }
        sheetNames = new String[names.size()];
        sheetNames = names.toArray( sheetNames );
    }

    /**
     * Creates an instance of the ExcelReaderXLSX
     *
     * @param fileName the excel xsl file to read from
     */
    public XlsXReader(String fileName) {
        try {
            workbook = new XSSFWorkbook(new FileInputStream(fileName));
            evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            init();

        } catch (Exception e) {
            throw new Error(e);
        }
    }


    @Override
    public String[] sheets() {
        return sheetNames ;
    }

    @Override
    public String value(String sheet, int row, int column) {
        XSSFCell cell = workbook.getSheet(sheet).getRow(row).getCell(column);
        return readCellValueAsString(cell);
    }

    @Override
    public int rowCount(String sheet) {
        return workbook.getSheet(sheet).getLastRowNum() + 1 ;
    }

    @Override
    public int columnCount(String sheet) {
        return workbook.getSheet(sheet).getRow(0).getLastCellNum() ;
    }
}