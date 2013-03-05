package balle.world.filter;

import Jama.Matrix;
import balle.world.Snapshot;

public class KalmanFilter implements Filter {

	static Matrix X;
	static Matrix Xk1;
	static Matrix P;
	static Matrix Pk1;
	static Matrix Zk1;
	static Matrix Vk;
	static Matrix Gain;
	static Matrix Sk1;

	static Matrix F, B, U, Q;
	static Matrix H, R;

	public void predict() {
		Xk1 = F.times(X).plus(B.times(U));
		Pk1 = F.times(P).times(F.transpose()).plus(Q);
	}

	public void observation(Matrix z) {
		Vk = z.minus(H.times(Xk1));
	}

	public void update() {

		// innovation covariance
		Sk1 = H.times(Pk1).times(H.transpose()).plus(R);

		// compute kalman gain
		Gain = Pk1.times(H.transpose()).times(Sk1.inverse());

		// update state estimate via Z
		X = Xk1.plus(Gain.times(Vk));

		// update error covariance
		Matrix I = Matrix.identity(Pk1.getRowDimension(),
				Pk1.getColumnDimension());
		P = (I.minus(Gain.times(H))).times(Pk1);
	}

	// public Matrix getX() {
	// return X;
	// }
	//
	// public void setX(Matrix X) {
	// X = X;
	// }
	//
	// public Matrix getXk1() {
	// return Xk1;
	// }
	//
	// public void setX_hat_k_1(Matrix xHatK_1) {
	// Xk1 = xHatK_1;
	// }
	//
	// public Matrix getP() {
	// return P;
	// }
	//
	// public void setP(Matrix P) {
	// P = P;
	// }
	//
	// public Matrix getPk1() {
	// return pk1;
	// }
	//
	// public void setPk1(Matrix Pk1) {
	// pk1 = Pk1;
	// }
	//
	// public Matrix getZk1() {
	// return zk1;
	// }
	//
	// public void setZk1(Matrix Zk1) {
	// zk1 = Zk1;
	// }
	//
	// public Matrix getVk() {
	// return vk;
	// }
	//
	// public void setVk(Matrix Vk) {
	// vk = Vk;
	// }
	//
	// public Matrix getKalmanGain() {
	// return gain;
	// }
	//
	// public void setKalmanGain(Matrix kalmanGain) {
	// gain = kalmanGain;
	// }
	//
	// public Matrix getSk1() {
	// return sk1;
	// }
	//
	// public void setSk1(Matrix Sk1) {
	// sk1 = Sk1;
	// }
	//
	// public Matrix getF() {
	// return f;
	// }
	//
	// public void setF(Matrix F) {
	// f = F;
	// //System.out.println("f set in kf->"+F.getColumnDimension());
	// }
	//
	// public Matrix getB() {
	// return b;
	// }
	//
	// public void setB(Matrix B) {
	// b = B;
	// }
	//
	// public Matrix getU() {
	// return u;
	// }
	//
	// public void setU(Matrix U) {
	// u = U;
	// }
	//
	// public Matrix getQ() {
	// return q;
	// }
	//
	// public void setQ(Matrix Q) {
	// q = Q;
	// }
	//
	// public Matrix getH() {
	// return h;
	// }
	//
	// public void setH(Matrix H) {
	// h = H;
	// }
	//
	// public Matrix getR() {
	// return r;
	// }
	//
	// public void setR(Matrix R) {
	// r = R;
	// }

	public Snapshot filter(Snapshot s) {
		double x = s.getBalle().getPosition().x;
		double y = s.getBalle().getPosition().y;
		return s;
	}

}
