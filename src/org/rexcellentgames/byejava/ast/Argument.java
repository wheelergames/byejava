package org.rexcellentgames.byejava.ast;

import java.util.ArrayList;

public class Argument {
	public String name;
	public String type;
	public ArrayList<Generetic> generetics;
	public boolean varg;
	public int array;

	public Argument(String name, String type, ArrayList<Generetic> generetics, boolean varg, int array) {
		this.name = name;
		this.type = type;
		this.generetics = generetics;
		this.varg = varg;
		this.array = array;
	}

	public Argument() {

	}
}
