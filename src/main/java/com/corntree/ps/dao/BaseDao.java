package com.corntree.ps.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class BaseDao {

    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    
    public BaseDao() {
    	
    }
}
