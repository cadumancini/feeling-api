package com.br.feelingestofados.feelingapi.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.util.List;

@Component
public class E070EMPService {
    private SessionFactory sessionFactory;

    @Autowired
    public E070EMPService(EntityManagerFactory factory) {
        if(factory.unwrap(SessionFactory.class) == null){
            throw new NullPointerException("factory is not a hibernate factory");
        }
        this.sessionFactory = factory.unwrap(SessionFactory.class);
    }

    public List findAll() {
        String hql = "from E070EMP where CODEMP > 0";
        Session currentSession = sessionFactory.getCurrentSession();
        Query query = currentSession.createQuery(hql);
        List results = query.list();
        return results;
    }
}
