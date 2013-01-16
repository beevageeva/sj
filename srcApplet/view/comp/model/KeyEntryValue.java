package view.comp.model;

public class KeyEntryValue {

	public KeyEntryValue(int key, int value) {
		this.key = key;
		this.value = value;
	}

	private int key;

	private int value;

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof KeyEntryValue)) {
			return false;
		}
		return key == ((KeyEntryValue) obj).getKey()
				|| value == ((KeyEntryValue) obj).getValue();
	}

	@Override
	public int hashCode() {
		return key;
	}

	@Override
	public String toString() {
		return key + " | " +value;
	}
	
	
	

}
