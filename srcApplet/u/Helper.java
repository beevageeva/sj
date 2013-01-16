package u;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class Helper {

	public void loadFileStream(InputStream is, OutputStream dOut)
			throws FileNotFoundException, IOException {
		byte[] byteBuff = null;
		try {
			int numBytes = 0;
			byteBuff = new byte[1024];
			while (-1 != (numBytes = is.read(byteBuff))) {
				dOut.write(byteBuff, 0, numBytes);
			}
		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
			byteBuff = null;
		}
	}
	
	public static String convertDecimalToBinary(long iNumber, int numberOfBits){
		String bin = "";
		while (iNumber>0) {
			if (iNumber%2!=0) {
				bin = "1"+bin;
			} else {
				bin = "0"+bin;
			}
			iNumber = (int) Math.floor(iNumber/2);
		}
		if(bin.length() > numberOfBits){
			//get the more repr bits
			Logger.log("string does not fit : binary repr  length = " + bin.length() + " , numberBits = " + numberOfBits+" , string will be truncated");
			return bin.substring(0, numberOfBits);
		}
		// left pad with zeros
		while (bin.length()<numberOfBits) {
			bin = "0"+bin;
		}
		return bin;
	}
	
	
	

}
