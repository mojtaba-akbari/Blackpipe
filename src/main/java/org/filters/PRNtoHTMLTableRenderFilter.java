package org.filters;

import org.HelperModels.ColumnStructure;
import org.ServiceModel.ElementHolder;
import org.baseFilters.BaseFilterModel;
import org.baseFilters.BaseFilterModelAnnotation;
import org.baseModels.BaseDataModel;
import org.baseModels.BaseDataModelAnnotation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Scanner;

/*
 Filter For Change PRN File To HTML Format
 */
@BaseFilterModelAnnotation(IsNeededSample = "true", IsIgnoreFirstLine = "true")
public class PRNtoHTMLTableRenderFilter extends BaseFilterModel {


    public PRNtoHTMLTableRenderFilter(BaseDataModel modelTypes) {
        modelType = modelTypes;

        // Add Column List Name And Calculate Their Len For PRN File //
        // Any Structure That you Create With Pointer To Other Function Can retain in memory do not worry :) //
        ArrayList<ColumnStructure> columns = new ArrayList<>();
        for (Field field : modelTypes.getClass().getDeclaredFields()) {
            columns.add(new ColumnStructure(field.getAnnotation(BaseDataModelAnnotation.class).ColumnName(), -1, -1)); // default column length //
        }

        boolean isIgnoreFirstLine = Boolean.valueOf(this.getClass().getAnnotation(BaseFilterModelAnnotation.class).IsIgnoreFirstLine());

        // PRN Format Need One Init Filter To Add Their Len Per Column //
        // Order Is Very Important Because On Order Executed //
        initialFilterList = new ArrayList<>();
        // Sample Processing //
        // This Filer Is Not Need Low Latency Because Process One Time //
        initialFilterList.add((initElement) -> {
            for (int i = 0; i < columns.size(); i++) {
                columns.get(i).offset = getSample().toString().indexOf(columns.get(i).name); // take offset
                if (i != columns.size() - 1)
                    columns.get(i).len = getSample().toString().indexOf(columns.get(i + 1).name) - columns.get(i).offset - 1; // take next columns offset and update len
                else
                    columns.get(i).len = getSample().toString().length() - columns.get(i).offset; // last item should take diff with full len
            }

            return null;
        });
        // End //
        initialFilterList.add(
                (initElement) -> {
                    String result = "<tr>";
                    for (ColumnStructure i : columns)
                        result += "<th>" + i.name + "</th>";
                    result += "</tr>";
                    return new ElementHolder("<html><body><table>" + result, -1); // Generate Undemanding Element without offset
                });


        recordFilterList = new ArrayList<>();
        recordFilterList.add(
                (element) -> {
                    if (element.id == 0 && isIgnoreFirstLine) return null;  // Ignore First Record In This Filter //

                    String tr_start = "<tr>"; // Point To The String Constant Pool
                    String tr_end = "</tr>"; // Point To The String Constant Pool
                    String td_start = "<td>"; // Point To The String Constant Pool
                    String td_end = "</td>"; // Point To The String Constant Pool
                    // Best Performance For Creating And Working New String Use Builder //
                    StringBuilder _SB_ = new StringBuilder();
                    _SB_.append(tr_start);

                    // Simply Substring with offset and len //
                    for (ColumnStructure c : columns) {
                        _SB_.append(td_start + element.message.substring(c.offset, c.offset + c.len) + td_end);
                    }

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

    // PRN Sample
    // Just Take First Line //
    @Override
    public void takeSample(Scanner scannerFile) {
        this.setSample(new StringBuilder(scannerFile.nextLine()));
        scannerFile.close();
    }
}
