package org.filters;

import org.baseFilters.BaseFilterModel;
import org.baseFilters.BaseFilterModelAnnotation;
import org.baseModels.BaseDataModel;
import org.baseModels.BaseDataModelAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

@BaseFilterModelAnnotation(IsNeededSample = "true")
public class PRNtoHTMLTableRenderFilter extends BaseFilterModel {

    // Simple Data Structure For Retain Offset And Len //
     class Column{
        public String name;
        public int offset;
        public int len;
        Column(String name,int offset,int len){
            this.name=name;
            this.offset=offset;
            this.len=len;
        }
    }

    public PRNtoHTMLTableRenderFilter(BaseDataModel modelTypes) {
        modelType=modelTypes;

        // Add Column List Name And Calculate Their Len For PRN File //
        // Any Structure That you Create With Pointer To Other Function Can retain in memory do not worry :) //
        ArrayList<Column> columns= new ArrayList<>();
        for (Field field : modelTypes.getClass().getFields()) {
            columns.add(new Column(field.getAnnotation(BaseDataModelAnnotation.class).ColumnName(),-1,-1)); // default column length //
        }


        // PRN Format Need One Init Filter To Add Their Len Per Column //
        // Order Is Very Important Because On Order Executed //
        initialFilterList=new ArrayList<>();
        // Sample Processing //
        // This Filer Is Not Need Low Latency Because Process One Time //
        initialFilterList.add((initSource)->{
                    for(int i=0;i<columns.size();i++){
                        columns.get(i).offset=getSample().indexOf(columns.get(i).name); // take offset
                        if(i != columns.size()-1) columns.get(i).len=getSample().indexOf(columns.get(i+1).name)-columns.get(i).offset - 1; // take next columns offset and update len
                        else columns.get(i).len=getSample().length()-columns.get(i).offset; // last item should take diff with full len
                    }

                    return "";
                });
        // End //
        initialFilterList.add(
                (initSource) -> {
                    String result="<tr>";
                    for(Column i:columns)
                        result+="<th>"+i.name+"</th>";
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
                    // Best Performance For Creating And Working New String Use Builder //
                    StringBuilder _SB_=new StringBuilder();
                    _SB_.append(td_start);

                    // Simply Substring with offset and len //
                    for(Column c:columns){
                        _SB_.append(tr_start+record.substring(c.offset,c.len)+tr_end);
                    }

                    // Make A HTML Table Record //
                    _SB_.insert(0,tr_start);
                    _SB_.append(tr_end);

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

    // PRN Sample
    // Just Take First Line //
    @Override
    public void takeSample(Scanner scannerFile) {
        this.setSample(new StringBuilder(scannerFile.nextLine()));
        scannerFile.close();
    }
}
