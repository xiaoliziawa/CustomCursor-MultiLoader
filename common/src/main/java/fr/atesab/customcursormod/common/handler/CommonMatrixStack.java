package fr.atesab.customcursormod.common.handler;

import org.joml.Matrix3x2fStack;

public class CommonMatrixStack extends BasicHandler<Matrix3x2fStack> {
	public CommonMatrixStack(Matrix3x2fStack handle) {
		super(handle);
	}

	public void push() {
		handle.pushMatrix();
	}

	public void pop() {
		handle.popMatrix();
	}

	public void scale(float x, float y) {
		handle.scale(x, y);
	}

	public void scale(float factor) {
		scale(factor, factor);
	}

	public void translate(float x, float y) {
		handle.translate(x, y);
	}
}
