package com.sanri.app.servlet;

import com.sanri.app.BaseServlet;
import com.sanri.app.jdbc.ExConnection;
import com.sanri.app.jdbc.Table;
import com.sanri.app.postman.CodeGeneratorConfig;
import com.sanri.frame.RequestMapping;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@RequestMapping("/mybatis/code")
public class MybatisGeneratorServlet extends BaseServlet {
    /**
     * 多表或单表部分代码生成,使用 mbg
     * @param codeGeneratorConfig
     * @return
     * @throws SQLException
     */
    public String tablesCode(CodeGeneratorConfig codeGeneratorConfig) throws SQLException {
        List<Table> tables = codeGeneratorConfig.getConnectionConfig().tables();

        return "";
    }
}
