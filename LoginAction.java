package com.internousdev.florida.action;

import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;

import com.internousdev.florida.dao.CartInfoDAO;
import com.internousdev.florida.dao.UserInfoDAO;
import com.internousdev.florida.dto.CartInfoDTO;
import com.internousdev.florida.dto.UserInfoDTO;
import com.internousdev.florida.util.InputChecker;
import com.opensymphony.xwork2.ActionSupport;

public class LoginAction extends ActionSupport implements SessionAware {

	private String userId;
	private String password;
	private List<String> userIdErrorMessageList;
	private List<String> passwordErrorMessageList;
	private String loginInfoErrorMessage;
	private Map<String, Object> session;
	UserInfoDAO userInfoDAO=new UserInfoDAO();
	UserInfoDTO userInfoDTO=new UserInfoDTO();
	CartInfoDAO cartInfoDAO=new CartInfoDAO();
	CartInfoDTO cartInfoDTO=new CartInfoDTO();
	private List<CartInfoDTO> cartInfo;
	private int totalPrice;
	private boolean savedUserIdFlag;

	public String execute(){

		if(!session.containsKey("tempUserId")){
			return "sessionTimeout";
		}

		//ユーザー登録画面から遷移した場合の処理
		if(session.containsKey("createUserFlag")){
			//ユーザー登録画面で入力したユーザIDとパスワードをログイン認証するために取得
			userId=session.get("userIdForCreateUser").toString();
			password=session.get("password").toString();
			//ユーザー登録機能で作成したsessionを削除
			session.remove("userIdForCreateUser");
			session.remove("password");
			session.remove("familyName");
			session.remove("firstName");
			session.remove("familyNameKana");
			session.remove("firstNameKana");
			session.remove("sex");
			session.remove("sexList");
			session.remove("email");
			session.remove("createUserFlag");
		}

		String result=ERROR;
		session.remove("savedUserIdFlag");

		//ログイン画面で入力したユーザーID,パスワードの桁数、文字種、未入力のチェック
		InputChecker inputChecker=new InputChecker();
		userIdErrorMessageList=inputChecker.doCheck("ユーザーID", userId, 1, 8, true, false, false, true, false, false);
		passwordErrorMessageList=inputChecker.doCheck("パスワード", password, 1, 16, true, false, false, true, false, false);

		//ユーザーID,パスワードの桁数、文字種、未入力の規定に反した場合、エラーメッセージをログイン画面に表示する
		if(userIdErrorMessageList.size()>0
				|| passwordErrorMessageList.size()>0){
		session.put("logined", 0);
		return result;
		}

		//ログイン認証処理
		userInfoDTO=userInfoDAO.checkUserInfo(userId, password);
		//ログイン認証に成功した場合if文の処理に分岐
		if(userInfoDTO.getCheckLoginFlg()){
			userInfoDAO.update(userId, password);

			//カート情報との紐付け処理
			String tempUserId=session.get("tempUserId").toString();
			List<CartInfoDTO> cartInfoDTOListForTempUser=cartInfoDAO.getCartInfo(tempUserId);
			//仮ユーザーIDに紐付くカート情報が存在した場合
			if(cartInfoDTOListForTempUser!=null){
				//changeCartInfoメソッドを実行し、カート情報を書き換える
				boolean cartresult=changeCartInfo(cartInfoDTOListForTempUser, tempUserId);
				//カート情報の紐付けに失敗した場合の処理
				if(!cartresult){
					return "DBError";
				}
			}

			//次に遷移する画面の決定
			//カートから遷移してきた場合
			if(session.containsKey("cartFlag")){
				session.remove("cartFlag");
				result="cart";
			//headerから遷移してきた場合
			}else{
				result=SUCCESS;
			}

			//ユーザーID、ログインフラグ、ユーザーID保存チェックの情報の保持。仮ユーザーID情報の削除
			UserInfoDTO userInfoDTO = userInfoDAO.getUserInfo(userId, password);
		      session.put("userId", userInfoDTO.getUserId());
		      session.put("logined", 1);
		      session.remove("tempUserId");
		      if(savedUserIdFlag){
			      session.put("savedUserIdFlag", true);
		      }
		      session.remove("tempUserId");
		//入力したユーザーIDまたはパスワードが違っていた場合の処理
		}else{
			setLoginInfoErrorMessage("ユーザーIDまたはパスワードが異なります。");
		}
		return result;
	}

	//カート情報の更新
	private boolean changeCartInfo(List<CartInfoDTO> cartInfoDTOListForTempUser, String tempUserId){
		int count=0;
		CartInfoDAO cartInfoDAO=new CartInfoDAO();
		boolean result=false;

		//仮ユーザーIDと紐付いたカート情報を一件ずつ更新
		for(CartInfoDTO dto : cartInfoDTOListForTempUser){
			//同じ商品IDのカート情報が存在した場合
			if(cartInfoDAO.isExistCartInfo(userId, dto.getProductId())){
				//処理対象のカート情報の製品の個数を、既に存在するユーザーのカート情報の個数に足す
				count+=cartInfoDAO.updateCartInfo(userId, dto.getProductId(), dto.getProductCount());
				//処理対象の該当するカート情報を削除する
				cartInfoDAO.deleteCartInfo(String.valueOf(dto.getProductId()), tempUserId);
			//同じ商品IDのカート情報が存在しなかった場合
			}else{
				//処理対処のカート情報のユーザーIDを、ログインするユーザーIDに書き換える
				count+=cartInfoDAO.linkToUserId(tempUserId,userId, dto.getProductId());
			}
		}

		//仮ユーザーIDに紐付いたカート情報が全て正しく処理された場合
		if(count==cartInfoDTOListForTempUser.size()){
			cartInfo=cartInfoDAO.getCartInfo(userId);
			totalPrice=cartInfoDAO.getTotalPrice(userId);
			result=true;
		}
		return result;
	}

	public List<CartInfoDTO> getCartInfo() {
		return cartInfo;
	}

	public void setCartInfo(List<CartInfoDTO> cartInfo) {
		this.cartInfo = cartInfo;
	}

	public Map<String, Object> getSession() {
		return session;
	}

	public String getUserId(){
		return userId;
	}

	public void setUserId(String userId){
		this.userId=userId;
	}

	public String getPassword(){
		return password;
	}

	public void setPassword(String password){
		this.password=password;
	}

	public List<String> getUserIdErrorMessageList(){
		return userIdErrorMessageList;
	}

	public void setUserIdErrorMessageList(List<String> userIdErrorMessageList){
		this.userIdErrorMessageList=userIdErrorMessageList;
	}

	public List<String> getPasswordErrorMessageList(){
		return passwordErrorMessageList;
	}

	public void setPasswordErrorMessageList(List<String> passwordErrorMessageList){
		this.passwordErrorMessageList=passwordErrorMessageList;
	}

	public String getLoginInfoErrorMessage(){
		return loginInfoErrorMessage;
	}

	public void setLoginInfoErrorMessage(String loginInfoErrorMessage){
		this.loginInfoErrorMessage=loginInfoErrorMessage;
	}

	public void setSession(Map<String, Object> session){
		this.session=session;
	}

	public int getTotalPrice(){
		return totalPrice;
	}

	public void setTotalPrice(int totalPrice){
		this.totalPrice=totalPrice;
	}

	public boolean getSavedUserIdFlag(){
		return savedUserIdFlag;
	}

	public void setSavedUserIdFlag(boolean savedUserIdFlag){
		this.savedUserIdFlag=savedUserIdFlag;
	}
}