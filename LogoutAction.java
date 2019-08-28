package com.internousdev.florida.action;

import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;

import com.internousdev.florida.dao.UserInfoDAO;
import com.opensymphony.xwork2.ActionSupport;

public class LogoutAction extends ActionSupport implements SessionAware{

	private Map<String,Object> session;

	public String execute(){
		UserInfoDAO userInfoDAO=new UserInfoDAO();
		String userId=String.valueOf(session.get("userId"));
		boolean savedUserIdFlag=Boolean.valueOf(String.valueOf(session.get("savedUserIdFlag")));
		int count=userInfoDAO.logout(userId);
		//logoutメソッドが正しく実行できた場合
		if(count>0){
			session.clear();
			//ユーザーID保存にチェックを入れていた場合
			if(savedUserIdFlag){
				session.put("savedUserIdFlag", true);
				session.put("userId", userId);
			}
		}
		return SUCCESS;
	}

	public Map<String, Object> getSession(){
		return session;
	}

	public void setSession(Map<String, Object> session){
		this.session=session;
	}
}
