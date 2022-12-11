package org.filters;

import org.HelperModels.ColumnStructure;
import org.ServiceModel.ElementHolder;
import org.baseFilters.BaseFilterModel;
import org.baseFilters.BaseFilterModelAnnotation;
import org.baseModels.BaseDataModel;
import org.baseModels.BaseDataModelAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Scanner;

/*
 Filter For Change CSV File To HTML Format
 */
@BaseFilterModelAnnotation(IsNeededSample = "false", IsIgnoreFirstLine = "true", CSVDelimiter = ",", CSVEscapeChar = "\"")
public class CSVtoHTMLTableRenderFilter extends BaseFilterModel {

    // Inject DataModel For Loading Elements //
    public CSVtoHTMLTableRenderFilter(BaseDataModel modelTypes) {
        modelType = modelTypes;
        Annotation modelTypeAnnotation = this.getClass().getAnnotation(BaseFilterModelAnnotation.class);
        BaseFilterModelAnnotation baseModelTypeAnnotation = (BaseFilterModelAnnotation) modelTypeAnnotation;

        int CSVDelimiter = baseModelTypeAnnotation.CSVDelimiter().charAt(0);
        int EscapeChar = baseModelTypeAnnotation.CSVEscapeChar().charAt(0);
        boolean isIgnoreFirstLine = Boolean.valueOf(this.getClass().getAnnotation(BaseFilterModelAnnotation.class).IsIgnoreFirstLine());

        // Add Column List Name //
        ArrayList<ColumnStructure> columns = new ArrayList<>();
        for (Field field : modelTypes.getClass().getDeclaredFields()) {
            columns.add(new ColumnStructure(field.getAnnotation(BaseDataModelAnnotation.class).ColumnName(), -1, -1));
        }


        initialFilterList = new ArrayList<>();
        initialFilterList.add(
                (initElement) -> {
                    String result = "<tr>";
                    for (ColumnStructure i : columns)
                        result += "<th>" + i.name + "</th>";
                    result += "</tr>";
                    return new ElementHolder("<html><body><table>" + result, -1);
                });

        recordFilterList = new ArrayList<>();
        recordFilterList.add(
                (element) -> {
                    if (element.id == 0 && isIgnoreFirstLine) return null;  // Ignore First Record In This Filter //

                    String tr_start = "<tr>"; // Point To The String Constant Pool
                    String tr_end = "</tr>"; // Point To The String Constant Pool
                    String td_start = "<td>"; // Point To The String Constant Pool
                    String td_end = "</td>"; // Point To The String Constant Pool
                    String replaceRecordCells = td_end + td_start; // Point To The String Constant Pool
                    // Best Performance For Creating And Working New String Use Builder //
                    StringBuilder _SB_ = new StringBuilder();
                    _SB_.append(td_start);

                    // Make Cell Table //
                    boolean isSeenUnDelimiterChar = false; // Use Atomic

                    // Prevent Using foreach //
                    char[] charArray = element.message.toCharArray();
                    for (int i = 0; i < charArray.length; i++) {
                        if (charArray[i] == EscapeChar) {
                            if (isSeenUnDelimiterChar) isSeenUnDelimiterChar = false;
                            else isSeenUnDelimiterChar = true;
                        } else if (charArray[i] == CSVDelimiter) {
                            if (!isSeenUnDelimiterChar) _SB_.append(replaceRecordCells);
                            else _SB_.append(charArray[i]);
                        } else _SB_.append(charArray[i]);
                    }

                    _SB_.insert(0, tr_start);
                    _SB_.append(tr_end);

                    element.message = _SB_.toString();

                    return element;
                }
        );

        endFilterList = new ArrayList<>();
        endFilterList.add(
                (endElement) -> {
                    return new ElementHolder("</table></body></html>", -1);
                }
        );

    }

    @Override
    public void takeSample(Scanner scannerFile) {
        setSample(new StringBuilder()); // Empty Builder For Sample // For CSV no need take Sample From File //
    }
}
