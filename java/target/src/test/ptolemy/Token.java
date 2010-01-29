package ptolemy;
public class Token {
    public Short type;
    Object payload;
	public Token() {};
	public Short getType() {
		return type; 
	}
	public Object getPayload() {
		return payload;
	}
}
