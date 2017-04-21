package com.xk.utils;

import com.xk.bean.Pointd;

/**
 * 贝塞尔曲线计算公式
 * @author o-kui.xiao
 *
 */
public class BezierUtil {

	/**
	 * 计算贝塞尔曲线在t上的一个点
	 * @param t
	 * @param points
	 * @return
	 * @author o-kui.xiao
	 */
	public static Pointd computLine(double t, Pointd... points) {
		if(null == points) {
			return new Pointd(0, 0);
		}
		int length = points.length;
		double x = 0d;
		for(int i = 0; i < length; i++) {
			if(null != points[i]) {
				x += computPoint(length - 1, t, points[i].x, i);
			}
		}
		double y = 0d;
		for(int i = 0; i < length; i++) {
			if(null != points[i]) {
				y += computPoint(length - 1, t, points[i].y, i);
			}
		}
		return new Pointd(x, y);
	}
	
	/**
	 * 解贝塞尔N阶方程其中一个累加表达式
	 * @param n
	 * @param t
	 * @param p
	 * @param i
	 * @return
	 * @author o-kui.xiao
	 */
	private static double computPoint(int n, double t, double p, int i) {
		double result = (getFactorialSum(n)/(getFactorialSum(i) * getFactorialSum(n - i)))
				* p * Math.pow((1 - t), n - i) * Math.pow(t, i);
		return result;
	}
	
	/**
	 * 计算阶乘
	 * @param n
	 * @return
	 * @author o-kui.xiao
	 */
	private static int getFactorialSum(int n) {
		if (n == 1 || n == 0) {
			return 1;
		} else {
			return getFactorialSum(n - 1) * n;
		}
	}
	
}
