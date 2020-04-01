package fmtaddr;

public class LineAddress {
private String addrLine1;
private String addrLine2;
public LineAddress() {
	addrLine1 = "";
	addrLine2 = "";
}
public final String getAddrLine1() {
	return addrLine1;
}
public final void setAddrLine1(String addrLine1) {
	this.addrLine1 = addrLine1;
}
public final String getAddrLine2() {
	return addrLine2;
}
public final void setAddrLine2(String addrLine2) {
	this.addrLine2 = addrLine2;
}
@Override
public String toString() {
	return String.format("LineAddress [addrLine1=%s, addrLine2=%s]", addrLine1, addrLine2);
}

}
