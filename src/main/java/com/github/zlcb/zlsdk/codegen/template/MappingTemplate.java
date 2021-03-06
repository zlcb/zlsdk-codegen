package com.github.zlcb.zlsdk.codegen.template;

import com.github.zlcb.zlsdk.codegen.engine.AbstractTemplateEngine;
import com.github.zlcb.zlsdk.codegen.model.Column;
import com.github.zlcb.zlsdk.codegen.model.Table;
import org.apache.commons.text.CaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zhongl
 * @date 2020/05/23 15:41
 */
public class MappingTemplate extends AbstractTemplate {

    public MappingTemplate(AbstractTemplateEngine engine) {
        super(engine);
        this.templatePath = getConfig().getTemplate()
                .getMapping();
    }

    @Override
    public void execute(Table t) {
        Map<String, Object> dataMap = new HashMap<String, Object>();

        dataMap.put("tableName", t.getName());
        String mapperName = String.format("%sMapper", t.getEntityName());
        dataMap.put("mapperName", mapperName);
        dataMap.put("mapperPackage", getConfig().getPackages().getMapper());
        dataMap.put("entityName", t.getEntityName());
        dataMap.put("entityPackage", getConfig().getPackages().getEntity());

        List<Map<String,Object>> fieldList = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> keyList = new ArrayList<Map<String,Object>>();
        boolean autoInc = false;
        StringBuilder aifn = new StringBuilder();
        StringBuilder aicn = new StringBuilder();

        String fieldName;
        Map<String,Object> fieldMap, keyMap;
        for (Column col : t.getColumns()) {
            fieldName = CaseUtils.toCamelCase(col.getName(), false, new char[]{'_'});
            fieldMap = new HashMap<String, Object>(4);
            fieldMap.put("name", fieldName);
            fieldMap.put("columnName", col.getName());
            fieldMap.put("jdbcType", col.getDataType().getJdbcType());
            fieldList.add(fieldMap);

            //主键
            if (col.isPrimaryKey()) {
                keyMap = new HashMap<>(2);
                keyMap.put("name", fieldName);
                keyMap.put("columnName", col.getName());
                keyList.add(keyMap);

                if (col.isAutoIncrement()) {
                    autoInc = true;
                    if (aifn.length() > 0) {
                        aifn.append(",");
                        aicn.append(",");
                    }
                    aifn.append(fieldName);
                    aicn.append(col.getName());
                }
            }
        }

        dataMap.put("fields", fieldList);
        dataMap.put("keys", keyList);
        dataMap.put("autoInc", autoInc);
        dataMap.put("autoIncProp", aifn.toString());
        dataMap.put("autoIncCol", aicn.toString());

        //生成Mapping文件
        engine.outputFile(templatePath, getConfig().getOutputPath()
                .getMapping(), mapperName, "xml", dataMap);
    }
}
