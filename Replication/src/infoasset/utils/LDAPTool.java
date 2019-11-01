package infoasset.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 *
 * @author itasset
 */
public class LDAPTool {
	
	public static final LDAPTool newInstanceById(String employeeID)
			throws NamingException {
		return new LDAPTool(employeeID);
	}
	public static final LDAPTool newInstance(String userName)
			throws NamingException {		
		return new LDAPTool(userName, null);
	}

	public static final LDAPTool newInstance(String userName, String password)
			throws NamingException {
		return new LDAPTool(userName, password);
	}

	private static final String CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	private static final String LDAP_URL = "ldap://ldapserver:389";
	private static final String DOMAIN = "dc=thailife,dc=com";
	private static final String PATTERN_ORG_CODE = "(ou=)(\\d+)";
	
	private String userName;
	private String employeeID;
	private String orgCode;
	private String employeeName;
	private Date exitDate;
	private boolean isActive = true;
	private LDAPTool(String employeeID) throws NamingException {
		InitialDirContext context = null;
		
			context = getDirContext();
		
		SearchControls ctrls = new SearchControls();
		ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		//ctrls.setReturningAttributes(new String[] { "userName", "uid","shadowExpire", "thFirstName", "thLastName" });
		String filter = "pid=" + employeeID.substring(0, 3) + "-" + employeeID.substring(3);
		NamingEnumeration<SearchResult> answers = context.search(DOMAIN,
				filter, ctrls);
		while (answers.hasMoreElements()) {			
			SearchResult result = answers.nextElement();
			printAttributes(result);
			this.employeeID = employeeID;			
			userName = result.getAttributes().get("uid").get().toString();
			employeeName = result.getAttributes().get("empName").get().toString();
			
			orgCode = searchOrgCode(result.getNameInNamespace());		
			Attribute expireObj = result.getAttributes().get("shadowExpire");
			if (expireObj != null) {
				 isActive = false;
				 long dtLong  = Long.parseLong(expireObj.get().toString());				
				 Calendar cal = Calendar.getInstance();
				 cal.setTimeInMillis(dtLong * 24 * 60 * 60 * 1000);				 				 
				 exitDate = cal.getTime();
				 
			 }
		
			break;
		}
	}

	private LDAPTool(String userName, String password) throws NamingException {
		
		InitialDirContext context = null;
		if (password == null) {
			context = getDirContext();
		} else {
			context = getDirContext(userName, password);
		}		//	userName = StringUtils.substringAfter(result.getAttributes().get("uid").toString(),":").trim();
		SearchControls ctrls = new SearchControls();
		ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		this.userName = userName;
		//ctrls.setReturningAttributes(new String[] { "givenName", "pid", "shadowExpire"});
		String filter = "uid=" + userName;
		NamingEnumeration<SearchResult> answers = context.search(DOMAIN,filter, ctrls);
		while (answers.hasMoreElements()) {
			SearchResult result = answers.nextElement();
			printAttributes(result);
			employeeID = result.getAttributes().get("pid").get().toString().replaceAll("\\D","");
			orgCode = searchOrgCode(result.getNameInNamespace());
			employeeName = result.getAttributes().get("empName").get().toString();
			Attribute expireObj = result.getAttributes().get("shadowExpire");
			if (expireObj != null) {
				 isActive = false;
				 long dtLong  = Long.parseLong(expireObj.get().toString());				
				 Calendar cal = Calendar.getInstance();
				 cal.setTimeInMillis(dtLong * 24 * 60 * 60 * 1000);				 				 
				 exitDate = cal.getTime();
			 }
											
		}
	}

	private String dnFromUserName(String userName) throws NamingException {
		String dn = "";
		InitialDirContext context = getDirContext();
		SearchControls ctrls = new SearchControls();
		ctrls.setReturningAttributes(new String[] { "givenName", "pid" });
		ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String filter = "uid=" + userName;
		NamingEnumeration<SearchResult> answers = context.search(DOMAIN,
				filter, ctrls);
		while (answers.hasMoreElements()) {
			
			SearchResult result = answers.next();
			printAttributes(result);
			dn = result.getNameInNamespace();
		}		
		return dn;
	}

	private void printAttributes(SearchResult result) {
		/*
				Attributes attrs = result.getAttributes();

		NamingEnumeration<String> ids = attrs.getIDs();		
		while (ids.hasMoreElements()) {
			String attrID = ids.nextElement();
			Attribute attr = attrs.get(attrID);	
			try {
				System.out.printf("Attr id= %s value=%s\n", attrID, attr.get().toString());
			} catch (NamingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
*/
	}


	private InitialDirContext getDirContext(String userName, String password)
			throws NamingException {
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
		props.put(Context.PROVIDER_URL, LDAP_URL);
		props.put(Context.REFERRAL, "ignore");
		props.put(Context.SECURITY_AUTHENTICATION, "simple");
		props.put(Context.SECURITY_PRINCIPAL, dnFromUserName(userName));
		props.put(Context.SECURITY_CREDENTIALS, password);
		InitialDirContext context = new InitialDirContext(props);
		return context;
	}

	private InitialDirContext getDirContext() throws NamingException {
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
		props.put(Context.PROVIDER_URL, LDAP_URL);
		props.put(Context.REFERRAL, "ignore");
		InitialDirContext context = new InitialDirContext(props);
		return context;
	}

	private String searchOrgCode(String nameSpace) {
		Pattern pattern = Pattern.compile(PATTERN_ORG_CODE);
		Matcher match = pattern.matcher(nameSpace);
		if (match.find()) {
			return match.group(2);
		}
		return null;
	}
	
	
	
	@Override
	public String toString() {
		return "LDAPTool [userName=" + userName + ", employeeID=" + employeeID + ", orgCode=" + orgCode
				+ ", employeeName=" + employeeName + ", exitDate=" + exitDate + ", isActive=" + isActive + "]";
	}
	public boolean isActiveEmployee() {
		return isActive;
	}

	public String getEmployeeID() {
		return employeeID;
	}

	public String getOrgCode() {
		return orgCode;
	}
	public String getUserName() {
		return userName;
	}
	public String getEmployeeName() {
		return employeeName;
	}
}