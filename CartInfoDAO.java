package com.internousdev.florida.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.internousdev.florida.dto.CartInfoDTO;
import com.internousdev.florida.util.DBConnector;

public class CartInfoDAO {

	public ArrayList<CartInfoDTO> getCartInfo(String userId) {
		ArrayList<CartInfoDTO> cartList = new ArrayList<>();
		DBConnector db = new DBConnector();
		Connection con = db.getConnection();
		String sql = "SELECT"
				+ " ci.id,"
				+ " ci.user_id,"
				+ " ci.product_id,"
				+ " ci.product_count,"
				+ " pi.product_name,"
				+ " pi.product_name_kana,"
				+ " pi.price,"
				+ " pi.image_file_path,"
				+ " pi.image_file_name,"
				+ " pi.release_company,"
				+ " pi.release_date,"
				+ " pi.status,"
				+ " ci.regist_date,"
				+ " ci.update_date"
				+ " FROM cart_info ci"
				+ " LEFT JOIN product_info pi"
				+ " ON ci.product_id = pi.product_id"
				+ " WHERE ci.user_id = ?"
				+ " order by update_date desc, regist_date desc";
		try{
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, userId);

			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				CartInfoDTO dto = new CartInfoDTO();
				dto.setId(rs.getInt("id"));
				dto.setUserId(rs.getString("user_id"));
				dto.setProductId(rs.getInt("product_id"));
				dto.setProductCount(rs.getInt("product_count"));
				dto.setProductName(rs.getString("product_name"));
				dto.setProductNameKana(rs.getString("product_name_kana"));
				dto.setPrice(rs.getInt("price"));
				//購入個数と価格を掛け合わせたものをsubPriceとして定義する
				int subPrice = rs.getInt("product_count") * rs.getInt("price");
				dto.setSubtotal(subPrice);
				dto.setImageFilePath(rs.getString("image_file_path"));
				dto.setImageFileName(rs.getString("image_file_name"));
				dto.setReleaseCompany(rs.getString("release_company"));
				dto.setReleaseDate(rs.getDate("release_date"));
				cartList.add(dto);
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}finally{
			try{
				con.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return cartList;
	}

	public int getTotalPrice(String userId){
		int totalPrice = 0;
		DBConnector db = new DBConnector();
		Connection con = db.getConnection();
		String sql="select "
				+"sum(product_count * price) as total_price"
				+" from cart_info ci"
				+" join product_info pi"
				+" on ci.product_id = pi.product_id"
				+" where user_id = ? group by user_id";
		try{
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, userId);
			ResultSet resultSet = ps.executeQuery();
			if(resultSet.next()){
				totalPrice = resultSet.getInt("total_price");
			}
		    }catch(SQLException e){
				e.printStackTrace();
			}finally{
				try{
					con.close();
				}catch(SQLException e) {
					e.printStackTrace();
				}
			}
			return totalPrice;
	}

	public int deleteCartInfo(String userId, String product_id) {
		DBConnector db = new DBConnector();
		Connection con = db.getConnection();
		String sql = "DELETE FROM cart_info WHERE user_id = ? AND product_id = ?";

		int count = 0;
		try{
			PreparedStatement ps = con.prepareStatement(sql);

			ps.setString(1, userId);
			ps.setString(2, product_id);

			count = ps.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try{
				con.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return count;
	}

	public boolean isExistCartInfo(String userId, int productId) {
		DBConnector db = new DBConnector();
		Connection con = db.getConnection();

		String sql = "SELECT COUNT(id) as count FROM cart_info WHERE user_id = ? AND product_id = ?";

		boolean result = false;
		try {
			PreparedStatement ps = con.prepareStatement(sql);

			ps.setString(1, userId);
			ps.setInt(2, productId);
			ResultSet rs = ps.executeQuery();

			if(rs.next()) {
				if(rs.getInt("count") > 0){
					result = true;
				}
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}finally{
			try{
				con.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public int updateCartInfo(String userId, int productId, int buyCount) {
		DBConnector db = new DBConnector();
		Connection con = db.getConnection();

		String sql = "UPDATE cart_info SET product_count = (product_count +?),update_date = now() WHERE user_id = ? AND product_id = ?";

		int count = 0;
		try{
			PreparedStatement ps = con.prepareStatement(sql);

			ps.setInt(1, buyCount);
			ps.setString(2, userId);
			ps.setInt(3, productId);

			count = ps.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try{
				con.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return count;
	}

	public int linkToUserId(String tempUserId, String userId, int productId){
		DBConnector db = new DBConnector();
		Connection con = db.getConnection();

		String sql="UPDATE cart_info set user_id=?, update_date = now() WHERE user_id = ? AND product_id = ?";

		int count = 0;
		try{
			PreparedStatement ps = con.prepareStatement(sql);

			ps.setString(1, userId);
			ps.setString(2, tempUserId);
			ps.setInt(3, productId);

			count = ps.executeUpdate();
		}catch (SQLException e){
			e.printStackTrace();
		}finally{
			try{
				con.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return count;
	}

	public int addCartInfo(String userId, int productId, int buyCount) {

		DBConnector db = new DBConnector();
		Connection con = db.getConnection();

		String sql = "INSERT INTO cart_info(user_id,product_id,product_count,regist_date,update_date)"
				+ " VALUES(?,?,?,now(),now())";

		int count =0;

		try{
			PreparedStatement ps = con.prepareStatement(sql);

			ps.setString(1, userId);
			ps.setInt(2, productId);
			ps.setInt(3, buyCount);

			count = ps.executeUpdate();
		}catch( SQLException e) {
			e.printStackTrace();
		}finally{
			try{
				con.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return count;
	}

	public int deleteAll(String userId) {
		DBConnector db = new DBConnector();
		Connection con = db.getConnection();

		String sql = "DELETE FROM cart_info WHERE user_id = ?";
		int count = 0;
		try {
			PreparedStatement ps = con.prepareStatement(sql);

			ps.setString(1, userId);

			 count = ps.executeUpdate();
		}catch(SQLException e) {
			e.printStackTrace();
		}finally{
			try{
				con.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return count;
	}
}
