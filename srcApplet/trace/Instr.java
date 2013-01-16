package trace;

import u.Graph;

public class Instr {

	public Instr() {
	};

	public Instr(short type, String addressBinaryRepr) {
		this.type = type;
		this.addressBinaryRepr = addressBinaryRepr;
	}

	public short type;

	public String addressBinaryRepr;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(Graph.getLabelText(type));
		sb.append(" ");
		sb.append(Integer.parseInt(addressBinaryRepr, 2));
		sb.append("(");
		sb.append(addressBinaryRepr);
		sb.append(")");
		return sb.toString();
	}
	
	
	
}
