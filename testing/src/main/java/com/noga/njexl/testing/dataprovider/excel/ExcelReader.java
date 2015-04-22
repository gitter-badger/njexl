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
