package org.qortal.repository.hsqldb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.qortal.data.at.ATData;
import org.qortal.data.at.ATStateData;
import org.qortal.repository.ATRepository;
import org.qortal.repository.DataException;

import com.google.common.primitives.Longs;

public class HSQLDBATRepository implements ATRepository {

	protected HSQLDBRepository repository;

	public HSQLDBATRepository(HSQLDBRepository repository) {
		this.repository = repository;
	}

	// ATs

	@Override
	public ATData fromATAddress(String atAddress) throws DataException {
		String sql = "SELECT creator, created_when, version, asset_id, code_bytes, code_hash, "
				+ "is_sleeping, sleep_until_height, is_finished, had_fatal_error, "
				+ "is_frozen, frozen_balance "
				+ "FROM ATs "
				+ "WHERE AT_address = ? LIMIT 1";

		try (ResultSet resultSet = this.repository.checkedExecute(sql, atAddress)) {
			if (resultSet == null)
				return null;

			byte[] creatorPublicKey = resultSet.getBytes(1);
			long created = resultSet.getLong(2);
			int version = resultSet.getInt(3);
			long assetId = resultSet.getLong(4);
			byte[] codeBytes = resultSet.getBytes(5); // Actually BLOB
			byte[] codeHash = resultSet.getBytes(6);
			boolean isSleeping = resultSet.getBoolean(7);

			Integer sleepUntilHeight = resultSet.getInt(8);
			if (sleepUntilHeight == 0 && resultSet.wasNull())
				sleepUntilHeight = null;

			boolean isFinished = resultSet.getBoolean(9);
			boolean hadFatalError = resultSet.getBoolean(10);
			boolean isFrozen = resultSet.getBoolean(11);

			Long frozenBalance = resultSet.getLong(11);
			if (frozenBalance == 0 && resultSet.wasNull())
				frozenBalance = null;

			return new ATData(atAddress, creatorPublicKey, created, version, assetId, codeBytes, codeHash,
					isSleeping, sleepUntilHeight, isFinished, hadFatalError, isFrozen, frozenBalance);
		} catch (SQLException e) {
			throw new DataException("Unable to fetch AT from repository", e);
		}
	}

	@Override
	public boolean exists(String atAddress) throws DataException {
		try {
			return this.repository.exists("ATs", "AT_address = ?", atAddress);
		} catch (SQLException e) {
			throw new DataException("Unable to check for AT in repository", e);
		}
	}

	@Override
	public byte[] getCreatorPublicKey(String atAddress) throws DataException {
		String sql = "SELECT creator FROM ATs WHERE AT_address = ?";

		try (ResultSet resultSet = this.repository.checkedExecute(sql, atAddress)) {
			if (resultSet == null)
				return null;

			return resultSet.getBytes(1);
		} catch (SQLException e) {
			throw new DataException("Unable to fetch AT creator's public key from repository", e);
		}
	}

	@Override
	public List<ATData> getAllExecutableATs() throws DataException {
		String sql = "SELECT AT_address, creator, created_when, version, asset_id, code_bytes, code_hash, "
				+ "is_sleeping, sleep_until_height, had_fatal_error, "
				+ "is_frozen, frozen_balance "
				+ "FROM ATs "
				+ "WHERE is_finished = false "
				+ "ORDER BY created_when ASC";

		List<ATData> executableATs = new ArrayList<>();

		try (ResultSet resultSet = this.repository.checkedExecute(sql)) {
			if (resultSet == null)
				return executableATs;

			boolean isFinished = false;

			do {
				String atAddress = resultSet.getString(1);
				byte[] creatorPublicKey = resultSet.getBytes(2);
				long created = resultSet.getLong(3);
				int version = resultSet.getInt(4);
				long assetId = resultSet.getLong(5);
				byte[] codeBytes = resultSet.getBytes(6); // Actually BLOB
				byte[] codeHash = resultSet.getBytes(7);
				boolean isSleeping = resultSet.getBoolean(8);

				Integer sleepUntilHeight = resultSet.getInt(9);
				if (sleepUntilHeight == 0 && resultSet.wasNull())
					sleepUntilHeight = null;

				boolean hadFatalError = resultSet.getBoolean(10);
				boolean isFrozen = resultSet.getBoolean(11);

				Long frozenBalance = resultSet.getLong(11);
				if (frozenBalance == 0 && resultSet.wasNull())
					frozenBalance = null;

				ATData atData = new ATData(atAddress, creatorPublicKey, created, version, assetId, codeBytes, codeHash,
						isSleeping, sleepUntilHeight, isFinished, hadFatalError, isFrozen, frozenBalance);

				executableATs.add(atData);
			} while (resultSet.next());

			return executableATs;
		} catch (SQLException e) {
			throw new DataException("Unable to fetch executable ATs from repository", e);
		}
	}

	@Override
	public List<ATData> getATsByFunctionality(byte[] codeHash, Boolean isExecutable, Integer limit, Integer offset, Boolean reverse) throws DataException {
		StringBuilder sql = new StringBuilder(512);
		List<Object> bindParams = new ArrayList<>();

		sql.append("SELECT AT_address, creator, created_when, version, asset_id, code_bytes, ")
				.append("is_sleeping, sleep_until_height, is_finished, had_fatal_error, ")
				.append("is_frozen, frozen_balance ")
				.append("FROM ATs ")
				.append("WHERE code_hash = ? ");
		bindParams.add(codeHash);

		if (isExecutable != null) {
			sql.append("AND is_finished != ? ");
			bindParams.add(isExecutable);
		}

		sql.append("ORDER BY created_when ");
		if (reverse != null && reverse)
			sql.append("DESC");

		HSQLDBRepository.limitOffsetSql(sql, limit, offset);

		List<ATData> matchingATs = new ArrayList<>();

		try (ResultSet resultSet = this.repository.checkedExecute(sql.toString(), bindParams.toArray())) {
			if (resultSet == null)
				return matchingATs;

			do {
				String atAddress = resultSet.getString(1);
				byte[] creatorPublicKey = resultSet.getBytes(2);
				long created = resultSet.getLong(3);
				int version = resultSet.getInt(4);
				long assetId = resultSet.getLong(5);
				byte[] codeBytes = resultSet.getBytes(6); // Actually BLOB
				boolean isSleeping = resultSet.getBoolean(7);

				Integer sleepUntilHeight = resultSet.getInt(8);
				if (sleepUntilHeight == 0 && resultSet.wasNull())
					sleepUntilHeight = null;

				boolean isFinished = resultSet.getBoolean(9);

				boolean hadFatalError = resultSet.getBoolean(10);
				boolean isFrozen = resultSet.getBoolean(11);

				Long frozenBalance = resultSet.getLong(12);
				if (frozenBalance == 0 && resultSet.wasNull())
					frozenBalance = null;

				ATData atData = new ATData(atAddress, creatorPublicKey, created, version, assetId, codeBytes, codeHash,
						isSleeping, sleepUntilHeight, isFinished, hadFatalError, isFrozen, frozenBalance);

				matchingATs.add(atData);
			} while (resultSet.next());

			return matchingATs;
		} catch (SQLException e) {
			throw new DataException("Unable to fetch matching ATs from repository", e);
		}
	}

	@Override
	public Integer getATCreationBlockHeight(String atAddress) throws DataException {
		String sql = "SELECT height "
				+ "FROM DeployATTransactions "
				+ "JOIN BlockTransactions ON transaction_signature = signature "
				+ "JOIN Blocks ON Blocks.signature = block_signature "
				+ "WHERE AT_address = ? "
				+ "LIMIT 1";

		try (ResultSet resultSet = this.repository.checkedExecute(sql, atAddress)) {
			if (resultSet == null)
				return null;

			return resultSet.getInt(1);
		} catch (SQLException e) {
			throw new DataException("Unable to fetch AT's creation block height from repository", e);
		}
	}

	@Override
	public void save(ATData atData) throws DataException {
		HSQLDBSaver saveHelper = new HSQLDBSaver("ATs");

		saveHelper.bind("AT_address", atData.getATAddress()).bind("creator", atData.getCreatorPublicKey()).bind("created_when", atData.getCreation())
				.bind("version", atData.getVersion()).bind("asset_id", atData.getAssetId())
				.bind("code_bytes", atData.getCodeBytes()).bind("code_hash", atData.getCodeHash())
				.bind("is_sleeping", atData.getIsSleeping()).bind("sleep_until_height", atData.getSleepUntilHeight())
				.bind("is_finished", atData.getIsFinished()).bind("had_fatal_error", atData.getHadFatalError()).bind("is_frozen", atData.getIsFrozen())
				.bind("frozen_balance", atData.getFrozenBalance());

		try {
			saveHelper.execute(this.repository);
		} catch (SQLException e) {
			throw new DataException("Unable to save AT into repository", e);
		}
	}

	@Override
	public void delete(String atAddress) throws DataException {
		try {
			this.repository.delete("ATs", "AT_address = ?", atAddress);
			// AT States also deleted via ON DELETE CASCADE
		} catch (SQLException e) {
			throw new DataException("Unable to delete AT from repository", e);
		}
	}

	// AT State

	@Override
	public ATStateData getATStateAtHeight(String atAddress, int height) throws DataException {
		String sql = "SELECT state_data, state_hash, fees, is_initial "
				+ "FROM ATStates "
				+ "WHERE AT_address = ? AND height = ? "
				+ "LIMIT 1";

		try (ResultSet resultSet = this.repository.checkedExecute(sql, atAddress, height)) {
			if (resultSet == null)
				return null;

			byte[] stateData = resultSet.getBytes(1); // Actually BLOB
			byte[] stateHash = resultSet.getBytes(2);
			long fees = resultSet.getLong(3);
			boolean isInitial = resultSet.getBoolean(4);

			return new ATStateData(atAddress, height, stateData, stateHash, fees, isInitial);
		} catch (SQLException e) {
			throw new DataException("Unable to fetch AT state from repository", e);
		}
	}

	@Override
	public ATStateData getLatestATState(String atAddress) throws DataException {
		String sql = "SELECT height, state_data, state_hash, fees, is_initial "
				+ "FROM ATStates "
				+ "WHERE AT_address = ? "
				// AT_address then height so the compound primary key is used as an index
				// Both must be the same direction also
				+ "ORDER BY AT_address DESC, height DESC "
				+ "LIMIT 1 ";

		try (ResultSet resultSet = this.repository.checkedExecute(sql, atAddress)) {
			if (resultSet == null)
				return null;

			int height = resultSet.getInt(1);
			byte[] stateData = resultSet.getBytes(2); // Actually BLOB
			byte[] stateHash = resultSet.getBytes(3);
			long fees = resultSet.getLong(4);
			boolean isInitial = resultSet.getBoolean(5);

			return new ATStateData(atAddress, height, stateData, stateHash, fees, isInitial);
		} catch (SQLException e) {
			throw new DataException("Unable to fetch latest AT state from repository", e);
		}
	}

	@Override
	public List<ATStateData> getMatchingFinalATStates(byte[] codeHash, Boolean isFinished,
			Integer dataByteOffset, Long expectedValue, Integer minimumFinalHeight,
			Integer limit, Integer offset, Boolean reverse) throws DataException {
		StringBuilder sql = new StringBuilder(1024);
		List<Object> bindParams = new ArrayList<>();

		sql.append("SELECT AT_address, height, state_data, state_hash, fees, is_initial "
				+ "FROM ATs "
				+ "CROSS JOIN LATERAL("
					+ "SELECT height, state_data, state_hash, fees, is_initial "
					+ "FROM ATStates "
					+ "WHERE ATStates.AT_address = ATs.AT_address ");

		if (minimumFinalHeight != null) {
			sql.append("AND height >= ? ");
			bindParams.add(minimumFinalHeight);
		}

		// AT_address then height so the compound primary key is used as an index
		// Both must be the same direction also
		sql.append("ORDER BY AT_address DESC, height DESC "
					+ "LIMIT 1 "
				+ ") AS FinalATStates "
				+ "WHERE code_hash = ? ");
		bindParams.add(codeHash);

		if (isFinished != null) {
			sql.append("AND is_finished = ? ");
			bindParams.add(isFinished);
		}

		if (dataByteOffset != null && expectedValue != null) {
			sql.append("AND SUBSTRING(state_data FROM ? FOR 8) = ? ");

			// We convert our long on Java-side to control endian
			byte[] rawExpectedValue = Longs.toByteArray(expectedValue);

			// SQL binary data offsets start at 1
			bindParams.add(dataByteOffset + 1);
			bindParams.add(rawExpectedValue);
		}

		sql.append(" ORDER BY height ");
		if (reverse != null && reverse)
			sql.append("DESC");

		HSQLDBRepository.limitOffsetSql(sql, limit, offset);

		List<ATStateData> atStates = new ArrayList<>();

		try (ResultSet resultSet = this.repository.checkedExecute(sql.toString(), bindParams.toArray())) {
			if (resultSet == null)
				return atStates;

			do {
				String atAddress = resultSet.getString(1);
				int height = resultSet.getInt(2);
				byte[] stateData = resultSet.getBytes(3); // Actually BLOB
				byte[] stateHash = resultSet.getBytes(4);
				long fees = resultSet.getLong(5);
				boolean isInitial = resultSet.getBoolean(6);

				ATStateData atStateData = new ATStateData(atAddress, height, stateData, stateHash, fees, isInitial);

				atStates.add(atStateData);
			} while (resultSet.next());

			return atStates;
		} catch (SQLException e) {
			throw new DataException("Unable to fetch matching AT states from repository", e);
		}
	}

	@Override
	public List<ATStateData> getBlockATStatesAtHeight(int height) throws DataException {
		String sql = "SELECT AT_address, state_hash, fees, is_initial "
				+ "FROM ATStates "
				+ "LEFT OUTER JOIN ATs USING (AT_address) "
				+ "WHERE height = ? "
				+ "ORDER BY created_when ASC";

		List<ATStateData> atStates = new ArrayList<>();

		try (ResultSet resultSet = this.repository.checkedExecute(sql, height)) {
			if (resultSet == null)
				return atStates; // No atStates in this block

			// NB: do-while loop because .checkedExecute() implicitly calls ResultSet.next() for us
			do {
				String atAddress = resultSet.getString(1);
				byte[] stateHash = resultSet.getBytes(2);
				long fees = resultSet.getLong(3);
				boolean isInitial = resultSet.getBoolean(4);

				ATStateData atStateData = new ATStateData(atAddress, height, stateHash, fees, isInitial);
				atStates.add(atStateData);
			} while (resultSet.next());
		} catch (SQLException e) {
			throw new DataException("Unable to fetch AT states for this height from repository", e);
		}

		return atStates;
	}

	@Override
	public int getAtTrimHeight() throws DataException {
		String sql = "SELECT AT_trim_height FROM DatabaseInfo";

		try (ResultSet resultSet = this.repository.checkedExecute(sql)) {
			if (resultSet == null)
				return 0;

			return resultSet.getInt(1);
		} catch (SQLException e) {
			throw new DataException("Unable to fetch AT state trim height from repository", e);
		}
	}

	@Override
	public void setAtTrimHeight(int trimHeight) throws DataException {
		String updateSql = "UPDATE DatabaseInfo SET AT_trim_height = ?";

		try {
			this.repository.executeCheckedUpdate(updateSql, trimHeight);
		} catch (SQLException e) {
			repository.examineException(e);
			throw new DataException("Unable to set AT state trim height in repository", e);
		}
	}

	@Override
	public void prepareForAtStateTrimming() throws DataException {
		// Rebuild cache of latest, non-finished AT states that we can't trim
		String deleteSql = "DELETE FROM LatestATStates";
		try {
			this.repository.executeCheckedUpdate(deleteSql);
		} catch (SQLException e) {
			repository.examineException(e);
			throw new DataException("Unable to delete temporary latest AT states cache from repository", e);
		}

		String insertSql = "INSERT INTO LatestATStates ("
				+ "SELECT AT_address, height FROM ATs "
				+ "CROSS JOIN LATERAL("
					+ "SELECT height FROM ATStates "
					+ "WHERE ATStates.AT_address = ATs.AT_address "
					+ "ORDER BY AT_address DESC, height DESC LIMIT 1"
				+ ") "
			+ ")";
		try {
			this.repository.executeCheckedUpdate(insertSql);
		} catch (SQLException e) {
			repository.examineException(e);
			throw new DataException("Unable to populate temporary latest AT states cache in repository", e);
		}
	}

	@Override
	public int trimAtStates(int minHeight, int maxHeight, int limit) throws DataException {
		if (minHeight >= maxHeight)
			return 0;

		// We're often called so no need to trim all states in one go.
		// Limit updates to reduce CPU and memory load.
		String sql = "UPDATE ATStates SET state_data = NULL "
				+ "WHERE state_data IS NOT NULL "
				+ "AND height BETWEEN ? AND ? "
				+ "AND NOT EXISTS("
					+ "SELECT TRUE FROM LatestATStates "
					+ "WHERE LatestATStates.AT_address = ATStates.AT_address "
					+ "AND LatestATStates.height = ATStates.height"
				+ ") "
				+ "LIMIT ?";

		try {
			return this.repository.executeCheckedUpdate(sql, minHeight, maxHeight, limit);
		} catch (SQLException e) {
			repository.examineException(e);
			throw new DataException("Unable to trim AT states in repository", e);
		}
	}

	@Override
	public void save(ATStateData atStateData) throws DataException {
		// We shouldn't ever save partial ATStateData
		if (atStateData.getStateHash() == null || atStateData.getHeight() == null)
			throw new IllegalArgumentException("Refusing to save partial AT state into repository!");

		HSQLDBSaver saveHelper = new HSQLDBSaver("ATStates");

		saveHelper.bind("AT_address", atStateData.getATAddress()).bind("height", atStateData.getHeight())
				.bind("state_data", atStateData.getStateData()).bind("state_hash", atStateData.getStateHash())
				.bind("fees", atStateData.getFees()).bind("is_initial", atStateData.isInitial());

		try {
			saveHelper.execute(this.repository);
		} catch (SQLException e) {
			throw new DataException("Unable to save AT state into repository", e);
		}
	}

	@Override
	public void delete(String atAddress, int height) throws DataException {
		try {
			this.repository.delete("ATStates", "AT_address = ? AND height = ?", atAddress, height);
		} catch (SQLException e) {
			throw new DataException("Unable to delete AT state from repository", e);
		}
	}

	@Override
	public void deleteATStates(int height) throws DataException {
		try {
			this.repository.delete("ATStates", "height = ?", height);
		} catch (SQLException e) {
			throw new DataException("Unable to delete AT states from repository", e);
		}
	}

	// Finding transactions for ATs to process

	public NextTransactionInfo findNextTransaction(String recipient, int height, int sequence) throws DataException {
		// We only need to search for a subset of transaction types: MESSAGE, PAYMENT or AT

		String sql = "SELECT height, sequence, Transactions.signature "
				+ "FROM ("
					+ "SELECT signature FROM PaymentTransactions WHERE recipient = ? "
					+ "UNION "
					+ "SELECT signature FROM MessageTransactions WHERE recipient = ? "
					+ "UNION "
					+ "SELECT signature FROM ATTransactions WHERE recipient = ?"
				+ ") AS Transactions "
				+ "JOIN BlockTransactions ON BlockTransactions.transaction_signature = Transactions.signature "
				+ "JOIN Blocks ON Blocks.signature = BlockTransactions.block_signature "
				+ "WHERE (height > ? OR (height = ? AND sequence > ?)) "
				+ "ORDER BY height ASC, sequence ASC "
				+ "LIMIT 1";

		Object[] bindParams = new Object[] { recipient, recipient, recipient, height, height, sequence };

		try (ResultSet resultSet = this.repository.checkedExecute(sql, bindParams)) {
			if (resultSet == null)
				return null;

			int nextHeight = resultSet.getInt(1);
			int nextSequence = resultSet.getInt(2);
			byte[] nextSignature = resultSet.getBytes(3);

			return new NextTransactionInfo(nextHeight, nextSequence, nextSignature);
		} catch (SQLException e) {
			throw new DataException("Unable to find next transaction to AT from repository", e);
		}

	}

}
