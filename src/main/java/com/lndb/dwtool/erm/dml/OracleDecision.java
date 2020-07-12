package com.lndb.dwtool.erm.dml;

import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.lndb.dwtool.erm.db.ColumnDescriptor;
import com.lndb.dwtool.erm.db.TableDescriptor;
import com.lndb.dwtool.erm.dml.data.TupleData;


/**
 * 
 * @author ZHANGMA
 *
 */
public enum OracleDecision {
	INSERT(OracleSqlConstant.UserDecision.INSERT) {
		@Override
		public void generateSQL(TupleData tuple) {
			if (!validateUpdateData(tuple)) {
				return ;
			}
			
			StringBuilder insertSql = new StringBuilder(OracleSqlConstant.INSERT_STATEMENT);
			insertSql.append(tuple.getTableName() + OracleSqlConstant.LEFT_PARENTHESE);
			
			int counter = 1;
			for (String colName : tuple.getUpdatingColumns()) {
				insertSql.append(colName + (counter != tuple.getUpdatingColumns().size() ? OracleSqlConstant.SEPARATOR : OracleSqlConstant.RIGHT_PARENTHESE));
				counter++;
			}
			
			insertSql.append(OracleSqlConstant.VALUES_CLAUSE + OracleSqlConstant.LEFT_PARENTHESE);
			
			counter = 1;
			for (String colName : tuple.getUpdatingColumns()) {
				String colValue = tuple.getUpdatingColValMap().get(colName);
				Integer colType = tuple.getUpdatingColTypeMap().get(colName);
				
				if (ConverterUtil.doesDataTypeRequireSingleQuote(colType, colName, colValue)) {
					insertSql.append(OracleSqlConstant.SINGLE_QUOTE);
					insertSql.append(colValue);
					insertSql.append(OracleSqlConstant.SINGLE_QUOTE);
				}
				else {
					insertSql.append(colValue);
				}
				insertSql.append(counter != tuple.getUpdatingColumns().size()?  OracleSqlConstant.SEPARATOR : OracleSqlConstant.RIGHT_PARENTHESE);
				counter++;
			}
			
			
			insertSql.append(OracleSqlConstant.END_OF_SQL_LINE);
			
			tuple.setSqlString(insertSql.toString());
		}
		
	},
	DELETE(OracleSqlConstant.UserDecision.DELETE) {
		@Override
		public void generateSQL(TupleData tuple) {
			if (!validateTableName(tuple)) {
				return;
			}
			if (!validMatchingColumnAndData(tuple)) {
				return;
			}
			
			StringBuilder deleteSql = new StringBuilder(OracleSqlConstant.DELETE_STATEMENT);
			deleteSql.append(tuple.getTableName());
			deleteSql.append(OracleSqlConstant.WHERE_CLAUSE);
			
			int counter = 1;
			String colValue;
			for (String colName : tuple.getMatchingColumns()) {
				Integer colType = tuple.getMatchingColTypeMap().get(colName);
				deleteSql.append(colName + OracleSqlConstant.EQUAL_SIGN);
				colValue = tuple.getMatchingColValMap().get(colName);
				
				if (ConverterUtil.doesDataTypeRequireSingleQuote(colType, colName, colValue)) {
					deleteSql.append(OracleSqlConstant.SINGLE_QUOTE);
					deleteSql.append(colValue);
					deleteSql.append(OracleSqlConstant.SINGLE_QUOTE);
				}
				else {
					deleteSql.append(tuple.getMatchingColValMap().get(colName));
				}
				deleteSql.append(counter != tuple.getMatchingColumns().size()?  OracleSqlConstant.AND_CLAUSE : "");
				counter++;
			}
			
			deleteSql.append(OracleSqlConstant.END_OF_SQL_LINE);
			tuple.setSqlString(deleteSql.toString());
			return;
		}
	},
	UPDATE(OracleSqlConstant.UserDecision.UPDATE) {
		@Override
		public void generateSQL(TupleData tuple) {
			if (!validateUpdateData(tuple)) {
				return;
			}
			
			if (!validMatchingColumnAndData(tuple)) {
				return;
			}
			
			StringBuilder updateSql = new StringBuilder(OracleSqlConstant.UPDATE_STATEMENT);
			updateSql.append(tuple.getTableName() + OracleSqlConstant.SET_CLAUSE);
			
			int counter = 1;
			String colValue;
			for (String colName : tuple.getUpdatingColumns()) {
				Integer colType = tuple.getUpdatingColTypeMap().get(colName);
				updateSql.append(colName + OracleSqlConstant.EQUAL_SIGN);
				colValue = tuple.getUpdatingColValMap().get(colName);
				if (ConverterUtil.doesDataTypeRequireSingleQuote(colType, colName, colValue)) {
					updateSql.append(OracleSqlConstant.SINGLE_QUOTE);
					updateSql.append(colValue);
					updateSql.append(OracleSqlConstant.SINGLE_QUOTE);
				}
				else {
					updateSql.append(tuple.getUpdatingColValMap().get(colName));
				}
				updateSql.append(counter != tuple.getUpdatingColumns().size()?  OracleSqlConstant.SEPARATOR : "");
				counter++;
			}
			
			updateSql.append(OracleSqlConstant.WHERE_CLAUSE);
			
			counter = 1;
			for (String colName : tuple.getMatchingColumns()) {
				Integer colType = tuple.getMatchingColTypeMap().get(colName);
				updateSql.append(colName);
				updateSql.append(OracleSqlConstant.EQUAL_SIGN);
				colValue = tuple.getMatchingColValMap().get(colName);
				if (ConverterUtil.doesDataTypeRequireSingleQuote(colType, colName, colValue)) {
					updateSql.append(OracleSqlConstant.SINGLE_QUOTE);
					updateSql.append(colValue);
					updateSql.append(OracleSqlConstant.SINGLE_QUOTE);
				}
				else {
					updateSql.append(tuple.getMatchingColValMap().get(colName));
				}
				updateSql.append(counter != tuple.getMatchingColumns().size()?  OracleSqlConstant.AND_CLAUSE : "");
				counter++;
			}
			
			updateSql.append(OracleSqlConstant.END_OF_SQL_LINE);
			tuple.setSqlString(updateSql.toString());
			return;
		}

	},
	
	NOCHANGE(OracleSqlConstant.UserDecision.NO_CHANGE) {
		@Override
		public void generateSQL(TupleData tuple) {
			return;
		}
	};
	
	private final String decision;
	
	OracleDecision(String decision) {
		this.decision = decision;
	}

	/**
	 * @return the decision
	 */
	public String getDecision() {
		return decision;
	}
	
	public abstract void generateSQL(TupleData tuple);
	
	public boolean validateUpdateData(TupleData tuple) {
		if (!validateTableName(tuple)) {
			return false;
		}
		
		if (tuple.getUpdatingColValMap() == null || tuple.getUpdatingColValMap().isEmpty() ) {
			tuple.setErrorMessage(OracleSqlConstant.ErrorMessage.NO_COLUMN_NAME);
			return false;
		}
		
		return true;
	}

	public boolean validateTableName(TupleData tuple) {
		if (StringUtils.isBlank(tuple.getTableName())) {
			tuple.setErrorMessage(OracleSqlConstant.ErrorMessage.NO_TABLE_NAME);
			return false;
		}
		return true;
	}
	
	
	public boolean validMatchingColumnAndData(TupleData tuple) {
		if (tuple.getMatchingColValMap() == null && tuple.getMatchingColValMap().isEmpty()) {
			tuple.setErrorMessage(OracleSqlConstant.ErrorMessage.NO_MATCHING_COLUMN);
			return false;
		}
		return true;
	}
	
}
