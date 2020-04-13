/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp;

import edu.skku.selab.blp.blia.analysis.BLIA;
import edu.skku.selab.blp.db.dao.DbUtil;
import edu.skku.selab.blp.evaluation.Evaluator;
import edu.skku.selab.blp.evaluation.EvaluatorForMethodLevel;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
// 1. vsm特征作用不明显，可以把sim,commit, stack等其他特征去掉验证
// 2. bug locator可能与blia score排名类似(如何设置bug locator特征的？)
// 3. tomcat项目中的commit_score和stack_score都很少，为什么？
// 4. method level的准确度很低，可能由于只取前10个file的method，受file精度影响较大考虑使用全部method
// 5. 为什么tomcat的效果这么差，而aspectj还可以(新的aspectj也有stack score和commit score)？
public class BLP {
	private static void initializeDB() throws Exception {
		Property prop = Property.getInstance();
		String beforeName = prop.getProductName();
		
		DbUtil dbUtil = new DbUtil();

		dbUtil.openConnetion(beforeName);

		dbUtil.dropAllAnalysisTables();
		dbUtil.createAllAnalysisTables();

		dbUtil.initializeAllData();

		dbUtil.closeConnection();
		
		dbUtil.openEvaluationDbConnection();

		dbUtil.dropEvaluationTable();
		dbUtil.createEvaluationTable();
		
		dbUtil.initializeExperimentResultData();
		
		dbUtil.closeConnection();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// Load properties data to run BLIA
		Property prop = Property.loadInstance();
		
		// initialize DB and create all tables.
		initializeDB();

		// Run BLIA algorithm
		BLIA blia = new BLIA();
		blia.run();
		
		String algorithmDescription = "[BLIA] alpha: " + prop.getAlpha() +
				", beta: " + prop.getBeta() + ", gamma: " + prop.getGamma() + ", pastDays: " + prop.getPastDays() +
				", cadidateLimitRate: " + prop.getCandidateLimitRate();

		// Evaluate the accuracy result of BLIA
		Evaluator evaluator1 = new Evaluator(prop.getProductName(),
				Evaluator.ALG_BLIA_FILE, algorithmDescription, prop.getAlpha(),
				prop.getBeta(), prop.getGamma(), prop.getPastDays(),
				prop.getCandidateLimitRate());
		evaluator1.evaluate();

		if(prop.isMethodLevel()) {
			Evaluator evaluator2 = new EvaluatorForMethodLevel(prop.getProductName(),
					EvaluatorForMethodLevel.ALG_BLIA_METHOD, algorithmDescription, prop.getAlpha(),
					prop.getBeta(), prop.getGamma(), prop.getPastDays(),
					prop.getCandidateLimitRate());
			evaluator2.evaluate();
		}

		// Evaluate the accuracy result of BugLocator
		Evaluator evaluator3 = new Evaluator(prop.getProductName(),
				Evaluator.ALG_BUG_LOCATOR, algorithmDescription, prop.getAlpha(),
				prop.getBeta(), prop.getGamma(), prop.getPastDays(),
				prop.getCandidateLimitRate());
		evaluator3.evaluate();
	}
}
