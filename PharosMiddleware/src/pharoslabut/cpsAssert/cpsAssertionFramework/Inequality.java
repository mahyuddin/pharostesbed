package pharoslabut.cpsAssert.cpsAssertionFramework;

public enum Inequality {
	LESS_THAN {
	    public String toString() {
	        return "less than";
	    }
	},
	
	LESS_THAN_EQUAL_TO{
	    public String toString() {
	        return "less than or equal to";
	    }
	},
	
	EQUAL_TO{
	    public String toString() {
	        return "equal to";
	    }
	},
	
	GREATER_THAN{
	    public String toString() {
	        return "greater than";
	    }
	},
	
	GREATER_THAN_EQUAL_TO{
	    public String toString() {
	        return "greater than or equal to";
	    }
	};

	public String toMathString() {
		switch (this) {
			case LESS_THAN: return "<";
			case LESS_THAN_EQUAL_TO: return "<=";
			case EQUAL_TO: return "==";
			case GREATER_THAN: return ">";
			case GREATER_THAN_EQUAL_TO: return ">=";
			default: return null;
		}
	}
}
