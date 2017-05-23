package ru.prizraq.SpringWeb.dao.impl;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class AbstractDao
{
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcOperations namedParameterJdbcOperations;
    
    @Autowired
    public void setDataSource(DataSource dataSource)
    {
        new org.postgresql.Driver();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcOperations = new NamedParameterJdbcTemplate(dataSource); 
    }

    public JdbcTemplate getJdbcTemplate()
    {
        return jdbcTemplate;
    }
    
    /**
     * Этот класс предоставляет выполнять запросы с именованными параметрами, параметры которого содержатся в мапке или бине
     * @return
     */
    public NamedParameterJdbcOperations getNamedParameterJdbcOperations()
    {
        return namedParameterJdbcOperations;
    }
}
