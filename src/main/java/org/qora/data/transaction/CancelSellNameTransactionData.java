package org.qora.data.transaction;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.qora.transaction.Transaction.TransactionType;

import io.swagger.v3.oas.annotations.media.Schema;

// All properties to be converted to JSON via JAX-RS
@XmlAccessorType(XmlAccessType.FIELD)
@Schema(allOf = { TransactionData.class })
public class CancelSellNameTransactionData extends TransactionData {

	// Properties
	@Schema(description = "owner's public key", example = "2tiMr5LTpaWCgbRvkPK8TFd7k63DyHJMMFFsz9uBf1ZP")
	private byte[] ownerPublicKey;
	@Schema(description = "which name to cancel selling", example = "my-name")
	private String name;

	// Constructors

	// For JAX-RS
	protected CancelSellNameTransactionData() {
		super(TransactionType.CANCEL_SELL_NAME);
	}

	public CancelSellNameTransactionData(byte[] ownerPublicKey, String name, BigDecimal fee, long timestamp, byte[] reference, byte[] signature) {
		super(TransactionType.CANCEL_SELL_NAME, fee, ownerPublicKey, timestamp, reference, signature);

		this.ownerPublicKey = ownerPublicKey;
		this.name = name;
	}

	public CancelSellNameTransactionData(byte[] ownerPublicKey, String name, BigDecimal fee, long timestamp, byte[] reference) {
		this(ownerPublicKey, name, fee, timestamp, reference, null);
	}

	// Getters / setters

	public byte[] getOwnerPublicKey() {
		return this.ownerPublicKey;
	}

	public String getName() {
		return this.name;
	}

}
