package tr.com.huseyinaydin.dao;

import java.util.List;

import tr.com.huseyinaydin.model.Payment;

//بسم الله الرحمن الرحيم

/**
 *
 * @author Huseyin_Aydin
 * @since 1994
 * @category Spring Boot
 *
 */

public interface PaymentDao {
	public String payNow(Payment payment);
	public List<Payment> getTransactionInfo(String vendor);
}