package tr.com.huseyinaydin.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import tr.com.huseyinaydin.model.Payment;

//بسم الله الرحمن الرحيم

/**
 *
 * @author Huseyin_Aydin
 * @since 1994
 * @category Spring Boot
 *
 */

// Bu anotasyon, JSON içinde modelde tanımlı olmayan (fazladan) alanlar gelse bile hata fırlatmak yerine bunları yok saymamı sağlar.
@JsonIgnoreProperties(ignoreUnknown = true)
// Bu anotasyon, JSON çıktısında değeri null olan alanları hiç yazmayarak daha temiz ve sade bir JSON üretmemi sağlar.
@JsonInclude(Include.NON_NULL)
public class PaymentResponse {

	private String status;
	private String message;
	private String txDate;
	private List<Payment> payments;

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getTxDate() {
		return txDate;
	}
	public void setTxDate(String txDate) {
		this.txDate = txDate;
	}
	public List<Payment> getPayments() {
		return payments;
	}
	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}
}