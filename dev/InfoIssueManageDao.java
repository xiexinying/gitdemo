package epm.cc.info.infoapply.dao;

import java.util.ArrayList;
import java.util.List;

import epm.cc.core.dao.util.CommonJdbcTemplate;
import epm.cc.core.dao.util.CoreConstant;
import epm.cc.core.dao.util.CoreDateTime;
import epm.cc.core.dao.util.CoreStringTool;
import epm.cc.core.dao.util.DaoConnectionManager;
import epm.cc.core.dao.util.DaoSequenceUtil;
import epm.cc.info.infoapply.model.InfoApply;
import epm.cc.info.infoauditing.model.InfoAuditing;
import epm.core.dao.BaseDao;
import epm.core.dao.DaoUtil;
import epm.core.exception.EPMRuntimeException;
import epm.core.model.SysUserInfo;
import epm.core.pagelist.Pagination;
/**
 * 信息发布
 * @author LinPeixun
 *
 */
public class InfoIssueManageDao extends BaseDao {

	/**
	 * 信息发布申请/信息发布 共用
	 * 
	 * @param timeStart
	 * @param timeEnd
	 * @param epmNo
	 * @param pageNo
	 * @param pageSize
	 * @param infoIssueApp
	 * @return
	 */
	public Pagination getInfoIssueApps( int pageNo, int pageSize, InfoApply infoIssueApp) {
		
		String timeStart = CoreStringTool.getString(infoIssueApp.getStartDate());
		String timeEnd = CoreStringTool.getString(infoIssueApp.getEndDate());
		String epmNo = CoreStringTool.getString(infoIssueApp.getEmpNoSearch());
		String releaseModeSCH = CoreStringTool.getString(infoIssueApp.getReleaseModeSCH());
		String infoTypeCodeSCH = CoreStringTool.getString(infoIssueApp.getInfoTypeCodeSCH());
		String agoNoSCH = CoreStringTool.getString(infoIssueApp.getAgoNoSCH());
		String telNoSCH = CoreStringTool.getString(infoIssueApp.getTelNoSCH());
		String appNo = CoreStringTool.getString(infoIssueApp.getAppNo());//
		
		SysUserInfo sysUserInfo = infoIssueApp.getUserInfo();
		String orgNo = "";
		if(sysUserInfo != null){
			orgNo = CoreStringTool.getString(sysUserInfo.getOrgNo());
		}

		List listPara = new ArrayList();
		if (!timeStart.equals("")) {
			listPara.add(timeStart);
		}
		if (!timeEnd.equals("") ) {
			listPara.add(timeEnd);
		}
		if (!telNoSCH.equals("") ) {
			listPara.add("%"+telNoSCH+"%");
		}
		if (!epmNo.equals("")) {
			listPara.add(epmNo);
		}
		if (!releaseModeSCH.equals("")) {
			listPara.add(releaseModeSCH);
		}
		if (!infoTypeCodeSCH.equals("")) {
			listPara.add(infoTypeCodeSCH);
		}
		
		//查询条件没有 申请单位，默认登陆人员所在供电单位
		if (!agoNoSCH.equals("")) {
			listPara.add(agoNoSCH);
		}else{
			if (!orgNo.equals("")) {
				listPara.add(orgNo);
			}
		}
		if (!appNo.equals("")) {
			listPara.add(appNo);
		}

//		Object[] params = new Object[count];
		
		String sqlCount = "select count(*) from ( " +
				" select a.app_no as appNo ,a.emp_no as empNo,a.tel_no as telNo ,a.release_date as releaseDate," +
				" a.app_time as appTime1, a.release_mode as releaseMode, " +
				" a.valid_date as validDate,a.overdue_reason as overDueReason," +
				" a.info_type_code as infoTypeCode,a.ok_flag as okFlag ,a.proc_ins_id as procInsId, " +
				" a.issue_Obj as issueObj,a.ago_no as agoNo," +
				" b.content as content,b.emp_no as empNoIssue,b.release_date as realseDate " +
				" FROM S_INFO_ISSUE_APP a left join s_issue_info b on a.app_no = b.app_no ) ";

		String sqlConMain = " m.appNo as appNo ,m.empNo as empNo,m.telNo as telNo ," +
				" m.releaseDate as releaseDate,  to_char(m.appTime1,'yyyy-mm-dd hh24:mi') " +
				" as appTime,  m.releaseMode as releaseMode,  m.validDate as validDate," +
				" m.overDueReason as overDueReason, m.infoTypeCode as  infoTypeCode," +
				" m.okFlag as okFlag ,m.agoNo as agoNo,m.procInsId as procInsId,m.issueObj as issueObj," +
				" m.realseDate as realseDate," +
//				" m.content as content," +
				"m.empNoIssue as empNoIssue ";

		String sql = "select "
				+ sqlConMain
				+ " from ( "
				+ " select a.app_no as appNo ,a.emp_no as empNo,a.tel_no as telNo ,a.release_date as releaseDate, "
				+ " a.app_time as appTime1, a.release_mode as releaseMode, a.valid_date as validDate,"
				+ " a.overdue_reason as overDueReason, a.info_type_code as infoTypeCode,a.ok_flag as okFlag ,"
				+ " a.proc_ins_id as procInsId,a.ago_no as agoNo,a.issue_Obj as issueObj," +
						" b.content as content,b.emp_no as empNoIssue," +
						" b.release_date as realseDate  "
				+ " FROM S_INFO_ISSUE_APP a left join s_issue_info b on a.app_no = b.app_no ) m ";

		String sqlOrder = " order by m.appTime1 desc";

//		String sFlag = "1";
		//组合条件:信息申请
		
		String 	aprOrIssueFlag = CoreStringTool.getString(infoIssueApp.getIndutOrIssueFlag());

		String condnSql = getCondnStrApp(timeStart,timeEnd,telNoSCH,epmNo,releaseModeSCH,
				infoTypeCodeSCH,aprOrIssueFlag,agoNoSCH,appNo,orgNo);

		sqlCount += condnSql;
		sql += condnSql;
		sql += sqlOrder;
		
		Object[] params = listPara.toArray();

		Pagination pagination = CommonJdbcTemplate.getPagination(
				DaoConnectionManager.getCCConnName(), sqlCount,
				sql, params, pageNo, pageSize, InfoApply.class);
		
		List<InfoApply> list = pagination.getList();
		for(int i=0;i<list.size();i++){
			String saveDate = list.get(i).getValidDate();
			String appDate = list.get(i).getAppTime();
			String displayDate =  displayValidDate(saveDate,appDate);
			
			list.get(i).setValidDate(displayDate);
		}
		pagination.setList(list);
		
		return pagination;
	}
	
	/**
	 * 转换发布时效．原：年-月-日 时:分　新：月日时分
	 * @param validDate
	 * @return
	 */
	private String saveValidDate(String displayDate){
		String saveDate = "";
		saveDate += displayDate.substring(0, 4); 
		saveDate += displayDate.substring(4, 6);  
		saveDate += displayDate.substring(6, 8);  
		saveDate += displayDate.substring(9, 11);
		saveDate += displayDate.substring(12);  
		return saveDate;
	}
	
	/**
	 * 转换发布时效．原：月日时分　新：年-月-日 时:分
	 * @param validDate
	 * @return
	 */
	private String displayValidDate(String saveDate,String appTime1){
		String displayDate = "";
		int flag = 0;
		
		int length = CoreStringTool.getString(saveDate).length();
        try {
        	if(length<12){
	        	displayDate = appTime1.substring(0,4);
	        	displayDate += "-";
				displayDate += saveDate.substring(0,2);
				displayDate += "-";
				displayDate += saveDate.substring(2,4);
				displayDate += " ";
				displayDate += saveDate.substring(4,6);
				displayDate += ":";
				displayDate += saveDate.substring(6);
				
				flag = displayDate.compareTo(appTime1);
				
				if(flag<0){
					displayDate = displayDate.replace(displayDate.substring(0,4),String.valueOf((Integer.parseInt(appTime1.substring(0,4))+1)));
				}
        	}else{
        		displayDate = saveDate.substring(0,4);
	        	displayDate += "-";
				displayDate += saveDate.substring(4,6);
				displayDate += "-";
				displayDate += saveDate.substring(6,8);
				displayDate += " ";
				displayDate += saveDate.substring(8,10);
				displayDate += ":";
				displayDate += saveDate.substring(10,12);
        	}
		} catch (NumberFormatException e) {
			// TODO 给显示的时间格式中，添加年份
			e.printStackTrace();
		}
		
		return displayDate;
	}

	/**
	 * 根据信息发布申请记录appNo查询有关内容
	 * 
	 * @param appNo
	 * @return
	 */
	public InfoApply getContentByAppNo(String appNo) {
		if (appNo != null && !"".equals(appNo)){
		String sql = "select a.APP_NO as appNo,a.CONTENT,a. content from S_ISSUE_INFO a where a.APP_NO = ?";
		return (InfoApply) DaoUtil.queryForBean(DaoConnectionManager
				.getCCConnName(), sql, new Object[] { appNo }, InfoApply.class);
		}else{
			return (new InfoApply());
		}
		
	}
	/**
	 * empty
	 * @param infoIssueApp
	 * @return
	 */
	public InfoApply getInfoIssueApp(InfoApply infoIssueApp) {
		return null;
	}

	
	/**
	 * 信息发布 修改
	 * @param infoApply
	 */
	public void editInfoIssueApp(InfoApply infoApply) {
		
		String connName = DaoConnectionManager.getCCConnName();
		String appTime = new CoreDateTime().getNowDateTime();
		//修改发布申请
		String sql = "update S_INFO_ISSUE_APP set emp_no = ? ,tel_no =?, release_date=?,"
				+ " app_time = to_date(?,'yyyy-mm-dd hh24:mi'), release_mode = ?, valid_date = ?,overdue_reason = ?,"
				+ " info_type_code = ?, ok_flag = ?,"
				+ " proc_ins_id = ?,issue_Obj= ?,ago_no =?  where app_no =?";
		
		String displayDate = infoApply.getValidDateDTL();
		String saveDate = saveValidDate(displayDate);
		
		Object[] params = new Object[] { infoApply.getEmpNo(),
				infoApply.getTelNoDTL(), infoApply.getReleaseDateDTL(),
				appTime.substring(0,appTime.length()-3),
				infoApply.getReleaseModeDTL(), saveDate,
				infoApply.getOverDueReasonDTL(),
				infoApply.getInfoTypeCodeDTL(),
				CoreStringTool.getString(infoApply.getOkFlagDTL()), //CoreConstant.LIBCOL_ASSESSFLAG_APP
				infoApply.getProcInsId(),
				infoApply.getIssueObj(), infoApply.getAgoNo(),
				infoApply.getAppNoHid()
				};
		
		DaoUtil.executeUpdate(connName,sql,params);
		
//		//修改内容
//		String sql2 = "Update S_ISSUE_INFO t Set t.content =? Where t.id = ?";
//		Object[] params2 = new Object[]{infoApply.getContentDTL(),infoApply.getAppNo()};
//		DaoUtil.executeUpdate(connName,sql2, params2);

	}
	
	
/**
 * 信息发布申请 保存
 * @param infoApply
 * @throws Exception
 */
	public String saveInfoIssueApp(InfoApply infoApply) throws Exception {

		String sequence = DaoSequenceUtil.getWorkflowAppNo();
		String epmNo = infoApply.getEmpNo();
		String orgNoLogin = infoApply.getOrgNoLogin();
		String appTime = new CoreDateTime().getNowDateTime();
		String okFlag = CoreStringTool.getString(infoApply.getOkFlag());
        //发布申请CoreConstant.OKFLAG_FALSE_CODE
		String sql = "insert into S_INFO_ISSUE_APP(" +
				"app_no,emp_no,tel_no,release_date,app_time,release_mode,valid_date,overdue_reason," +
				"info_type_code,ok_flag,proc_ins_id,issue_obj,ago_no " +
				") values(?,?,?,?,to_date(?,'yyyy-mm-dd hh24:mi'),?,?,?,?,?,?,?,?)";
		
		String displayDate = infoApply.getValidDateDTL();
		String saveDate = saveValidDate(displayDate);
			
		Object[] params = new Object[] { sequence, epmNo,
				infoApply.getTelNoDTL(), infoApply.getReleaseDateDTL(),
				appTime.substring(0,appTime.length()-3),
				infoApply.getReleaseModeDTL(), saveDate,
				infoApply.getOverDueReasonDTL(),
				infoApply.getInfoTypeCodeDTL(), okFlag,
				infoApply.getProcInsId(), infoApply.getIssueObj(),
				infoApply.getAgoNo() 
				};
		
	        	DaoUtil.executeUpdate(DaoConnectionManager.getCCConnName(),
				sql, params);
		// 内容
		String sql2 = "insert into S_ISSUE_INFO(id,app_no) values(?,?)";
		Object[] params2 = new Object[] {sequence,sequence};
		DaoUtil.executeUpdate(DaoConnectionManager.getCCConnName(),
				sql2, params2);
		
		return sequence;
	}

/**
 * 删除信息发布申请
 * @param appNoArr
 * @param infoIssueApp
 */
	public void removeInfoIssueApp(String[] appNoArr, InfoApply infoIssueApp) {

		String sql = "delete  from s_info_issue_app ";
		String delIdStr = getDelIdStr(appNoArr);
		sql += delIdStr;

		DaoUtil.executeUpdate(DaoConnectionManager.getCCConnName(),
				sql, appNoArr);

	}
/**
 * 删除发布信息
 * @param appNoArr
 * @param infoIssueApp
 */	
	public void removeInfoIssueInfo(String[] appNoArr, InfoApply infoIssueApp) {

		String sql = "delete  from S_ISSUE_INFO ";
		String delIdStr = getDelIdStr(appNoArr);
		sql += delIdStr;

		DaoUtil.executeUpdate(DaoConnectionManager.getCCConnName(),
				sql, appNoArr);

	}
	
/**
 * 根据参数组合查询条件:"--信息申请--"
 * @param releaseMode
 * @param timeStart
 * @param timeEnd
 * @param epmNo
 * @param flag
 * @return  所有申请信息,没特别限制条件的条件组合
 */
	public String getCondnStrApp(String timeStart, String timeEnd, String telNoSCH, String epmNo,String releaseModeSCH,
			String infoTypeCodeSCH,String aprOrIssueFlag,String agoNo,String appNo,String orgNo) {
		
		timeStart = CoreStringTool.getString(timeStart);
		timeEnd = CoreStringTool.getString(timeEnd);
		releaseModeSCH = CoreStringTool.getString(releaseModeSCH);
		
		StringBuffer strBuff = new StringBuffer();
		String sFlag = "0";

		if (!epmNo.equals("") || !timeStart.equals("") || !timeEnd.equals("") || !"".equals(telNoSCH)
				|| !"".equals(releaseModeSCH) || !"".equals(infoTypeCodeSCH) || !"".equals(agoNo)
				|| !"".equals(appNo) || !"".equals(orgNo)) {

			strBuff.append(" where ");

			//查询开始时间
			if (!timeStart.equals("")) {
				if (sFlag.equals("0")) {
					strBuff.append(" ");
					sFlag = "1";
				} else {
					strBuff.append(" and ");
				}
					strBuff.append(" appTime1 >= to_date(?,'yyyy-mm-dd hh24:mi')");
			}
			//查询结束时间
			if (!timeEnd.equals("")) {
				if (sFlag.equals("0")) {
					strBuff.append(" ");
					sFlag = "1";
				} else {
					strBuff.append(" and ");
				}
					strBuff.append(" appTime1 <= to_date(?,'yyyy-mm-dd hh24:mi')");
			}
			//查询结束时间
			if (!telNoSCH.equals("")) {
				if (sFlag.equals("0")) {
					strBuff.append(" ");
					sFlag = "1";
				} else {
					strBuff.append(" and ");
				}
					strBuff.append(" telNo like ? ");
			}
            //发布人员
			if (!epmNo.equals("")) {
				if (sFlag.equals("0")) {
					strBuff.append(" ");
					sFlag = "1";
				} else {
					strBuff.append(" and ");
				}
					strBuff.append(" empNo =?");
			}
			
			if (!"".equals(releaseModeSCH)) {
				if (sFlag.equals("0")) {
					strBuff.append(" ");
					sFlag = "1";
				} else {
					strBuff.append(" and ");
				}
					strBuff.append(" releaseMode = ?");
			}
			
			if (!"".equals(infoTypeCodeSCH)) {
				if (sFlag.equals("0")) {
					strBuff.append(" ");
					sFlag = "1";
				} else {
					strBuff.append(" and ");
				}
					strBuff.append(" infoTypeCode = ?");
			}
			
			if (!"".equals(agoNo)) {
				if (sFlag.equals("0")) {
					strBuff.append(" ");
					sFlag = "1";
				} else {
					strBuff.append(" and ");
				}
					strBuff.append(" agoNo = ?");
			}else{
				if (!"".equals(orgNo)) {
					if (sFlag.equals("0")) {
						strBuff.append(" ");
						sFlag = "1";
					} else {
						strBuff.append(" and ");
					}
						strBuff.append(" agoNo = ?");
				}
			}
			
			if (!"".equals(appNo)) {
				if (sFlag.equals("0")) {
					strBuff.append(" ");
					sFlag = "1";
				} else {
					strBuff.append(" and ");
				}
					strBuff.append(" appNo = ?");
			}
			
			//查询条件不为空:判断审核或发布
			if(!"".equals(aprOrIssueFlag)){
				if(aprOrIssueFlag.equals("indut")){//审核步骤:获取okFlag为null的未审核信息
						if (sFlag.equals("0")) {
							strBuff.append(" ");
							sFlag = "1";
						} else {
							strBuff.append(" and ");
						}
							strBuff.append(" okFlag = '"+CoreConstant.LIBCOL_ASSESSFLAG_APP+"'");
				}else{
					if(aprOrIssueFlag.equals("issue")){//发布步骤:根据"发布人员"是否为空过滤结果
						if (sFlag.equals("0")) {
							strBuff.append(" ");
							sFlag = "1";
						} else {
							strBuff.append(" and ");
						}
						strBuff.append(" okFlag = '"+CoreConstant.LIBCOL_ASSESSFLAG_PASS+"' ");
					}
				}
			}else{
				if (sFlag.equals("0")) {
					strBuff.append(" ");
					sFlag = "1";
				} else {
					strBuff.append(" and ");
				}
				strBuff.append(" (okFlag = '"+CoreConstant.LIBCOL_ASSESSFLAG_APP+"' or okFlag = '"+CoreConstant.LIBCOL_ASSESSFLAG_DISPASS+"') ");
			}	
			
		}else{//所有查询条件为空,判断为 审核 或 发布
			strBuff.append(" where ");
			if(!"".equals(aprOrIssueFlag)){
				if(aprOrIssueFlag.equals("indut")){//审核
					strBuff.append(" okFlag = '"+CoreConstant.LIBCOL_ASSESSFLAG_APP+"'");
				}else{//发布
						strBuff.append(" okFlag= '"+CoreConstant.LIBCOL_ASSESSFLAG_PASS+"'");
				}
			}else{
				strBuff.append(" (okFlag = '"+CoreConstant.LIBCOL_ASSESSFLAG_APP+"' or okFlag = '"+CoreConstant.LIBCOL_ASSESSFLAG_DISPASS+"') ");
			}
			
		}

		return strBuff.toString();
	}

/**
 * 组合 删除组合条件
 * @param appNoArr
 * @return
 */
	public String getDelIdStr(String[] appNoArr) {

		StringBuffer strBuff = new StringBuffer();
		int sLength = appNoArr.length;
		if (sLength > 0) {
			strBuff.append(" where app_no = ? ");
			for (int i = 2; i <= sLength; i++) {
				strBuff.append(" or app_no = ? ");
			}
		}
		return strBuff.toString();
	}
	
	/**
	 * 信息发布审核 修改审核状态为“已发布”
	 * 
	 * @param infoIssueApp
	 * @throws Exception 
	 */

	public void chgInfoIssueAppFlag(InfoAuditing infoAuditing) throws Exception {

		String sql = "update S_INFO_ISSUE_APP set ok_flag = ? where app_no =?";
		Object[] params = new Object[] { infoAuditing.getOkFlag(),
				infoAuditing.getAppNo() };
		DaoUtil.executeUpdate(DaoConnectionManager.getCCConnName(),
				sql, params);
		
		//工作流:审核
//		workflowInduting(okFlag,infoAuditing.getUserInfo(),infoAuditing.getAppNo());
	}
	

/**
 * 信息发布操作
 * @param infoIssue
 */
    public void doIssueDeal(InfoAuditing infoAuditing){
    	 String appNo = infoAuditing.getAppNo();//appNo=id
		 CoreDateTime coreDareTime = new CoreDateTime();

		 int month = coreDareTime.getNowMonthOnly();
		 int date  = coreDareTime.getNowDateOnly();
		 
		 String empNoLogin = infoAuditing.getUserInfo().getSysUserName();
		 String orgNoLogin = infoAuditing.getUserInfo().getOrgNo();
		 
		 String sMonth = month<10?("0"+String.valueOf(month)):(String.valueOf(month));
		 String sDate  = date<10?("0"+String.valueOf(date)):(String.valueOf(date));

    	String dateTime = String.valueOf(coreDareTime.getNowYearOnly())+""+sMonth+""+sDate;

    	if(appNo != null && !"".equals(appNo)){
    		String sql = "Update s_issue_info t set t.release_date = ?,t.emp_no = ?" +
    				",t.dept_no=?,t.valid_flag = ? Where t.id = ?";
    		Object[] params = new Object[]{dateTime,empNoLogin,orgNoLogin,"true",appNo};
    		
    		DaoUtil.executeUpdate(DaoConnectionManager.getCCConnName(), sql, params);

    	}
    }
    

    /**
     * 修改 收集信息的 审核标志
     * @param infoIssue
     */
        public void uptInfoOkFlag(String okFlag,String infoId){
        	
        	String sql = " update S_INFO_ISSUE_APP set ok_Flag = ? where app_no = ?";
        	Object[] params = new Object[]{okFlag,infoId};
        	DaoUtil.executeUpdate(DaoConnectionManager.getCCConnName(), sql, params);
        	
        }

	
	/**
	 * 发布超期原因 添加发布超期原因
	 * 
	 * @param infoIssue
	 */
	public void addDelayReason(InfoAuditing infoAuditing) {

		String sql = "update S_INFO_ISSUE_APP set overdue_reason = ? where app_no =?";
		Object[] params = new Object[] { infoAuditing.getOverDueReason(),
				infoAuditing.getAppNoHID()};

		DaoUtil.executeUpdate(DaoConnectionManager.getCCConnName(),
				sql, params);

	}
	/**
	 * empty
	 * @return
	 */
	public List getInfoListForIndex(){
		
		List list = new ArrayList();
		String sql = "";
		
		
		return list;
	}
	
	/**
	 * 根据appno获取appno,content等所有信息（需要字段再拓展）表的详细信息
	 * 
	 * @param appNo
	 * @return
	 */
	public InfoApply getInfoDetailByAppNo(String appNo) {
		
		InfoApply sInfoApply = new InfoApply();
		if (appNo != null && !"".equals(appNo)){
		String sql = "select a.APP_NO as appNo,a.CONTENT content,b.ok_flag okFlag  " +
				" from S_ISSUE_INFO a,S_INFO_ISSUE_APP b " +
				" where a.APP_NO = b.APP_NO and a.app_no = ?";
		
		sInfoApply = (InfoApply) DaoUtil.queryForBean(DaoConnectionManager
				.getCCConnName(), sql, new Object[] { appNo }, InfoApply.class);
		}
		
		return sInfoApply;
		
	}
	/**
	 * 停用申请发布的信息
	 * @param infoAuditing
	 * @throws EPMRuntimeException
	 */
    public int stopInfoApply(String appNo){
    	String sql="update S_INFO_ISSUE_APP set ok_flag='05' where app_no = ?";
    	return DaoUtil.executeUpdate(DaoConnectionManager.getCCConnName(),sql, new Object[]{appNo});
    }
	

}
