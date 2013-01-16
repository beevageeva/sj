package trace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import u.Logger;
import view.model.CacheEntry;



public class InstrReader {


	private List<Instr> instructions;

	private String name;
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public InstrReader(InputStream is, int virtualAddressNBits, String name)
			throws IOException {
		this.name = name;
		instructions = new ArrayList<Instr>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		int i1, i2;
		long value;
		String type;
		int i = 0;
		Instr instr;
		String addressString;
		while ((line = reader.readLine()) != null) {
			i++;
			// add1
			i1 = 0;
			while (Character.isWhitespace(line.charAt(i1))) {
				i1++;
			}
			i2 = i1;
			while (Character.isLetterOrDigit(line.charAt(i2))) {
				i2++;
			}
			addressString = line.substring(i1, i2);
			try {
				value = Long.parseLong(addressString, 16);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				Logger.log("line " + i + " of file ");
				value = -1;
			}
			instr = new Instr();
			instructions.add(instr);
			instr.addressBinaryRepr = u.Helper.convertDecimalToBinary(value,
					virtualAddressNBits);
			// type
			i1 = i2;
			while (Character.isWhitespace(line.charAt(i1))) {
				i1++;
			}
			i2 = i1;
			while (Character.isLetterOrDigit(line.charAt(i2))) {
				i2++;
			}
			type = line.substring(i1, i2);
			if (type.equalsIgnoreCase("FETCH")) {
				instr.type = CacheEntry.FETCHINSTR;
			} else if (type.equalsIgnoreCase("MEMREAD")) {
				instr.type = CacheEntry.READDATA;
			} else if (type.equalsIgnoreCase("MEMWRITE")) {
				instr.type = CacheEntry.MODIFDATA;
			} else {
				Logger.log("line " + i + " of file ");
			}
			// time not used
			i1 = i2;
			while (Character.isWhitespace(line.charAt(i1))) {
				i1++;
			}
			i2 = i1;
			while (i2 < line.length()
					&& Character.isLetterOrDigit(line.charAt(i2))) {
				i2++;
			}
			try {
				value = Integer.parseInt(line.substring(i1, i2), 10);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				Logger.log("line " + i + " of file ");
				value = -1;
				break;
			}

		}

	}


	public List<Instr> getInstructions() {
		return instructions;
	}


}
