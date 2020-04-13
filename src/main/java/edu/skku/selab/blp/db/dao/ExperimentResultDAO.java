/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.db.dao;

import edu.skku.selab.blp.db.ExperimentResult;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class ExperimentResultDAO extends BaseDAO {

	/**
	 * @throws Exception
	 */
	public ExperimentResultDAO() throws Exception {
		super();
	}
	
	public int insertExperimentResult(ExperimentResult experimentResult) {
		String sql = "INSERT INTO EXP_INFO (TOP1, TOP5, TOP10, TOP20, TOP50, TOP1_RATE, TOP5_RATE, TOP10_RATE, TOP20_RATE, TOP50_RATE, MRR, MAP, PROD_NAME, ALG_NAME, ALG_DESC, ALPHA, BETA, GAMMA, PAST_DAYS, EXP_DATE) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?)";
		int returnValue = INVALID;
		
		try {
			ps = evaluationDbConnection.prepareStatement(sql);
			ps.setInt(1, experimentResult.getTop1());
			ps.setInt(2, experimentResult.getTop5());
			ps.setInt(3, experimentResult.getTop10());
			ps.setInt(4, experimentResult.getTop20());
			ps.setInt(5, experimentResult.getTop50());
			ps.setDouble(6, experimentResult.getTop1Rate());
			ps.setDouble(7, experimentResult.getTop5Rate());
			ps.setDouble(8, experimentResult.getTop10Rate());
			ps.setDouble(9, experimentResult.getTop20Rate());
			ps.setDouble(10, experimentResult.getTop50Rate());
			ps.setDouble(11, experimentResult.getMRR());
			ps.setDouble(12, experimentResult.getMAP());
			ps.setString(13, experimentResult.getProductName());
			ps.setString(14, experimentResult.getAlgorithmName());
			ps.setString(15, experimentResult.getAlgorithmDescription());
			ps.setDouble(16, experimentResult.getAlpha());
			ps.setDouble(17, experimentResult.getBeta());
			ps.setDouble(18, experimentResult.getGamma());
			ps.setInt(19, experimentResult.getPastDays());
			ps.setString(20, experimentResult.getExperimentDateString());
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}

	public int deleteAllExperimentResults() {
		String sql = "DELETE FROM EXP_INFO";
		int returnValue = INVALID;
		
		try {
			ps = evaluationDbConnection.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public ExperimentResult getExperimentResult(String productName, String algorithmName) {
		ExperimentResult returnValue = null;

		String sql = "SELECT TOP1, TOP5, TOP10, TOP20, TOP50, TOP1_RATE, TOP5_RATE, TOP10_RATE, TOP20_RATE, TOP50_RATE, MRR, MAP, ALG_DESC, ALPHA, BETA, GAMMA, PAST_DAYS, EXP_DATE "+
				"FROM EXP_INFO " +
				"WHERE PROD_NAME = ? AND ALG_NAME = ?";
		
		try {
			ps = evaluationDbConnection.prepareStatement(sql);
			ps.setString(1, productName);
			ps.setString(2, algorithmName);
			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				returnValue = new ExperimentResult();
				
				returnValue.setTop1(rs.getInt("TOP1"));
				returnValue.setTop5(rs.getInt("TOP5"));
				returnValue.setTop10(rs.getInt("TOP10"));
				returnValue.setTop20(rs.getInt("TOP20"));
				returnValue.setTop50(rs.getInt("TOP50"));
				returnValue.setTop1Rate(rs.getDouble("TOP1_RATE"));
				returnValue.setTop5Rate(rs.getDouble("TOP5_RATE"));
				returnValue.setTop10Rate(rs.getDouble("TOP10_RATE"));
				returnValue.setTop20Rate(rs.getDouble("TOP20_RATE"));
				returnValue.setTop50Rate(rs.getDouble("TOP50_RATE"));
				returnValue.setMRR(rs.getDouble("MRR"));
				returnValue.setMAP(rs.getDouble("MAP"));
				returnValue.setProductName(productName);
				returnValue.setAlgorithmName(algorithmName);
				returnValue.setAlgorithmDescription(rs.getString("ALG_DESC"));
				returnValue.setAlpha(rs.getDouble("ALPHA"));
				returnValue.setBeta(rs.getDouble("BETA"));
				returnValue.setBeta(rs.getDouble("GAMMA"));
				returnValue.setPastDays(rs.getInt("PAST_DAYS"));
				returnValue.setExperimentDate(rs.getTimestamp("EXP_DATE"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
}
