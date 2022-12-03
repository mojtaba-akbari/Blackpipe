package org.filters;

import org.baseFilters.BaseFilterModel;
import org.baseModels.BaseDataModel;
import org.baseModels.BaseDataModelAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class HTMLTableRenderFilter extends BaseFilterModel {

    // Inject DataModel For Loading Elements //
    public HTMLTableRenderFilter(BaseDataModel modelTypes) {
        modelType=modelTypes;
        Annotation modelTypeAnnotation=this.modelType.getClass().getAnnotation(BaseDataModelAnnotation.class);
        BaseDataModelAnnotation baseModelTypeAnnotation= (BaseDataModelAnnotation) modelTypeAnnotation;
        int CSVDelimiter=baseModelTypeAnnotation.CSVDelimiter().charAt(0);
        int EscapeChar=baseModelTypeAnnotation.CSVEscapeChar().charAt(0);

        // Add Column List Name And Calculate Their Len For PRN File //
        ArrayList<String> columnNameAndTheirLen= new ArrayList<>();
        for (Field field : modelTypes.getClass().getFields()) {
            columnNameAndTheirLen.add(field.getAnnotation(BaseDataModelAnnotation.class).ColumnName());
        }


        initialFilterList=new ArrayList<>();
        initialFilterList.add(
             (initSource) -> {
                String result="<tr>";
                for(String i:columnNameAndTheirLen)
                    result+="<th>"+i+"</th>";
                result+="</tr>";
                return "<html><body><table>"+result;
            });

        recordFilterList=new ArrayList<>();
        recordFilterList.add(
            (record) -> {
                String tr_start="<tr>"; // Point To The String Constant Pool
                String tr_end="</tr>"; // Point To The String Constant Pool
                String td_start="<td>"; // Point To The String Constant Pool
                String td_end="</td>"; // Point To The String Constant Pool
                String replaceRecordCells=td_end+td_start; // Point To The String Constant Pool
                // Best Performance For Creating And Working New String Use Builder //
                StringBuilder _SB_=new StringBuilder();
                _SB_.append(td_start);

                // Make Cell Table //
                boolean isSeenUnDelimiterChar= false; // Use Atomic Because Of Parallel

                // Prevent Using foreach //
                char[] charArray=record.toCharArray();
                for(int i=0;i<charArray.length;i++){
                    if(charArray[i] == EscapeChar) {
                        if(isSeenUnDelimiterChar) isSeenUnDelimiterChar=false;
                        else isSeenUnDelimiterChar=true;
                    }
                    else if(charArray[i] == CSVDelimiter) {
                        if(!isSeenUnDelimiterChar) _SB_.append(replaceRecordCells);
                        else _SB_.append(charArray[i]);
                    }
                    else _SB_.append(charArray[i]);
                }


                // Make A HTML Table Record //
                _SB_.insert(0,tr_start);
                _SB_.append(tr_end);

                charArray=null;

                return _SB_.toString();
            }
        );

        endFilterList=new ArrayList<>();
        endFilterList.add(
            (endSource) -> {
                return "</table></body></html>";
            }
        );

    }
}
