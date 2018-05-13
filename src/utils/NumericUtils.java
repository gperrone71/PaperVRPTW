package utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


/**
 * 
 * Class to obtain customized string conversions for double values
 * 
 * @author Massimo
 *
 */
public class NumericUtils {

	private static DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("#.##");
	private static DecimalFormat decimalFormat = null;
	
	private NumericUtils() {
	 // TODO Auto-generated constructor stub
	}
	
	public static void setFormat(String format) {
		decimalFormat = new DecimalFormat(format);		// was "#.###"
	}
	
	public static void setPointAsSep() {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ITALY);
		otherSymbols.setDecimalSeparator('.');
		otherSymbols.setGroupingSeparator(','); 
		decimalFormat = new DecimalFormat("#.##", otherSymbols);
	}
	
	public static void setDefaultFormat() {
		decimalFormat = new DecimalFormat(DEFAULT_DECIMAL_FORMAT.toPattern());
	}
	
	public static String Double2String(Double d) {
	
		if (decimalFormat != null) {
			return decimalFormat.format(d);
		}
		return DEFAULT_DECIMAL_FORMAT.format(d);
	
	}
}

