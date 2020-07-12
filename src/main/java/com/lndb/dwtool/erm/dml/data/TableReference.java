package com.lndb.dwtool.erm.dml.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.lndb.dwtool.erm.db.TableDescriptor;

public enum TableReference {
	PERM_TMPL {
		@Override
		protected void InitializeReferences() {
			ReferenceData reference = new ReferenceData();
			initializeTypeReference(reference);
			getReferences().add(reference);
		}

		
	},
	ROLE_ATTR {
		@Override
		protected void InitializeReferences() {
			ReferenceData reference = new ReferenceData();
			initializeTypeReference(reference);
			getReferences().add(reference);
			
			reference = new ReferenceData();
			initializeAttrReference(reference);
			getReferences().add(reference);
		}

		
	},
	ROLE {
		@Override
		protected void InitializeReferences() {
//			ReferenceData reference = new ReferenceData();
//			getReferences().add(reference);
//			reference.setTargetTableName("KRIM_TYP_T");
//			reference.getExcelToTableCols().put("TYP_NMSPC_CD", "NMSPC_CD");
//			reference.getExcelToTableCols().put("TYP_NM", "NM");
//			reference.setSelectReferToColName("KIM_TYP_ID");
//			reference.setSelectReferByColName("KIM_TYP_ID");
		}
	},
	ROLE_MBR {
		@Override
		protected void InitializeReferences() {
			ReferenceData reference = new ReferenceData();
			getReferences().add(reference);
			reference.setTargetTableName("KRIM_ROLE_T");
			reference.getExcelToTableCols().put("roleNm", "ROLE_NM");
			reference.getExcelToTableCols().put("nmspcCd", "NMSPC_CD");
			reference.setSelectReferToColName("ROLE_ID");
			reference.setSelectReferByColName("MBR_ID");
			reference = new ReferenceData();
			reference.setTargetTableName("KRIM_ROLE_T");
			reference.getExcelToTableCols().put("ROLE_NM", "ROLE_NM");
			reference.getExcelToTableCols().put("NMSPC_CD", "NMSPC_CD");
			reference.setSelectReferToColName("ROLE_ID");
			reference.setSelectReferByColName("ROLE_ID");
			getReferences().add(reference);
		}
		
	},
	ROLE_PERM {
		@Override
		protected void InitializeReferences() {
			ReferenceData reference = new ReferenceData();
			getReferences().add(reference);
			reference.setTargetTableName("KRIM_PERM_T");
			reference.getExcelToTableCols().put("PERM_NM", "NM");
			reference.getExcelToTableCols().put("PERMNMSPC", "NMSPC_CD");
			reference.setSelectReferToColName("PERM_ID");
			reference.setSelectReferByColName("PERM_ID");
			reference = new ReferenceData();
			getReferences().add(reference);
			reference.setTargetTableName("KRIM_ROLE_T");
			reference.getExcelToTableCols().put("ROLE_NM", "ROLE_NM");
			reference.getExcelToTableCols().put("NMSPC", "NMSPC_CD");
			reference.setSelectReferToColName("ROLE_ID");
			reference.setSelectReferByColName("ROLE_ID");
		}
		
	},
	PERM {
		@Override
		protected void InitializeReferences() {
//			ReferenceData reference = new ReferenceData();
//			getReferences().add(reference);
//			reference.setTargetTableName("KRIM_PERM_T");
		}
		
	},
	PERM_ATTR {
		@Override
		protected void InitializeReferences() {
//			ReferenceData reference = new ReferenceData();
//			getReferences().add(reference);
//			reference.setTargetTableName("KRIM_PERM_T");
//			reference.getExcelToTableCols().put("NM", "NM");
//			reference.getExcelToTableCols().put("NMSPC_CD", "NMSPC_CD");
//			reference.setSelectReferToColName("PERM_ID");
//			reference.setSelectReferByColName("PERM_ID");
		}
		
	},
	RSP {
		@Override
		protected void InitializeReferences() {
//			ReferenceData reference = new ReferenceData();
//			getReferences().add(reference);
//			reference.setTargetTableName("KRIM_ROLE_T");
//			reference.getExcelToTableCols().put("NMSPC_CD","NMSPC_CD");
//			reference.getExcelToTableCols().put("ROLE_NM","ROLE_NM");
//			reference.setSelectReferToColName("ROLE_ID");
//			reference.setSelectReferByColName("ROLE_ID");
		}
	},
	ROLE_RSP {
		@Override
		protected void InitializeReferences() {
			ReferenceData reference = new ReferenceData();
			getReferences().add(reference);
			reference.setTargetTableName("KRIM_ROLE_T");
			reference.getExcelToTableCols().put("ROLE_NM", "ROLE_NM");
			reference.getExcelToTableCols().put("NMSPC_CD", "NMSPC_CD");
			reference.setSelectReferToColName("ROLE_ID");
			reference.setSelectReferByColName("ROLE_ID");
		}
	},
	ROLE_RSP_ACT {
		@Override
		protected void InitializeReferences() {
//			ReferenceData reference = new ReferenceData();
//			getReferences().add(reference);
//			reference.setTargetTableName("KRIM_ROLE_RSP_T");
//			reference.getExcelToTableCols().put("ROLE_RSP_ID","ROLE_RSP_ID");
//			reference.setSelectReferToColName("ROLE_RSP_ID");
//			reference.setSelectReferByColName("ROLE_RSP_ID");
		}
		
	};
	
	private TableReference() {
		references = new ArrayList<TableReference.ReferenceData>();
		InitializeReferences();
	}
	
	private List<ReferenceData> references;
	
	protected abstract void InitializeReferences();
	
	private static void initializeRoleReference(ReferenceData reference) {
		reference.setTargetTableName("KRIM_ROLE_T");
		reference.getExcelToTableCols().put("NAME", "ROLE_NM");
		reference.getExcelToTableCols().put("NMSPC_Cd", "NMSPC_CD");
		reference.setSelectReferToColName("ROLE_ID");
		reference.setSelectReferByColName("ROLE_ID");
	}
	
	private static void initializeTypeReference(ReferenceData reference) {
		reference.setTargetTableName("KRIM_TYP_T");
		reference.getExcelToTableCols().put("TYP_NMSPC", "NMSPC_CD");
		reference.getExcelToTableCols().put("TYP_NM", "NM");
		reference.setSelectReferToColName("KIM_TYP_ID");
		reference.setSelectReferByColName("KIM_TYP_ID");
	}
	
	private static void initializeAttrReference(ReferenceData reference) {
		reference.setTargetTableName("KRIM_ATTR_DEFN_T");
		reference.getExcelToTableCols().put("ATTR_NMSPC", "NMSPC_CD");
		reference.getExcelToTableCols().put("ATTR_NM", "NM");
		reference.setSelectReferToColName("KIM_ATTR_DEFN_ID");
		reference.setSelectReferByColName("KIM_ATTR_DEFN_ID");
	}
	
	/**
	 * @return the references
	 */
	public List<ReferenceData> getReferences() {
		return references;
	}

	/**
	 * @param references the references to set
	 */
	public void setReferences(List<ReferenceData> references) {
		this.references = references;
	}

	public class ReferenceData {
		// table col name -> Excel col name
		private Map<String, String> excelToTableCols;
		// table col name -> value
		private Map<String, String> searchingCriterias;
		private String selectReferToColName;
		private String selectReferByColName;
		
		// source table col -> source table value
		private Map<String, String> sourceToTargetCols;
		private Map<String, String> sourceTableResults;
		private List<String> sourceTableCols;
		private String targetTableName;
		
		public ReferenceData() {
			excelToTableCols = new HashMap<String, String>();
			searchingCriterias = new HashMap<String, String>();
			sourceTableResults = new HashMap<String, String>();
			sourceToTargetCols = new HashMap<String, String>();
			sourceTableCols = new ArrayList<String>();
		}

		
		
		/**
		 * @return the selectReferByColName
		 */
		public String getSelectReferByColName() {
			return selectReferByColName;
		}



		/**
		 * @param selectReferByColName the selectReferByColName to set
		 */
		public void setSelectReferByColName(String selectReferByColName) {
			this.selectReferByColName = selectReferByColName;
		}



		/**
		 * @return the sourceToTargetCols
		 */
		public Map<String, String> getSourceToTargetCols() {
			return sourceToTargetCols;
		}


		/**
		 * @param sourceToTargetCols the sourceToTargetCols to set
		 */
		public void setSourceToTargetCols(Map<String, String> sourceToTargetCols) {
			this.sourceToTargetCols = sourceToTargetCols;
		}


		/**
		 * @return the excelToTableCols
		 */
		public Map<String, String> getExcelToTableCols() {
			return excelToTableCols;
		}

		/**
		 * @param excelToTableCols the excelToTableCols to set
		 */
		public void setExcelToTableCols(Map<String, String> excelToTableCols) {
			this.excelToTableCols = excelToTableCols;
		}

		/**
		 * @return the searchingCriterias
		 */
		public Map<String, String> getSearchingCriterias() {
			return searchingCriterias;
		}

		/**
		 * @param searchingCriterias the searchingCriterias to set
		 */
		public void setSearchingCriterias(Map<String, String> searchingCriterias) {
			this.searchingCriterias = searchingCriterias;
		}


		/**
		 * @return the selectReferToColName
		 */
		public String getSelectReferToColName() {
			return selectReferToColName;
		}


		/**
		 * @param selectReferToColName the selectReferToColName to set
		 */
		public void setSelectReferToColName(String selectReferToColName) {
			this.selectReferToColName = selectReferToColName;
		}

		/**
		 * @return the sourceTableResults
		 */
		public Map<String, String> getSourceTableResults() {
			return sourceTableResults;
		}

		/**
		 * @param sourceTableResults the sourceTableResults to set
		 */
		public void setSourceTableResults(Map<String, String> sourceTableResults) {
			this.sourceTableResults = sourceTableResults;
		}

		/**
		 * @return the sourceTableCols
		 */
		public List<String> getSourceTableCols() {
			return sourceTableCols;
		}

		/**
		 * @param sourceTableCols the sourceTableCols to set
		 */
		public void setSourceTableCols(List<String> sourceTableCols) {
			this.sourceTableCols = sourceTableCols;
		}

		/**
		 * @return the targetTableName
		 */
		public String getTargetTableName() {
			return targetTableName;
		}

		/**
		 * @param targetTableName the targetTableName to set
		 */
		public void setTargetTableName(String targetTableName) {
			this.targetTableName = targetTableName;
		}

	}
}
