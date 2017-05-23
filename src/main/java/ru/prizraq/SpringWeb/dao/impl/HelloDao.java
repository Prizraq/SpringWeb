package ru.prizraq.SpringWeb.dao.impl;

import org.springframework.stereotype.Repository;

import ru.prizraq.SpringWeb.dao.IHelloDao;

@Repository
public class HelloDao extends AbstractDao
    implements IHelloDao
{
    public String getHello(String text)
    {
        System.out.println("In dao");
        System.out.println(getJdbcTemplate().queryForMap("select * from hallo_world	 where id=1"));
        return text;
    }
}
