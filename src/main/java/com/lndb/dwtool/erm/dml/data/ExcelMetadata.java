package com.lndb.dwtool.erm.dml.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.lndb.dwtool.erm.db.TableDescriptor;

public enum ExcelMetadata {
	PERM_TMPL {
		@Override
		protected void Initialize() {
			setTableName("KRIM_PERM_TMPL_T");
		}

		
	},
	ROLE_ATTR {
		@Override
		protected void Initialize() {
			setTableName("KRIM_TYP_ATTR_T");
			getTableColMap().put("SORT_CD", "ATTR_SORT");
		}

		
	},
	ROLE {
		@Override
		protected void Initialize() {
			setTableName("KRIM_ROLE_T");
//			getTableColMap().put("ROLE_NM", "NAME");
//			getTableColMap().put("ROLE_ID", "ROLEID");
//			getTableColMap().put("DESC_TXT", "DESC");
		}
	},
	ROLE_MBR {
		@Override
		protected void Initialize() {
			setTableName("KRIM_ROLE_MBR_T");
			getColToPropertyMap().put("ROLE_MBR_ID", "roleMemberId");
			getColToPropertyMap().put("ROLE_ID", "roleId");
			getColToPropertyMap().put("MBR_ID", "memberId");
			getColToPropertyMap().put("MBR_TYP_CD", "memberTypeCode");
			getColToPropertyMap().put("ACTV_FRM_DT", "activeFromDate");
			getColToPropertyMap().put("ACTV_TO_DT", "activeToDate");
			getColToPropertyMap().put("LAST_UPDT_DT", "lastUpdateDate");
		}
	},
	PERM {
		@Override
		protected void Initialize() {
			setTableName("KRIM_PERM_T");
			// if excel name not matching table column name, put table col name->excel col name mapping here
//			getTableColMap().put("NMSPC_CD", "PERMNMSPC");
//			getTableColMap().put("NM", "PERM_NM");
//			getTableColMap().put("PERM_TMPL_ID", "PERMTMPLID");
//			getColToPropertyMap().put("PERM_ID", "permId");
//			getColToPropertyMap().put("PERM_TMPL_ID", "permTmplId");
//			getColToPropertyMap().put("NMSPC_CD", "namespaceCd");
//			getColToPropertyMap().put("NM", "name");
//			getColToPropertyMap().put("DESC_TXT", "description");
//			getColToPropertyMap().put("ACTV_IND", "activeInd");
		}
	},
	ROLE_PERM {
		@Override
		protected void Initialize() {
			setTableName("KRIM_ROLE_PERM_T");
			getColToPropertyMap().put("ROLE_PERM_ID", "rolePermId");
			getColToPropertyMap().put("ROLE_ID", "roleId");
			getColToPropertyMap().put("PERM_ID", "permId");
			getColToPropertyMap().put("ACTV_IND", "activeInd");
		}
	},
	PERM_ATTR {
		@Override
		protected void Initialize() {
			setTableName("KRIM_PERM_ATTR_DATA_T");
			getColToPropertyMap().put("ATTR_DATA_ID", "permAttributeId");
			getColToPropertyMap().put("ATTR_VAL", "value");
			getColToPropertyMap().put("KIM_TYP_ID", "typeId");
			getColToPropertyMap().put("KIM_ATTR_DEFN_ID", "attributeDefnId");
			getColToPropertyMap().put("PERM_ID", "permId");
		}
	},
	RSP {
		@Override
		protected void Initialize() {
			setTableName("KRIM_RSP_T");
//			getTableColMap().put("NMSPC_CD", "RSPNMSPC");
//			getTableColMap().put("RSP_TMPL_ID", "RSPTMPLID");
		}
	},
	RSP_ATTR {
		@Override
		protected void Initialize() {
			setTableName("KRIM_RSP_ATTR_DATA_T");
			getColToPropertyMap().put("ATTR_DATA_ID", "rspAttributeId");
			getColToPropertyMap().put("RSP_ID", "rspId");
			getColToPropertyMap().put("KIM_TYP_ID", "typeId");
			getColToPropertyMap().put("KIM_ATTR_DEFN_ID", "attributeDefnId");
			getColToPropertyMap().put("ATTR_VAL", "value");
		}
	},
	ROLE_RSP {
		@Override
		protected void Initialize() {
			setTableName("KRIM_ROLE_RSP_T");
//			getColToPropertyMap().put("ROLE_RSP_ID", "roleRspId");
//			getColToPropertyMap().put("RSP_ID", "rspId");
//			getColToPropertyMap().put("ROLE_ID", "roleId");
//			getColToPropertyMap().put("ACTV_IND", "activeInd");
		}
	},
	ROLE_RSP_ACT {
		@Override
		protected void Initialize() {
			setTableName("KRIM_ROLE_RSP_ACTN_T");
//			getTableColMap().put("ROLE_RSP_ACTN_ID", "ACTN_ID");
		}
	};
	
	private ExcelMetadata() {
		tableColMap = new HashMap<String, String>();
		colToPropertyMap = new HashMap<String, String>();
		Initialize();
	}
	
	private String tableName;
	// map table column name TO excel column name 
	private Map<String, String> tableColMap;
	// map table column name to java object property
	private Map<String, String> colToPropertyMap;
	
	protected abstract void Initialize();

	/**
	 * @return the tagetTableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tagetTableName the tagetTableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return the tableColMap
	 */
	public Map<String, String> getTableColMap() {
		return tableColMap;
	}

	/**
	 * @param tableColMap the tableColMap to set
	 */
	public void setTableColMap(Map<String, String> tableColMap) {
		this.tableColMap = tableColMap;
	}

	/**
	 * @return the colToPropertyMap
	 */
	public Map<String, String> getColToPropertyMap() {
		return colToPropertyMap;
	}

	/**
	 * @param colToPropertyMap the colToPropertyMap to set
	 */
	public void setColToPropertyMap(Map<String, String> colToPropertyMap) {
		this.colToPropertyMap = colToPropertyMap;
	}
	
	
}
