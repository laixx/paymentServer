package com.corntree.ps.dao;

import java.util.List;

//import javax.transaction.Transactional;

//import org.springframework.data.repository.CrudRepository;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.corntree.ps.domain.Payment;
/*
@Transactional
public interface PaymentDao extends CrudRepository<Payment, Long> {

	public Payment findByTransactionCode(String transactionCode);
}
*/

@Repository
public class PaymentDao extends BaseDao {

    @Transactional
    public Payment createPayment(int serverId, long accountId, long playerId, int ingots, String code) {
        Payment payment = new Payment(serverId, accountId, playerId, ingots, code);
        entityManager.persist(payment);
        return payment;
    }
    
    public Payment findByTransactionCode(String transactionCode) {
        TypedQuery<Payment> query = entityManager.createQuery("select p from Payment p where p.transactionCode = :transactionCode",
                Payment.class);
        query.setParameter("transactionCode", transactionCode);
        List<Payment> results = query.getResultList();
        return results.size() > 0 ? results.get(0) : null;
    }

    public Payment retrievePayment(Long paymentId) {
        TypedQuery<Payment> query = entityManager.createQuery("select from Payment p where p.id = :paymentId", Payment.class);
        query.setParameter("paymentId", paymentId);
        List<Payment> results = query.getResultList();
        return results.size() > 0 ? results.get(0) : null;
    }

    @Transactional
    public void save(Payment payment) {
    	entityManager.persist(payment);
    }
    
    public void updatePayment(Payment payment) {
        jdbcTemplate.update("update payment set fee = ? , finished = 0, append_msg = ?, ingots = ?, recharge_channel = ?, discount = ?, sandbox = ? where id = ?", payment.getFee(), payment.getAppendMsg(), payment.getIngots(), payment.getRechargeChannel(), payment.getDiscount(), payment.getSandbox(), payment.getId());
    }
    
    public void updatePaymentFinished(String code) {
        jdbcTemplate.update("update payment set finished = 1 where transaction_code = ?", code);
    }
}