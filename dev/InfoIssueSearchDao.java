package epm.cc.info.infoapply.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import epm.cc.core.dao.util.CommonJdbcTemplate;
import epm.cc.core.dao.util.CoreStringTool;
import epm.cc.core.dao.util.DaoConnectionManager;
import epm.cc.info.infoapply.model.InfoApply;
import epm.core.dao.BaseDao;
import epm.core.dao.DaoUtil;
import epm.core.pagelist.Pagination;

public class InfoIssueSearchDao extends BaseDao {

	/**
	 * 
	 * @param timeStart
	 * @param timeEnd
	 * @param epmNo
	 * @param pageNo
	 * @param pageSize
	 * @param infoIssueApp
	 * @return
	 */
	public Pagination getInfoIssueSearchList( int pageNo, int pageSize, InfoApply infoIssueApp,List<Map> listSubOrgNo) {
		
		String timeStart = CoreStringTool.getString(infoIssueApp.getStartDate());
		String timeEnd = CoreStringTool.getString(infoIssueApp.getEndDate());
		String epmNo = CoreStringTool.getString(infoIssueApp.getEmpNoSearch());
		String releaseModeSCH = CoreStringTool.getString(infoIssueApp.getReleaseModeSCH());
		String infoTypeCodeSCH = CoreStringTool.getString(infoIssueApp.getInfoTypeCodeSCH());
		String agoNoSCH = CoreStringTool.getString(infoIssueApp.getAgoNoSCH());
		//listSubOrgNo.remove(0);
		String releaseBeginDate = CoreStringTool.getString(infoIssueApp.getReleaseBeginDate());
		String releaseEndDate = CoreStringTool.getString(infoIssueApp.getReleaseEndDate());
		String status = CoreStringTool.getString(infoIssueApp.getStatus());
		String appNoSCH = CoreStringTool.getString(infoIssueApp.getAppNoSCH());
		
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
		
		boolean firstFlag = true;
		List listPara = new ArrayList();
		
		String 	aprOrIssueFlag =  CoreStringTool.getString(infoIssueApp.getIndutOrIssueFlag());
		StringBuffer  condnSqlStrBuf  = new StringBuffer();
//		condnSql = getCondnStrApp(timeStart,timeEnd,epmNo,releaseModeSCH,
//				infoTypeCodeSCH,agoNoSCH,appNo,orgNo);

		if(!appNoSCH.equals("")){
			if(firstFlag){
				condnSqlStrBuf.append(" where ");
				firstFlag = false;
			}else{
				condnSqlStrBuf.append(" and ");
			}
			condnSqlStrBuf.append(" appNo = ? ");
			listPara.add(appNoSCH);			
		}
		else {
			if (!timeStart.equals("")) {
				if (firstFlag) {
					condnSqlStrBuf.append(" where ");
					firstFlag = false;
				} else {
					condnSqlStrBuf.append(" and ");
				}
				condnSqlStrBuf
						.append(" appTime1 >= to_date(?,'yyyy-mm-dd hh24:mi') ");
				listPara.add(timeStart);
			}
			if (!timeEnd.equals("")) {
				if (firstFlag) {
					condnSqlStrBuf.append(" where ");
					firstFlag = false;
				} else {
					condnSqlStrBuf.append(" and ");
				}
				condnSqlStrBuf
						.append(" appTime1 <= to_date(?,'yyyy-mm-dd hh24:mi')");
				listPara.add(timeEnd);
			}
			if (!epmNo.equals("")) {
				if (firstFlag) {
					condnSqlStrBuf.append(" where ");
					firstFlag = false;
				} else {
					condnSqlStrBuf.append(" and ");
				}
				condnSqlStrBuf.append(" empNo =?");
				listPara.add(epmNo);
			} else {
				if (firstFlag) {
					condnSqlStrBuf.append(" where ");
					firstFlag = false;
				} else {
					condnSqlStrBuf.append(" and ");
				}
				if (!agoNoSCH.equals("")) {// 指定单位查找
					String[] agoNoArr = agoNoSCH.split(",");
					// condnSqlStrBuf.append(" agoNo = ? ");
					condnSqlStrBuf.append(DaoUtil.getInsqlWithlField("agoNo",
							agoNoArr, new ArrayList() {
							}));
					// listPara.add(agoNoSCH);
					for (String agoNo : agoNoArr) {
						listPara.add(agoNo);
					}
				} else {
					if (listSubOrgNo.size() == 1) {
						condnSqlStrBuf.append(" agoNo = ? ");
						listPara.add(listSubOrgNo.get(0).get("orgno"));
					} else {
						for (int i = 0; i < listSubOrgNo.size(); i++) {
							if (i == 0) {
								condnSqlStrBuf.append(" agoNo in ( ? ");
								if (i == (listSubOrgNo.size() - 1)) {
									condnSqlStrBuf.append(" )");
								}
								listPara.add(listSubOrgNo.get(i).get("orgno"));
							} else {
								if (i == listSubOrgNo.size() - 1) {
									condnSqlStrBuf.append(" ,? ) ");
									listPara.add(listSubOrgNo.get(i).get(
											"orgno"));
								} else {
									condnSqlStrBuf.append(" ,?  ");
									listPara.add(listSubOrgNo.get(i).get(
											"orgno"));
								}
							}
						}
					}
				}
			}
			if (!releaseModeSCH.equals("")) {
				if (firstFlag) {
					condnSqlStrBuf.append(" where ");
					firstFlag = false;
				} else {
					condnSqlStrBuf.append(" and ");
				}
				condnSqlStrBuf.append(" releaseMode = ? ");
				listPara.add(releaseModeSCH);
			}
			if (!infoTypeCodeSCH.equals("")) {
				if (firstFlag) {
					condnSqlStrBuf.append(" where ");
					firstFlag = false;
				} else {
					condnSqlStrBuf.append(" and ");
				}
				condnSqlStrBuf.append(" infoTypeCode = ?");
				listPara.add(infoTypeCodeSCH);
			}
			if (!releaseBeginDate.equals("")) {
				if (firstFlag) {
					condnSqlStrBuf.append(" where ");
					firstFlag = false;
				} else {
					condnSqlStrBuf.append(" and ");
				}
				condnSqlStrBuf
						.append(" (LENGTH(VALIDDATE) = 12 AND"
								+ "       TO_DATE(SUBSTR(VALIDDATE, 0, 4) || '-' || SUBSTR(VALIDDATE, 5, 2) || '-' ||"
								+ "                SUBSTR(VALIDDATE, 7, 2) || ' ' || SUBSTR(VALIDDATE, 9, 2) || ':' ||"
								+ "                SUBSTR(VALIDDATE, 11, 2),"
								+ "                'yyyy-MM-dd hh24:mi') >="
								+ "       TO_DATE(?, 'yyyy-mm-dd hh24:mi'))");
				listPara.add(releaseBeginDate);
			}
			if (!releaseEndDate.equals("")) {
				if (firstFlag) {
					condnSqlStrBuf.append(" where ");
					firstFlag = false;
				} else {
					condnSqlStrBuf.append(" and ");
				}
				condnSqlStrBuf
						.append(" (LENGTH(VALIDDATE) = 12 AND"
								+ "       TO_DATE(SUBSTR(VALIDDATE, 0, 4) || '-' || SUBSTR(VALIDDATE, 5, 2) || '-' ||"
								+ "                SUBSTR(VALIDDATE, 7, 2) || ' ' || SUBSTR(VALIDDATE, 9, 2) || ':' ||"
								+ "                SUBSTR(VALIDDATE, 11, 2),"
								+ "                'yyyy-MM-dd hh24:mi') <="
								+ "       TO_DATE(?, 'yyyy-mm-dd hh24:mi'))");
				listPara.add(releaseEndDate);
			}
			if (!status.equals("")) {
				if (firstFlag) {
					condnSqlStrBuf.append(" where ");
					firstFlag = false;
				} else {
					condnSqlStrBuf.append(" and ");
				}
				condnSqlStrBuf.append(" okFlag = ?");
				listPara.add(status);
			}
		}
		sqlCount += condnSqlStrBuf.toString();
		sql += condnSqlStrBuf.toString();
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
	 * 转换发布时效．原：月日时分　新：年-月-日 时:分
	 * @param validDate
	 * @return
	 */
	private String displayValidDate(String saveDate,String appTime1){
		String displayDate = "";
		int flag = 0;
		
		int length =saveDate.length();
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
	}


