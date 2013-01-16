package config;

public 	class InverseMappedPageTableCfg implements Config{
	private int hashAnchorSizeNBits = 0;
	
	public int getHashAnchorSizeNBits() {
		return hashAnchorSizeNBits;
	}

	public void setHashAnchorSizeNBits(int hashAnchorSizeNBits) {
		this.hashAnchorSizeNBits = hashAnchorSizeNBits;
	}

	public String getStringInfo() {
		StringBuffer sb = new StringBuffer();
		sb.append("Hash anchor size = ");
		sb.append("2 ** ");
		sb.append(hashAnchorSizeNBits);
		return sb.toString();
	}

}
