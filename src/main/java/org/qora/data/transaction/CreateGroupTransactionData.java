package org.qora.data.transaction;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.qora.transaction.Transaction.TransactionType;

import io.swagger.v3.oas.annotations.media.Schema;

// All properties to be converted to JSON via JAX-RS
@XmlAccessorType(XmlAccessType.FIELD)
@Schema(allOf = { TransactionData.class })
public class CreateGroupTransactionData extends TransactionData {

	@Schema(description = "group owner's address", example = "QgV4s3xnzLhVBEJxcYui4u4q11yhUHsd9v")
	private String owner;
	@Schema(description = "group name", example = "miner-group")
	private String groupName;
	@Schema(description = "short description of group", example = "this group is for block miners")
	private String description;
	@Schema(description = "whether anyone can join group (open) or group is invite-only (closed)", example = "true")
	private boolean isOpen;

	// Constructors

	// For JAX-RS
	protected CreateGroupTransactionData() {
		super(TransactionType.CREATE_GROUP);
	}

	public CreateGroupTransactionData(byte[] creatorPublicKey, String owner, String groupName, String description, boolean isOpen, BigDecimal fee, long timestamp, byte[] reference,
			byte[] signature) {
		super(TransactionType.CREATE_GROUP, fee, creatorPublicKey, timestamp, reference, signature);

		this.creatorPublicKey = creatorPublicKey;
		this.owner = owner;
		this.groupName = groupName;
		this.description = description;
		this.isOpen = isOpen;
	}

	public CreateGroupTransactionData(byte[] creatorPublicKey, String owner, String groupName, String description, boolean isOpen, BigDecimal fee, long timestamp, byte[] reference) {
		this(creatorPublicKey, owner, groupName, description, isOpen, fee, timestamp, reference, null);
	}

	// Getters / setters

	public String getOwner() {
		return this.owner;
	}

	public String getGroupName() {
		return this.groupName;
	}

	public String getDescription() {
		return this.description;
	}

	public boolean getIsOpen() {
		return this.isOpen;
	}

	// Re-expose creatorPublicKey for this transaction type for JAXB
	@XmlElement(name = "creatorPublicKey")
	@Schema(name = "creatorPublicKey", description = "group creator's public key", example = "2tiMr5LTpaWCgbRvkPK8TFd7k63DyHJMMFFsz9uBf1ZP")
	public byte[] getGroupCreatorPublicKey() {
		return this.creatorPublicKey;
	}

	@XmlElement(name = "creatorPublicKey")
	@Schema(name = "creatorPublicKey", description = "group creator's public key", example = "2tiMr5LTpaWCgbRvkPK8TFd7k63DyHJMMFFsz9uBf1ZP")
	public void setGroupCreatorPublicKey(byte[] creatorPublicKey) {
		this.creatorPublicKey = creatorPublicKey;
	}

}
