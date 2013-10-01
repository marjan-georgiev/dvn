/*
   Copyright (C) 2005-2012, by the President and Fellows of Harvard College.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

   Dataverse Network - A web application to share, preserve and analyze research data.
   Developed at the Institute for Quantitative Social Science, Harvard University.
   Version 3.0.
*/
/*
 * OAISetServiceBean.java
 *
 * Created on Oct 2, 2007, 5:13:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package edu.harvard.iq.dvn.core.vdc;


import ORG.oclc.oai.server.verb.NoItemsMatchException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Vector;

/**
 *
 * @author Ellen Kraffmiller
 */
@Stateless
public class OAISetServiceBean implements OAISetServiceLocal {
   @PersistenceContext(unitName="VDCNet-ejbPU")
    private EntityManager em; 
    
    public boolean specExists(String spec) {
        boolean specExists = true;
        try {
            OAISet set = findBySpec(spec);
            
        } catch(NoItemsMatchException e) {
            specExists = false;
        }
        return specExists;
    }

    public OAISet findBySpec(String spec) throws NoItemsMatchException{
     String query="SELECT o from OAISet o where o.spec = :fieldName";
       OAISet oaiSet=null;
       try {
           oaiSet=(OAISet)em.createQuery(query).setParameter("fieldName",spec).getSingleResult();
       } catch (javax.persistence.NoResultException e) {
           throw new ORG.oclc.oai.server.verb.NoItemsMatchException();
           // Do nothing, just return null. 
       }
       return oaiSet;
    }
/*
    public OAISet findByName(String name) throws NoItemsMatchException {
     String query="SELECT o from OAISet o where o.name = :fieldName";
       OAISet oaiSet=null;
       try {
           oaiSet=(OAISet)em.createQuery(query).setParameter("fieldName",name).getSingleResult();
       } catch (javax.persistence.NoResultException e) {
           throw new NoItemsMatchException();
           // Do nothing, just return null.
       }
       return oaiSet;
    }
*/
    public List<OAISet> findAll() {
        return em.createQuery("select object(o) from OAISet as o order by o.name").getResultList();
    }

     public List<OAISet> findAllOrdered() {


        List<OAISet> returnOaiSets = em.createQuery("select object(o) from OAISet  as o where  o.lockssConfig is null order by o.name").getResultList();
       returnOaiSets.addAll(em.createQuery("select object(o) from OAISet  as o where  o.lockssConfig is not null and o.lockssConfig.vdc is  null  order by o.name").getResultList());
       returnOaiSets.addAll(em.createQuery("select object(o) from OAISet  as o where  o.lockssConfig is not null and o.lockssConfig.vdc is not null  order by o.name").getResultList());

        return returnOaiSets;

    }

    public List<OAISet> findAllOrderedSorted() {
        //return em.createQuery("select object(o) from OAISet as o order by o.name").getResultList();

        //List<Long> returnOaiSets = new ArrayList();
        //return em.createQuery("select object(o) from OAISet  as o where  o.lockssconfig_id != null  order by o.name").getResultList();

        List<OAISet> returnOaiSets = getNonLockssDVOAISets();
        if (returnOaiSets== null) {
            returnOaiSets = new ArrayList<OAISet>();
        }
       returnOaiSets.addAll(em.createQuery("select object(o) from OAISet  as o where  o.lockssConfig is not null and o.lockssConfig.vdc is not null  order by o.name").getResultList());

        return returnOaiSets;

    }

    private List<OAISet> getNonLockssDVOAISets() {
        String queryStr = "select o.id from oaiset o left join lockssconfig l on o.lockssconfig_id = l.id where vdc_id is null order by o.name";
        Query query = em.createNativeQuery(queryStr);
        // since query is native, must parse through Vector results
        if (!query.getResultList().isEmpty()){
            String inClause = "(";
            int i = 0;
            for (Object currentResult : query.getResultList()) {
                // convert results into Longs
                if (i == 0){
                    
                   inClause = inClause + new Long(((Integer) (currentResult)));                           
                } else {
                    inClause = inClause + "," + new Long(((Integer) (currentResult))) ;
                }
                i++;
            }
            inClause = inClause + ")";

            return em.createQuery("select object(o) from OAISet  as o where  o.id in "+ inClause + "   order by o.name").getResultList();
        } else {
            return null;
        }
    }

    public void remove(Long id) {
        OAISet oaiSet = em.find(OAISet.class, id);
        em.createQuery("delete from HarvestStudy hs where hs.setName = '" + oaiSet.getSpec() + "'").executeUpdate();
        em.remove(oaiSet);
    }
    
    public OAISet findById(Long id) {
       return em.find(OAISet.class,id);
    }   
    
    public void update(OAISet oaiSet) {
        em.merge(oaiSet);
    }
    
    
}
