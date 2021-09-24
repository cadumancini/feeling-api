package com.br.feelingestofados.feelingapi.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.util.List;

@Component
public class DBQueriesService {
    private SessionFactory sessionFactory;

    @Autowired
    public DBQueriesService(EntityManagerFactory factory) {
        if(factory.unwrap(SessionFactory.class) == null){
            throw new NullPointerException("factory is not a hibernate factory");
        }
        this.sessionFactory = factory.unwrap(SessionFactory.class);
    }

    public List findAll() {
        String hql = "SELECT CODEMP, CODFIL, NOMFIL FROM E070FIL WHERE CODEMP > 0";
        Session currentSession = sessionFactory.getCurrentSession();
        Query query = currentSession.createSQLQuery(hql);
        List results = query.list();
        return results;
    }
}
