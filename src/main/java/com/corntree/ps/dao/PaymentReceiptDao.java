package com.corntree.ps.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.persistence.TypedQuery;


//import javax.transaction.Transactional;

//port org.springframework.data.repository.CrudRepository;

import com.corntree.ps.domain.PaymentReceipt;
/*
@Transactional
public interface PaymentReceiptDao extends CrudRepository<PaymentReceipt, Long> {

	public PaymentReceipt findByTransactionCode(String transactionCode);
	public PaymentReceipt findByReceipt(String receipt);
}
*/

@Repository
public class PaymentReceiptDao extends BaseDao {

    public PaymentReceipt findByReceipt(String receipt) {
        TypedQuery<PaymentReceipt> query = entityManager.createQuery("select p from PaymentReceipt p where p.receipt = :receipt",
                PaymentReceipt.class);
        query.setParameter("receipt", receipt);
        List<PaymentReceipt> results = query.getResultList();
        return results.size() > 0 ? results.get(0) : null;
    }
    
    public PaymentReceipt findByTransactionCode(String transCode) {
        TypedQuery<PaymentReceipt> query = entityManager.createQuery("select p from PaymentReceipt p where p.transactionCode = :transCode",
                PaymentReceipt.class);
        query.setParameter("transCode", transCode);
        List<PaymentReceipt> results = query.getResultList();
        return results.size() > 0 ? results.get(0) : null;
    }
    
	@Transactional
    public void save(PaymentReceipt paymentReceipt) {
    	entityManager.persist(paymentReceipt);
    }
}
